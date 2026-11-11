package setup.domain;

/**
 * PATTERN: Factory Method (creational).
 * create() is the common algorithm (validation); make() is the factory method
 * that each concrete factory overrides to produce its own Component subclass.
 */
public abstract class ComponentFactory {

    public final Component create(String id, String name, double price, int power, String socket) {
        if (id == null || id.isBlank() || name == null || name.isBlank())
            throw new ValidationException("Component id and name are required");
        if (price < 0 || power < 0)
            throw new ValidationException("Price and power must not be negative");
        return make(id, name, price, power, socket == null ? "" : socket);
    }

    /** The factory method. */
    protected abstract Component make(String id, String name, double price, int power, String socket);

    public static ComponentFactory of(ComponentType type) {
        return switch (type) {
            case CPU -> new CpuFactory();
            case MOTHERBOARD -> new MotherboardFactory();
            case RAM -> new RamFactory();
            case GPU -> new GpuFactory();
            case STORAGE -> new StorageFactory();
            case PSU -> new PsuFactory();
        };
    }

    static final class CpuFactory extends ComponentFactory {
        protected Component make(String i, String n, double p, int w, String s) { return new Component.Cpu(i, n, p, w, s); }
    }
    static final class MotherboardFactory extends ComponentFactory {
        protected Component make(String i, String n, double p, int w, String s) { return new Component.Motherboard(i, n, p, w, s); }
    }
    static final class RamFactory extends ComponentFactory {
        protected Component make(String i, String n, double p, int w, String s) { return new Component.Ram(i, n, p, w, s); }
    }
    static final class GpuFactory extends ComponentFactory {
        protected Component make(String i, String n, double p, int w, String s) { return new Component.Gpu(i, n, p, w, s); }
    }
    static final class StorageFactory extends ComponentFactory {
        protected Component make(String i, String n, double p, int w, String s) { return new Component.Storage(i, n, p, w, s); }
    }
    static final class PsuFactory extends ComponentFactory {
        protected Component make(String i, String n, double p, int w, String s) { return new Component.Psu(i, n, p, w, s); }
    }
}
