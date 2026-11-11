package setup.app;

import setup.domain.*;
import setup.events.*;
import setup.pricing.*;
import setup.storage.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * PATTERN: Facade (structural).
 * One simple entry point for the web layer. It hides the repository, catalog,
 * compatibility checker, price calculator and event bus.
 */
public final class SetupFacade {
    public record Summary(PcBuild build, List<String> issues, PriceCalculator.Quote quote) {}

    private final Catalog catalog = new Catalog();
    private final BuildRepository repo;
    private final OrderRepository orders;
    private final PriceCalculator calculator = new PriceCalculator();
    private final CompatibilityChecker checker = new CompatibilityChecker();
    private final BuildEventBus bus = new BuildEventBus();
    private final ActivityLog log = new ActivityLog();

    public SetupFacade(Path dataFile) {
        repo = new BuildRepository(dataFile, catalog);
        orders = new OrderRepository(dataFile.resolveSibling(dataFile.getFileName().toString().replaceFirst("\\.txt$", "") + ".orders.txt"));
        bus.subscribe(log);
        bus.subscribe(new CompatibilityWatcher(repo::find, checker, bus));
        if (repo.findAll().isEmpty()) seedSamples();
    }

    public Catalog catalog() { return catalog; }
    public List<String> activity() { return log.latest(); }

    public List<Summary> listBuilds() {
        return repo.findAll().stream().map(b -> summary(b.id())).toList();
    }

    public PcBuild getBuild(String id) {
        PcBuild b = repo.find(id);
        if (b == null) throw new ValidationException("Build not found: " + id);
        return b;
    }

    public Summary summary(String id) {
        PcBuild b = getBuild(id);
        return new Summary(b, checker.check(b), calculator.quote(b));
    }

    public PcBuild createBuild(String name) {
        PcBuild b = PcBuild.builder().name(name).build();
        repo.save(b);
        bus.publish(new BuildEvent("CREATED", b.id(), "Build '" + b.name() + "' created"));
        return b;
    }

    public PcBuild setComponent(String buildId, String componentId) {
        Component c = catalog.find(componentId);
        if (c == null) throw new ValidationException("Unknown component: " + componentId);
        PcBuild b = getBuild(buildId).toBuilder().part(c).build();
        repo.save(b);
        bus.publish(new BuildEvent("CHANGED", buildId, "'" + b.name() + "': " + c.type() + " set to " + c.name()));
        return b;
    }

    public PcBuild setUpgrade(String buildId, String code, boolean on) {
        if (!Upgrades.LABELS.containsKey(code)) throw new ValidationException("Unknown upgrade: " + code);
        PcBuild b = getBuild(buildId).toBuilder().upgrade(code, on).build();
        repo.save(b);
        bus.publish(new BuildEvent("CHANGED", buildId,
                "'" + b.name() + "': " + Upgrades.LABELS.get(code) + (on ? " added" : " removed")));
        return b;
    }

    public PcBuild setStrategy(String buildId, String code) {
        PricingStrategy s = PricingStrategy.byCode(code);
        PcBuild b = getBuild(buildId).toBuilder().strategy(code).build();
        repo.save(b);
        bus.publish(new BuildEvent("CHANGED", buildId, "'" + b.name() + "': pricing switched to " + s.label()));
        return b;
    }

    public void deleteBuild(String id) {
        PcBuild b = getBuild(id);
        repo.delete(id);
        bus.publish(new BuildEvent("DELETED", id, "Build '" + b.name() + "' deleted"));
    }

    /** Final output: a text report. Only complete, compatible builds can be exported. */
    public String report(String id) {
        Summary s = summary(id);
        PcBuild b = s.build();
        requireReady(s);

        StringBuilder sb = new StringBuilder();
        sb.append("PC BUILD REPORT\n===============\n");
        sb.append("Name: ").append(b.name()).append('\n');
        sb.append("Date: ").append(LocalDate.now()).append("\n\nPrice breakdown:\n");
        for (String line : s.quote().lines()) sb.append("  - ").append(line).append('\n');
        sb.append("\nSubtotal: $").append(PriceItem.money(s.quote().subtotal())).append('\n');
        sb.append("Discount: $").append(PriceItem.money(s.quote().discount())).append('\n');
        sb.append("TOTAL:    $").append(PriceItem.money(s.quote().total())).append('\n');
        bus.publish(new BuildEvent("EXPORTED", id, "Report exported for '" + b.name() + "'"));
        return sb.toString();
    }

    public PcBuild renameBuild(String buildId, String newName) {
        PcBuild old = getBuild(buildId);
        PcBuild b = old.toBuilder().name(newName).build();
        repo.save(b);
        bus.publish(new BuildEvent("CHANGED", buildId, "Build '" + old.name() + "' renamed to '" + b.name() + "'"));
        return b;
    }

    /** Third entity: an Order stores the final price of a complete, compatible build. */
    public Order placeOrder(String buildId) {
        Summary s = summary(buildId);
        requireReady(s);
        Order o = new Order(UUID.randomUUID().toString().substring(0, 8), buildId, s.build().name(),
                s.quote().total(), LocalDate.now(), "PLACED");
        orders.save(o);
        bus.publish(new BuildEvent("ORDERED", buildId, "Order " + o.id() + " placed for '" + o.buildName()
                + "': $" + PriceItem.money(o.total())));
        return o;
    }

    public Order cancelOrder(String orderId) {
        Order o = orders.find(orderId);
        if (o == null) throw new ValidationException("Order not found: " + orderId);
        if (o.status().equals("CANCELLED")) throw new ValidationException("Order is already cancelled");
        Order c = o.cancelled();
        orders.save(c);
        bus.publish(new BuildEvent("CANCELLED", o.buildId(), "Order " + orderId + " for '" + o.buildName() + "' cancelled"));
        return c;
    }

    public List<Order> listOrders() { return orders.findAll(); }

    private void requireReady(Summary s) {
        if (!s.build().isComplete()) throw new ValidationException("Build is incomplete, missing: " + s.build().missing());
        if (!s.issues().isEmpty()) throw new ValidationException("Fix compatibility issues first: " + String.join("; ", s.issues()));
    }

    private void seedSamples() {
        PcBuild.Builder gaming = PcBuild.builder().name("Gaming Rig").strategy("standard")
                .upgrade("warranty", true).upgrade("rgb", true);
        for (String id : List.of("cpu-r7", "mb-b650", "ram-32", "gpu-4070", "ssd-2t", "psu-750"))
            gaming.part(catalog.find(id));
        repo.save(gaming.build());

        PcBuild.Builder broken = PcBuild.builder().name("Mismatch Demo").strategy("student");
        for (String id : List.of("cpu-i5", "mb-b650")) broken.part(catalog.find(id));
        repo.save(broken.build());
    }
}
