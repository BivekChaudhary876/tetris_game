package au.edu.Griffith.player;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.PlayerType;

/**
 * A player driven by the keyboard.
 *
 * <p>Does nothing on {@link #update}: its moves arrive from JavaFX key events,
 * which the {@link au.edu.Griffith.controller.InputHandler} translates into
 * actions and pushes here. An empty {@code update} is the honest implementation,
 * not a stub.</p>
 */
public class HumanPlayer extends AbstractPlayer {

    @Override
    public PlayerType getType() {
        return PlayerType.HUMAN;
    }

    @Override
    public void update(double elapsedMs) {
        // Human moves are event-driven, so there is nothing to do per frame.
    }

    /**
     * Entry point for a key press that has already been mapped to an action.
     *
     * @param action what the key means
     */
    public void onAction(CommandFactory.Action action) {
        submit(commands.create(action));
    }
}
