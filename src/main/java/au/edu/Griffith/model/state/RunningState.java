package au.edu.Griffith.model.state;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;

/** Normal play: gravity applies and input is accepted. */
public class RunningState implements GameState {

    @Override
    public GameStatus status() {
        return GameStatus.RUNNING;
    }

    @Override
    public void tick(GameModel model, double elapsedMs) {
        model.applyGravity(elapsedMs);
    }

    @Override
    public boolean acceptsInput() {
        return true;
    }
}
