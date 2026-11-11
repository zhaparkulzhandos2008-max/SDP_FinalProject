package setup.pricing;

import setup.domain.ValidationException;
import java.util.List;

/**
 * PATTERN: Strategy (behavioral).
 * The user can switch how the discount is calculated; PriceCalculator does not
 * know which algorithm it uses.
 */
public interface PricingStrategy {
    String code();
    String label();
    double discount(double subtotal);

    static List<PricingStrategy> all() { return List.of(new Standard(), new Student(), new Volume()); }

    static PricingStrategy byCode(String code) {
        for (PricingStrategy s : all()) if (s.code().equals(code)) return s;
        throw new ValidationException("Unknown pricing strategy: " + code);
    }

    final class Standard implements PricingStrategy {
        public String code() { return "standard"; }
        public String label() { return "Standard price"; }
        public double discount(double subtotal) { return 0; }
    }

    final class Student implements PricingStrategy {
        public String code() { return "student"; }
        public String label() { return "Student discount (10%)"; }
        public double discount(double subtotal) { return Math.round(subtotal * 10) / 100.0; }
    }

    final class Volume implements PricingStrategy {
        public String code() { return "volume"; }
        public String label() { return "Volume discount (12% from $2000)"; }
        public double discount(double subtotal) { return subtotal >= 2000 ? Math.round(subtotal * 12) / 100.0 : 0; }
    }
}
