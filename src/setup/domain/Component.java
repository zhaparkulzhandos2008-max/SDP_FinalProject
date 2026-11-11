package setup.domain;

/** A catalog part. 'power' = draw in watts (CPU/GPU) or capacity in watts (PSU). */
public abstract class Component {
    private final String id, name, socket;
    private final double price;
    private final int power;

    protected Component(String id, String name, double price, int power, String socket) {
        this.id = id; this.name = name; this.price = price; this.power = power; this.socket = socket;
    }

    public String id() { return id; }
    public String name() { return name; }
    public double price() { return price; }
    public int power() { return power; }
    public String socket() { return socket; }
    public abstract ComponentType type();

    public static final class Cpu extends Component {
        Cpu(String i, String n, double p, int w, String s) { super(i, n, p, w, s); }
        public ComponentType type() { return ComponentType.CPU; }
    }
    public static final class Motherboard extends Component {
        Motherboard(String i, String n, double p, int w, String s) { super(i, n, p, w, s); }
        public ComponentType type() { return ComponentType.MOTHERBOARD; }
    }
    public static final class Ram extends Component {
        Ram(String i, String n, double p, int w, String s) { super(i, n, p, w, s); }
        public ComponentType type() { return ComponentType.RAM; }
    }
    public static final class Gpu extends Component {
        Gpu(String i, String n, double p, int w, String s) { super(i, n, p, w, s); }
        public ComponentType type() { return ComponentType.GPU; }
    }
    public static final class Storage extends Component {
        Storage(String i, String n, double p, int w, String s) { super(i, n, p, w, s); }
        public ComponentType type() { return ComponentType.STORAGE; }
    }
    public static final class Psu extends Component {
        Psu(String i, String n, double p, int w, String s) { super(i, n, p, w, s); }
        public ComponentType type() { return ComponentType.PSU; }
    }
}
