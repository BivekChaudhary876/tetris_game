package au.edu.Griffith.model.state;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;

/**
 * Terminal state: the stack reached the top and no further play is possible.
 * Only a replay leaves it.
 */
public class GameOverState implements GameState {

    @Override
    public GameStatus status() {
        return GameStatus.GAME_OVER;
    }

    @Override
    public void tick(GameModel model, double elapsedMs) {
        // Nothing advances once the game is over.
    }

    @Override
    public boolean acceptsInput() {
        return false;
    }
}
