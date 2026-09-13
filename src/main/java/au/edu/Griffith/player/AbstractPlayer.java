package au.edu.Griffith.player;

import au.edu.Griffith.controller.command.Command;
import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.GameModel;

/**
 * Shared plumbing for the three player kinds: holding the model, building a
 * {@link CommandFactory} for it and refusing to act while the game is not
 * accepting input.
 *
 * <p>Exists so that rule — "no player may move a piece in a paused or finished
 * game" — is written once here rather than three times in the subclasses.</p>
 */
public abstract class AbstractPlayer implements Player {

    protected GameModel model;
    protected CommandFactory commands;

    @Override
    public void attach(GameModel model) {
        throw new UnsupportedOperationException("TODO: store the model and build a CommandFactory for it");
    }

    /**
     * Runs a command, but only if the current {@link au.edu.Griffith.model.state.GameState}
     * accepts input.
     */
    protected void submit(Command command) {
        throw new UnsupportedOperationException("TODO: execute the command only when the model's state accepts input");
    }

    @Override
    public void dispose() {
        // Nothing to release by default.
    }
}
