package au.edu.Griffith.model.observer;

/**
 * An immutable notification pushed from the model to every registered
 * {@link GameObserver}.
 *
 * @param type     what happened
 * @param source   the model that raised it, so a two-player view can tell the
 *                 two fields apart
 * @param payload  optional extra detail (for example the number of lines
 *                 cleared); {@code null} when the type alone says enough
 */
public record GameEvent(GameEventType type, Object source, Object payload) {

    /** Convenience factory for events that carry no payload. */
    public static GameEvent of(GameEventType type, Object source) {
        return new GameEvent(type, source, null);
    }
}
