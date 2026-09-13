package au.edu.Griffith.controller.command;

import au.edu.Griffith.model.Movable;

/**
 * Builds the {@link Command} objects for a given {@link Movable} target.
 *
 * <p>A second Factory alongside {@link au.edu.Griffith.model.tetromino.TetrominoFactory},
 * and the reason the keyboard handler can be written without importing a single
 * concrete command class.</p>
 */
public class CommandFactory {

    /** The actions a player can request. These are Milestone 1's four controls. */
    public enum Action {
        MOVE_LEFT,
        MOVE_RIGHT,
        ROTATE,
        SOFT_DROP
    }

    private final Movable target;

    public CommandFactory(Movable target) {
        this.target = target;
    }

    /**
     * Creates the command for an action.
     *
     * @param action what the player asked for
     * @return a ready-to-execute command
     */
    public Command create(Action action) {
        return switch (action) {
            case MOVE_LEFT -> new MoveLeftCommand(target);
            case MOVE_RIGHT -> new MoveRightCommand(target);
            case ROTATE -> new RotateCommand(target);
            case SOFT_DROP -> new SoftDropCommand(target);
        };
    }
}
