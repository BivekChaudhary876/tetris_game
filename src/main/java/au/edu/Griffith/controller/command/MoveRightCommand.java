package au.edu.Griffith.controller.command;

import au.edu.Griffith.model.Movable;

/** Shifts the active piece one column right. */
public class MoveRightCommand implements Command {

    private final Movable target;

    public MoveRightCommand(Movable target) {
        this.target = target;
    }

    @Override
    public void execute() {
        target.moveRight();
    }

    @Override
    public void undo() {
        target.moveLeft();
    }

    @Override
    public String name() {
        return "MOVE_RIGHT";
    }
}
