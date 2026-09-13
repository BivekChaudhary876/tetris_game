package au.edu.Griffith.controller.command;

import au.edu.Griffith.model.Movable;

/** Steps the active piece down one row. */
public class SoftDropCommand implements Command {

    private final Movable target;

    public SoftDropCommand(Movable target) {
        this.target = target;
    }

    @Override
    public void execute() {
        target.softDrop();
    }

    @Override
    public String name() {
        return "SOFT_DROP";
    }
}
