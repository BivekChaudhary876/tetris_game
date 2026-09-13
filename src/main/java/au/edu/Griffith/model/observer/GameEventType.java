package au.edu.Griffith.model.observer;

/**
 * The kinds of change the model can announce to its observers.
 *
 * <p>Having a closed enum of event types means a view can {@code switch} over
 * them exhaustively, and adding a new event forces every observer to be
 * revisited at compile time.</p>
 */
public enum GameEventType {

    /** A new piece entered the field. */
    PIECE_SPAWNED,

    /** The active piece changed position or rotation. */
    PIECE_MOVED,

    /** The active piece came to rest and became part of the board. */
    PIECE_LOCKED,

    /** One or more rows were completed and removed. */
    LINES_CLEARED,

    /** The score changed. */
    SCORE_CHANGED,

    /** The game moved between RUNNING / PAUSED / GAME_OVER. */
    STATUS_CHANGED
}
