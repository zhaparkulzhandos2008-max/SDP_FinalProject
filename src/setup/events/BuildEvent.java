package setup.events;

/** type: CHANGED, CREATED, DELETED, WARNING, EXPORTED. */
public record BuildEvent(String type, String buildId, String message) {}
