package setup.storage;

import setup.domain.Order;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Orders in a text file, one per line: id|buildId|buildName|total|date|status */
public final class OrderRepository {
    private final Path file;
    private final Map<String, Order> orders = new LinkedHashMap<>();

    public OrderRepository(Path file) {
        this.file = file;
        load();
    }

    public synchronized List<Order> findAll() { return new ArrayList<>(orders.values()); }
    public synchronized Order find(String id) { return orders.get(id); }
    public synchronized void save(Order o) { orders.put(o.id(), o); flush(); }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] f = line.split("\\|", -1);
                if (f.length < 6) continue;
                try {
                    orders.put(f[0], new Order(f[0], f[1], f[2], Double.parseDouble(f[3]), LocalDate.parse(f[4]), f[5]));
                } catch (RuntimeException ignored) { /* skip corrupted line */ }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    private void flush() {
        List<String> lines = new ArrayList<>();
        for (Order o : orders.values())
            lines.add(String.join("|", o.id(), o.buildId(), o.buildName(), String.valueOf(o.total()), o.date().toString(), o.status()));
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
