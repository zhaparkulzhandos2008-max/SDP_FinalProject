package setup.events;

/** Observer interface. */
public interface BuildListener {
    void onEvent(BuildEvent event);
}
