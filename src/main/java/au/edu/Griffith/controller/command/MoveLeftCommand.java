package au.edu.Griffith.controller.command;

import au.edu.Griffith.model.Movable;

/**
 * Shifts the active piece one column left.
 *
 * <p>Depends on {@link Movable}, not on {@code GameModel}, so the same command
 * can drive a real game or a test double.</p>
 */
public class MoveLeftCommand implements Command {

    private final Movable target;

    public MoveLeftCommand(Movable target) {
        this.target = target;
    }

    @Override
    public void execute() {
        target.moveLeft();
    }

    @Override
    public void undo() {
        target.moveRight();
    }

    @Override
    public String name() {
        return "MOVE_LEFT";
    }
}
