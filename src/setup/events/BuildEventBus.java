package setup.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PATTERN: Observer (behavioral) - the subject.
 * Data changes are published here; subscribers react without the publisher knowing them.
 */
public final class BuildEventBus {
    private final List<BuildListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(BuildListener l) { listeners.add(l); }
    public void unsubscribe(BuildListener l) { listeners.remove(l); }

    public void publish(BuildEvent e) {
        for (BuildListener l : listeners) l.onEvent(e);
    }
}
