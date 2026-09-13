package au.edu.Griffith.player;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.PlayerType;

import java.util.Deque;

/**
 * A player that plans its own moves.
 *
 * <p>Works in two phases so the UI never stutters. When a piece spawns, the
 * planner scores every reachable (column, rotation) placement on a background
 * thread and produces a queue of actions; {@link #update} then feeds that queue
 * to the model one action per frame, on the JavaFX thread. Keeping the search
 * off the render thread is the "Threads" criterion, and doing it this way is why
 * the AI can be given a deeper search later without the frame rate suffering.</p>
 */
public class AIPlayer extends AbstractPlayer {

    /** How long to wait between issuing queued moves, so play is watchable. */
    private static final long MOVE_INTERVAL_MS = 60;

    private Deque<CommandFactory.Action> plannedMoves;
    private double sinceLastMove;

    @Override
    public PlayerType getType() {
        return PlayerType.AI;
    }

    @Override
    public void update(double elapsedMs) {
        throw new UnsupportedOperationException(
                "TODO: plan if the queue is empty, then pop one action every MOVE_INTERVAL_MS");
    }

    /**
     * Scores every legal placement for the active piece and returns the moves
     * that reach the best one.
     *
     * <p>Runs off the JavaFX thread.</p>
     */
    protected Deque<CommandFactory.Action> planMoves() {
        throw new UnsupportedOperationException(
                "TODO: for each rotation and column, simulate the drop and score it "
                        + "on aggregate height, holes, bumpiness and lines cleared; return moves to the best");
    }

    /** Heuristic score for a candidate board; higher is better. */
    protected double evaluate(int[] columnHeights, int holes, int linesCleared) {
        throw new UnsupportedOperationException("TODO: weighted sum of the heuristics");
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("TODO: shut down the planning executor");
    }
}
