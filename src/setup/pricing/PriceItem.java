package setup.pricing;

import java.util.List;
import java.util.Locale;

/** Component interface of the Decorator pattern. */
public interface PriceItem {
    double price();
    void describe(List<String> lines);

    static String money(double d) { return String.format(Locale.US, "%.2f", d); }
}
