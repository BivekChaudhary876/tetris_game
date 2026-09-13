package au.edu.Griffith.model;

/**
 * The movement operations that can be performed on a falling piece.
 *
 * <p>Carried over from Milestone 1, but moved into the model layer and now
 * implemented by {@link GameModel} rather than by a JavaFX class. That matters:
 * the {@link au.edu.Griffith.controller.command Command} objects are written
 * against this interface, so none of them depends on how the game is drawn, and
 * a test can drive a game with no UI at all.</p>
 *
 * <p>These four are exactly the moves Milestone 1 supported — there is no hard
 * drop.</p>
 *
 * <p>Each method is a request, not a guarantee: an illegal move is silently
 * rejected by the board.</p>
 */
public interface Movable {

    /** Shifts the active piece one column left, if the board allows it. */
    void moveLeft();

    /** Shifts the active piece one column right, if the board allows it. */
    void moveRight();

    /** Steps the active piece down one row, if the board allows it. */
    void softDrop();

    /** Rotates the active piece 90 degrees clockwise, applying a wall kick if needed. */
    void rotate();
}
