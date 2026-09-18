package au.edu.Griffith.player;

import au.edu.Griffith.ai.Move;
import au.edu.Griffith.ai.TetrisAI;
import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.Position;
import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoType;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Plays a field on its own, using {@link TetrisAI} to choose each placement.
 *
 * <p>Two phases. When a piece spawns we copy the board and piece <b>on the
 * JavaFX thread</b> and send the copy to a background thread to be scored; once
 * a {@link Move} comes back, {@link #update} steers the real piece toward it.
 * Copying first matters: the game mutates its piece and board on the JavaFX
 * thread, so reading them from the planner thread throws, and the executor hides
 * the exception — the AI just stops moving for the rest of the game.</p>
 *
 * <p>Each field owns its own AIPlayer, so "AI vs AI" in Extend Mode is two of
 * these running side by side with no extra work.</p>
 *
 * <p><b>Rotations are verified, not counted.</b> The game turns a piece about
 * its index-1 cell, which pushes one cell up a row; at spawn that lands on
 * row -1, which {@code Board.canPlace} rejects, and the wall kick only shifts
 * columns so it cannot help. So <i>no piece can rotate on its spawn row</i> (S
 * needs two rows). Firing a fixed number of ROTATE commands means they are all
 * refused and the piece lands flat. Instead we remember the orientation we want
 * and keep rotating until the piece matches it, dropping a row for clearance
 * whenever a turn is refused.</p>
 */
public class AIPlayer extends AbstractPlayer {

    /** Gap between commands. Short, or the piece is still moving when it lands. */
    private static final long MOVE_INTERVAL_MS = 25;

    /** Rows pushed down per tick once lined up. The model ignores extras once it rests. */
    private static final int DROP_STEPS_PER_TICK = 3;

    /** Give up rotating after this many refusals and place the piece as it is. */
    private static final int MAX_ROTATE_ATTEMPTS = 40;

    private final TetrisAI ai = new TetrisAI();

    /** Search runs here so the frame rate never depends on how long it takes. */
    private final ExecutorService planner = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ai-planner");
        thread.setDaemon(true);
        return thread;
    });

    /** Piece the current plan belongs to. Used to spot when a new piece spawns. */
    private AbstractTetromino planningForPiece;

    /** Orientation the plan wants, as a shape signature. */
    private volatile String targetOrientation;

    /** Set by the planner thread when a plan is ready. */
    private volatile Move currentTarget;

    /** Set when the search found nothing, so update() stops waiting. */
    private volatile boolean planFailed;

    private int rotateAttempts;
    private double sinceLastMove;

    @Override
    public PlayerType getType() {
        return PlayerType.AI;
    }

    @Override
    public void update(double elapsedMs) {
        if (model == null || model.getActivePiece() == null) {
            return;
        }

        AbstractTetromino activePiece = model.getActivePiece();
        if (activePiece != planningForPiece) {
            planningForPiece = activePiece;
            currentTarget = null;
            targetOrientation = null;
            planFailed = false;
            rotateAttempts = 0;
            planMoves();
        }

        sinceLastMove += elapsedMs;
        if (sinceLastMove < MOVE_INTERVAL_MS) {
            return;
        }
        sinceLastMove = 0;

        Move target = currentTarget;
        if (target == null) {
            // No plan yet. If the search came back empty the board is full, so
            // drop rather than freeze; otherwise it is still running, so wait.
            if (planFailed) {
                drop();
            }
            return;
        }

        if (needsRotation() && rotateAttempts < MAX_ROTATE_ATTEMPTS) {
            attemptRotation();
            return;
        }

        int currentCol = leftmostColumn(model.getActivePiece());
        if (currentCol < target.column()) {
            submit(commands.create(CommandFactory.Action.MOVE_RIGHT));
        } else if (currentCol > target.column()) {
            submit(commands.create(CommandFactory.Action.MOVE_LEFT));
        } else {
            drop();
        }
    }

    /** True while the piece is not yet in the orientation the plan chose. */
    private boolean needsRotation() {
        String wanted = targetOrientation;
        return wanted != null
                && !wanted.equals(TetrisAI.orientationSignature(model.getActivePiece().getCells()));
    }

    /**
     * Rotates once, then checks whether the shape actually changed. A refusal near
     * the top is normal, so we drop one row to make space and retry next tick.
     */
    private void attemptRotation() {
        rotateAttempts++;

        String before = TetrisAI.orientationSignature(model.getActivePiece().getCells());
        submit(commands.create(CommandFactory.Action.ROTATE));
        String after = TetrisAI.orientationSignature(model.getActivePiece().getCells());

        if (before.equals(after)) {
            submit(commands.create(CommandFactory.Action.SOFT_DROP));
        }
    }

    private void drop() {
        for (int step = 0; step < DROP_STEPS_PER_TICK; step++) {
            submit(commands.create(CommandFactory.Action.SOFT_DROP));
        }
    }

    /** Copies the game state here (JavaFX thread), then scores it on the planner thread. */
    protected void planMoves() {
        AbstractTetromino piece = planningForPiece;

        boolean[][] grid = TetrisAI.toGrid(model.getBoard());
        List<Position> pieceCells = List.copyOf(piece.getCells());

        TetrominoType nextType = model.getNextType();
        List<Position> nextCells = nextType != null ? TetrisAI.spawnCells(nextType) : null;

        planner.submit(() -> {
            try {
                Move best = ai.findBestMove(grid, pieceCells, nextCells);

                if (piece != planningForPiece) {
                    return; // a new piece spawned while we were thinking
                }
                if (best == null) {
                    planFailed = true;
                    return;
                }

                List<List<Position>> orientations = ai.distinctRotations(pieceCells);
                targetOrientation = TetrisAI.orientationSignature(orientations.get(best.rotations()));
                currentTarget = best; // written last so it publishes the orientation too
            } catch (RuntimeException e) {
                // Without this the executor swallows the error and the AI goes
                // quiet with no clue why.
                System.err.println("AI planning failed: " + e);
                e.printStackTrace();
                planFailed = true;
            }
        });
    }

    /** Column of the piece's left edge — what {@link Move#column()} targets. */
    private static int leftmostColumn(AbstractTetromino piece) {
        int min = Integer.MAX_VALUE;
        for (Position cell : piece.getCells()) {
            min = Math.min(min, cell.col());
        }
        return min;
    }

    @Override
    public void dispose() {
        planner.shutdownNow();
    }
}