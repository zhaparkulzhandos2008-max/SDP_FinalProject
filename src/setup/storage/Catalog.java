package setup.storage;

import setup.domain.*;
import java.util.*;

/** Sample parts catalog (read-only). Components are created through the Factory Method. */
public final class Catalog {
    private final Map<String, Component> items = new LinkedHashMap<>();

    public Catalog() {
        add(ComponentType.CPU, "cpu-r5", "AMD Ryzen 5 7600", 229, 65, "AM5");
        add(ComponentType.CPU, "cpu-r7", "AMD Ryzen 7 7800X3D", 399, 120, "AM5");
        add(ComponentType.CPU, "cpu-i5", "Intel Core i5-13600K", 289, 125, "LGA1700");
        add(ComponentType.MOTHERBOARD, "mb-b650", "ASUS TUF B650-Plus", 199, 0, "AM5");
        add(ComponentType.MOTHERBOARD, "mb-z790", "MSI MAG Z790 Tomahawk", 249, 0, "LGA1700");
        add(ComponentType.RAM, "ram-16", "Corsair Vengeance 16GB DDR5", 64, 0, "");
        add(ComponentType.RAM, "ram-32", "G.Skill Trident 32GB DDR5-6000", 109, 0, "");
        add(ComponentType.GPU, "gpu-4060", "NVIDIA GeForce RTX 4060", 299, 115, "");
        add(ComponentType.GPU, "gpu-4070", "NVIDIA GeForce RTX 4070 Super", 599, 220, "");
        add(ComponentType.GPU, "gpu-7800", "AMD Radeon RX 7800 XT", 489, 263, "");
        add(ComponentType.STORAGE, "ssd-500", "WD Blue SN580 500GB", 45, 0, "");
        add(ComponentType.STORAGE, "ssd-1t", "Samsung 980 Pro 1TB", 89, 0, "");
        add(ComponentType.STORAGE, "ssd-2t", "Crucial T500 2TB", 149, 0, "");
        add(ComponentType.PSU, "psu-450", "Generic Budget 450W", 49, 450, "");
        add(ComponentType.PSU, "psu-550", "Corsair CV550 550W", 65, 550, "");
        add(ComponentType.PSU, "psu-750", "Seasonic Focus 750W", 119, 750, "");
        add(ComponentType.PSU, "psu-1000", "be quiet! Straight Power 1000W", 189, 1000, "");
    }

    private void add(ComponentType t, String id, String name, double price, int power, String socket) {
        items.put(id, ComponentFactory.of(t).create(id, name, price, power, socket));
    }

    public List<Component> all() { return new ArrayList<>(items.values()); }
    public Component find(String id) { return items.get(id); }
}
