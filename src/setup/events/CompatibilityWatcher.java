package setup.events;

import setup.domain.CompatibilityChecker;
import setup.domain.PcBuild;
import java.util.function.Function;

/**
 * Observer #2: reacts to every CHANGED event by re-checking compatibility and
 * publishing a WARNING event for each problem found.
 */
public final class CompatibilityWatcher implements BuildListener {
    private final Function<String, PcBuild> lookup;
    private final CompatibilityChecker checker;
    private final BuildEventBus bus;

    public CompatibilityWatcher(Function<String, PcBuild> lookup, CompatibilityChecker checker, BuildEventBus bus) {
        this.lookup = lookup; this.checker = checker; this.bus = bus;
    }

    public void onEvent(BuildEvent e) {
        if (!e.type().equals("CHANGED")) return;
        PcBuild b = lookup.apply(e.buildId());
        if (b == null) return;
        for (String issue : checker.check(b))
            bus.publish(new BuildEvent("WARNING", b.id(), "'" + b.name() + "': " + issue));
    }
}
