package setup.storage;

import setup.domain.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;

/**
 * Persistence in a plain text file, one build per line:
 * id|name|strategy|upgrade1,upgrade2|CPU=cpu-r5;GPU=gpu-4060
 */
public final class BuildRepository {
    private final Path file;
    private final Catalog catalog;
    private final Map<String, PcBuild> builds = new LinkedHashMap<>();

    public BuildRepository(Path file, Catalog catalog) {
        this.file = file; this.catalog = catalog;
        load();
    }

    public synchronized List<PcBuild> findAll() { return new ArrayList<>(builds.values()); }
    public synchronized PcBuild find(String id) { return builds.get(id); }
    public synchronized void save(PcBuild b) { builds.put(b.id(), b); flush(); }
    public synchronized void delete(String id) { builds.remove(id); flush(); }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) parse(line);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void parse(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length < 5) return;
        try {
            PcBuild.Builder b = PcBuild.builder().id(f[0]).name(f[1]).strategy(f[2]);
            for (String u : f[3].split(",")) if (!u.isBlank()) b.upgrade(u, true);
            for (String p : f[4].split(";")) {
                String[] kv = p.split("=", 2);
                Component c = kv.length == 2 ? catalog.find(kv[1]) : null;
                if (c != null) b.part(c);
            }
            builds.put(f[0], b.build());
        } catch (ValidationException ignored) {
            // skip a corrupted line instead of failing the whole startup
        }
    }

    private void flush() {
        List<String> lines = new ArrayList<>();
        for (PcBuild b : builds.values()) {
            StringBuilder parts = new StringBuilder();
            for (Component c : b.parts().values()) {
                if (parts.length() > 0) parts.append(';');
                parts.append(c.type()).append('=').append(c.id());
            }
            lines.add(String.join("|", b.id(), b.name(), b.strategy(), String.join(",", b.upgrades()), parts));
        }
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
