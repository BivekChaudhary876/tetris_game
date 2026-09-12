package au.edu.Griffith.controller.command;

import au.edu.Griffith.model.Movable;

/** Rotates the active piece 90 degrees clockwise. */
public class RotateCommand implements Command {

    private final Movable target;

    public RotateCommand(Movable target) {
        this.target = target;
    }

    @Override
    public void execute() {
        target.rotate();
    }

    @Override
    public String name() {
        return "ROTATE";
    }
}
