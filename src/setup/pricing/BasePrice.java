package setup.pricing;

import setup.domain.Component;
import setup.domain.PcBuild;
import java.util.List;

/** Concrete component of the Decorator pattern: the plain sum of selected parts. */
public final class BasePrice implements PriceItem {
    private final PcBuild build;
    public BasePrice(PcBuild build) { this.build = build; }

    public double price() {
        double sum = 0;
        for (Component c : build.parts().values()) sum += c.price();
        return sum;
    }

    public void describe(List<String> lines) {
        for (Component c : build.parts().values())
            lines.add(c.type() + " " + c.name() + ": $" + PriceItem.money(c.price()));
    }
}
