package au.edu.Griffith.controller.command;

/**
 * Command pattern: one player action, wrapped as an object.
 *
 * <p>In Milestone 1 the key handler was a {@code switch} on {@code KeyCode} that
 * called a movement method inline, which meant only the keyboard could ever
 * drive the game. Turning each action into a {@code Command} gives the AI and
 * the network client the same vocabulary the keyboard has: all three produce
 * commands, and one place executes them.</p>
 *
 * <p>{@link #undo()} is declared now because it is nearly free once actions are
 * objects, and it is what a practice or replay mode would be built on.</p>
 */
public interface Command {

    /** Performs the action. */
    void execute();

    /**
     * Reverses the action.
     *
     * <p>Default is a no-op so the many commands that are not sensibly
     * reversible — a hard drop, for instance — do not have to say so.</p>
     */
    default void undo() {
        // Most commands are not reversible.
    }

    /** Short name for logging and for the replay list. */
    String name();
}
