package setup.pricing;

import java.util.*;

/**
 * PATTERN: Decorator (structural).
 * Each optional upgrade wraps another PriceItem and adds to its price and description.
 * Order matters: warranty is applied last, so it covers every other upgrade too.
 */
public final class Upgrades {
    private Upgrades() {}

    /** code -> label, in the order the decorators are applied. */
    public static final Map<String, String> LABELS = new LinkedHashMap<>();
    static {
        LABELS.put("assembly", "Professional assembly");
        LABELS.put("rgb", "RGB lighting kit");
        LABELS.put("overclock", "Factory overclock tuning");
        LABELS.put("warranty", "Extended warranty (10%)");
    }

    public static PriceItem decorate(PriceItem base, Set<String> codes) {
        PriceItem result = base;
        for (String code : LABELS.keySet()) {
            if (!codes.contains(code)) continue;
            result = switch (code) {
                case "assembly" -> new Assembly(result);
                case "rgb" -> new Rgb(result);
                case "overclock" -> new Overclock(result);
                default -> new Warranty(result);
            };
        }
        return result;
    }

    /** Abstract decorator. */
    abstract static class PriceDecorator implements PriceItem {
        protected final PriceItem inner;
        PriceDecorator(PriceItem inner) { this.inner = inner; }
        protected abstract double extra();
        protected abstract String label();

        public double price() { return inner.price() + extra(); }
        public void describe(List<String> lines) {
            inner.describe(lines);
            lines.add(label() + ": +$" + PriceItem.money(extra()));
        }
    }

    static final class Assembly extends PriceDecorator {
        Assembly(PriceItem i) { super(i); }
        protected double extra() { return 50; }
        protected String label() { return LABELS.get("assembly"); }
    }
    static final class Rgb extends PriceDecorator {
        Rgb(PriceItem i) { super(i); }
        protected double extra() { return 60; }
        protected String label() { return LABELS.get("rgb"); }
    }
    static final class Overclock extends PriceDecorator {
        Overclock(PriceItem i) { super(i); }
        protected double extra() { return 40; }
        protected String label() { return LABELS.get("overclock"); }
    }
    static final class Warranty extends PriceDecorator {
        Warranty(PriceItem i) { super(i); }
        protected double extra() { return Math.round(inner.price() * 10) / 100.0; }
        protected String label() { return LABELS.get("warranty"); }
    }
}
