package au.edu.Griffith.model.state;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;

/**
 * State pattern: one implementation per phase of a game, each deciding what the
 * model does when the clock ticks and whether input is accepted.
 *
 * <p>The Milestone 1 code carried {@code isPaused} and {@code isGameOver}
 * booleans and re-tested them at the top of the timer callback, the key handler
 * and several other methods — guards that were easy to forget. Replacing them
 * with state objects means the rule "a paused game ignores movement" is enforced
 * in exactly one place: {@link PausedState} simply does not act on it.</p>
 */
public interface GameState {

    /** The status tag this state corresponds to, for display. */
    GameStatus status();

    /**
     * Advances the game by one frame.
     *
     * @param model     the game being driven
     * @param elapsedMs milliseconds since the previous tick
     */
    void tick(GameModel model, double elapsedMs);

    /** Whether player input should reach the board while in this state. */
    boolean acceptsInput();
}
