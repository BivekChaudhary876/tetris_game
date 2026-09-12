package au.edu.Griffith.model.tetromino;

/**
 * Supplies the sequence of shapes a game will be dealt.
 *
 * <p>An interface rather than a method on the model because two-player mode
 * requires both fields to receive <em>the same</em> sequence: the two
 * {@code GameModel}s share one generator instance. It also lets a test inject a
 * fixed, predictable sequence — the Stub the marking criteria asks for — instead
 * of fighting a random source.</p>
 */
public interface TetrominoGenerator {

    /** Returns the next shape in the sequence, advancing it. */
    TetrominoType next();

    /** Returns the shape after {@link #next()} without advancing, for the preview panel. */
    TetrominoType peek();

    /** Restarts the sequence from the beginning, for a replay. */
    void reset();
}
