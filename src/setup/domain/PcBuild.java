package setup.domain;

import java.util.*;

/**
 * An immutable PC configuration.
 * PATTERN: Builder (creational) - the nested Builder assembles a PcBuild step by step
 * and validates it in build(). Edits go through toBuilder(), so a PcBuild never changes.
 */
public final class PcBuild {
    private final String id, name, strategy;
    private final Map<ComponentType, Component> parts;
    private final Set<String> upgrades;

    private PcBuild(Builder b) {
        id = b.id;
        name = b.name.trim();
        strategy = b.strategy;
        parts = Collections.unmodifiableMap(new EnumMap<>(b.parts));
        upgrades = Collections.unmodifiableSet(new TreeSet<>(b.upgrades));
    }

    public String id() { return id; }
    public String name() { return name; }
    public String strategy() { return strategy; }
    public Map<ComponentType, Component> parts() { return parts; }
    public Component part(ComponentType t) { return parts.get(t); }
    public Set<String> upgrades() { return upgrades; }
    public boolean isComplete() { return parts.size() == ComponentType.values().length; }

    public List<ComponentType> missing() {
        List<ComponentType> m = new ArrayList<>();
        for (ComponentType t : ComponentType.values()) if (!parts.containsKey(t)) m.add(t);
        return m;
    }

    public static Builder builder() { return new Builder(); }
    public Builder toBuilder() { return new Builder(this); }

    public static final class Builder {
        private String id = UUID.randomUUID().toString().substring(0, 8);
        private String name = "", strategy = "standard";
        private final Map<ComponentType, Component> parts = new EnumMap<>(ComponentType.class);
        private final Set<String> upgrades = new TreeSet<>();

        private Builder() {}
        private Builder(PcBuild b) {
            id = b.id; name = b.name; strategy = b.strategy;
            parts.putAll(b.parts); upgrades.addAll(b.upgrades);
        }

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name == null ? "" : name; return this; }
        public Builder strategy(String code) { this.strategy = code; return this; }
        public Builder part(Component c) { parts.put(c.type(), c); return this; }
        public Builder upgrade(String code, boolean on) {
            if (on) upgrades.add(code); else upgrades.remove(code);
            return this;
        }

        public PcBuild build() {
            if (name.isBlank()) throw new ValidationException("Build name must not be empty");
            if (name.trim().length() > 40) throw new ValidationException("Build name must be at most 40 characters");
            if (name.contains("|") || name.contains("\n") || name.contains("\r"))
                throw new ValidationException("Build name contains forbidden characters");
            return new PcBuild(this);
        }
    }
}
