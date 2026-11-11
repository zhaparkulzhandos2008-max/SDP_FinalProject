package setup.pricing;

import setup.domain.PcBuild;
import java.util.ArrayList;
import java.util.List;

/** Combines the Decorator chain (subtotal) with the selected Strategy (discount). */
public final class PriceCalculator {

    public record Quote(List<String> lines, double subtotal, double discount, double total) {}

    public Quote quote(PcBuild build) {
        PriceItem item = Upgrades.decorate(new BasePrice(build), build.upgrades());
        double subtotal = item.price();
        PricingStrategy strategy = PricingStrategy.byCode(build.strategy());
        double discount = strategy.discount(subtotal);

        List<String> lines = new ArrayList<>();
        item.describe(lines);
        if (discount > 0) lines.add(strategy.label() + ": -$" + PriceItem.money(discount));
        return new Quote(lines, subtotal, discount, Math.round((subtotal - discount) * 100) / 100.0);
    }
}
