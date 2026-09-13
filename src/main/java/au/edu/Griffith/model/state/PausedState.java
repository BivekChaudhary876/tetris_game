package au.edu.Griffith.model.state;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;

/**
 * Paused with {@code P}: the clock is frozen and movement is ignored.
 *
 * <p>Note there is no {@code if (isPaused) return;} anywhere — {@link #tick}
 * doing nothing and {@link #acceptsInput()} returning {@code false} <em>is</em>
 * the pause behaviour.</p>
 */
public class PausedState implements GameState {

    @Override
    public GameStatus status() {
        return GameStatus.PAUSED;
    }

    @Override
    public void tick(GameModel model, double elapsedMs) {
        // A paused game does not advance.
    }

    @Override
    public boolean acceptsInput() {
        return false;
    }
}
