package setup.events;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Observer #1: keeps the latest notifications shown in the UI. */
public final class ActivityLog implements BuildListener {
    private static final int MAX = 30;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final LinkedList<String> entries = new LinkedList<>();

    public synchronized void onEvent(BuildEvent e) {
        entries.addFirst("[" + LocalTime.now().format(FMT) + "] " + e.type() + ": " + e.message());
        while (entries.size() > MAX) entries.removeLast();
    }

    public synchronized List<String> latest() { return new ArrayList<>(entries); }
}
