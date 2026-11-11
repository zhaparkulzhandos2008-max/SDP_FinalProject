package setup;

import setup.app.SetupFacade;
import setup.domain.*;
import java.nio.file.*;
import java.util.List;

/** Dependency-free automated tests (no JUnit needed). Exit code 1 if any test fails. */
public final class TestRunner {
    static int failed = 0;

    static void test(String name, Runnable r) {
        try { r.run(); System.out.println("PASS  " + name); }
        catch (Throwable t) { failed++; System.out.println("FAIL  " + name + " -> " + t); }
    }
    static void check(boolean ok, String msg) { if (!ok) throw new AssertionError(msg); }
    static void near(double exp, double act) { check(Math.abs(exp - act) < 0.01, "expected " + exp + " but was " + act); }
    static void rejects(Runnable r) {
        try { r.run(); } catch (ValidationException e) { return; }
        throw new AssertionError("ValidationException expected");
    }

    static SetupFacade fresh() throws Exception {
        return new SetupFacade(Files.createTempDirectory("bys").resolve("builds.txt"));
    }
    static String full(SetupFacade app) {
        String id = app.createBuild("T").id();
        for (String c : List.of("cpu-r5", "mb-b650", "ram-32", "gpu-4060", "ssd-1t", "psu-550")) app.setComponent(id, c);
        return id;
    }

    public static void main(String[] args) throws Exception {
        SetupFacade app = fresh();

        test("1 full compatible build: price with upgrades (Decorator)", () -> {
            String id = full(app);
            check(app.summary(id).quote().total() == 990, "base 990");
            app.setUpgrade(id, "assembly", true); app.setUpgrade(id, "warranty", true);
            near(1144, app.summary(id).quote().total());
            check(app.summary(id).issues().isEmpty() && app.getBuild(id).isComplete(), "complete, no issues");
        });
        test("2 strategy changes the discount (Strategy)", () -> {
            String id = full(app);
            app.setUpgrade(id, "assembly", true); app.setUpgrade(id, "warranty", true);
            app.setStrategy(id, "student");
            near(1029.6, app.summary(id).quote().total());
            app.setStrategy(id, "volume");
            near(1144, app.summary(id).quote().total());   // below $2000: no discount
        });
        test("3 socket mismatch reported + WARNING event (Observer)", () -> {
            String id = app.createBuild("Bad").id();
            app.setComponent(id, "cpu-i5"); app.setComponent(id, "mb-b650");
            check(app.summary(id).issues().size() == 1, "one issue");
            check(app.activity().stream().anyMatch(a -> a.contains("WARNING") && a.contains("socket")), "warning logged");
        });
        test("4 weak power supply detected", () -> {
            String id = app.createBuild("Weak").id();
            app.setComponent(id, "cpu-i5"); app.setComponent(id, "gpu-7800"); app.setComponent(id, "psu-450");
            check(app.summary(id).issues().get(0).contains("too weak"), "psu issue");
        });
        test("5 data survives restart (persistence)", () -> {
            Path f = Path.of(System.getProperty("java.io.tmpdir"), "bys-" + System.nanoTime() + ".txt");
            SetupFacade a = new SetupFacade(f);
            String id = full(a); a.setUpgrade(id, "rgb", true);
            SetupFacade b = new SetupFacade(f);
            check(b.getBuild(id).isComplete() && b.getBuild(id).upgrades().contains("rgb"), "reloaded");
        });
        test("6 invalid names rejected (Builder validation)", () -> {
            rejects(() -> app.createBuild("   "));
            rejects(() -> app.createBuild("a|b"));
            rejects(() -> app.createBuild("x".repeat(41)));
        });
        test("7 unknown ids rejected", () -> {
            String id = app.createBuild("U").id();
            rejects(() -> app.setComponent(id, "nope"));
            rejects(() -> app.setUpgrade(id, "nope", true));
            rejects(() -> app.setStrategy(id, "nope"));
            rejects(() -> app.getBuild("missing"));
        });
        test("8 report only for complete + compatible build (Facade)", () -> {
            String id = app.createBuild("R").id();
            rejects(() -> app.report(id));
            String ok = full(app);
            check(app.report(ok).contains("TOTAL:    $990.00"), "report total");
        });

        test("9 rename build: valid + invalid names", () -> {
            String id = app.createBuild("Old").id();
            check(app.renameBuild(id, "New name").name().equals("New name"), "renamed");
            rejects(() -> app.renameBuild(id, " "));
            rejects(() -> app.renameBuild(id, "a|b"));
        });
        test("10 order lifecycle: reject incomplete, place, persist, cancel", () -> {
            Path f = Path.of(System.getProperty("java.io.tmpdir"), "bys-" + System.nanoTime() + ".txt");
            SetupFacade a = new SetupFacade(f);
            rejects(() -> a.placeOrder(a.createBuild("Empty").id()));
            Order o = a.placeOrder(full(a));
            near(990, o.total());
            check(new SetupFacade(f).listOrders().size() == 1, "order persisted");
            check(a.cancelOrder(o.id()).status().equals("CANCELLED"), "cancelled");
            rejects(() -> a.cancelOrder(o.id()));
        });

        System.out.println(failed == 0 ? "\nALL TESTS PASSED" : "\n" + failed + " TEST(S) FAILED");
        System.exit(failed == 0 ? 1 - 1 : 1);
    }
}
