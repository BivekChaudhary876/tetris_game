package au.edu.Griffith.player;

import au.edu.Griffith.ai.TetrisAI;
import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.Position;
import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.network.ExternalPlayerClient;
import au.edu.Griffith.network.OpMove;
import au.edu.Griffith.network.PureGame;
import au.edu.Griffith.network.PureGameFactory;

import java.util.List;

/**
 * A player whose moves come from {@code TetrisServer.jar} over a socket.
 *
 * <p>The connection lives in {@link ExternalPlayerClient}; this class only turns
 * the returned {@link OpMove} into commands. That split keeps socket and
 * threading concerns in the {@code network} package, and lets this class be
 * tested against a mocked client — the Mockito example the marking criteria
 * asks for, and why the client arrives through the constructor rather than
 * being created here.</p>
 *
 * <p><b>Server missing is an ordinary state, not an error.</b> While the client
 * reports unreachable this issues no commands at all, so the field keeps falling
 * under gravity and the screen shows a warning. Nothing is cached: the flag is
 * re-read every frame, so the moment the server appears the current piece asks
 * for a move and control resumes — no restart, no waiting for the next piece.</p>
 *
 * <p><b>Requests are retried.</b> The client only calls back on success, so a
 * request that fails mid-flight would otherwise leave the piece waiting forever.
 * {@link #REQUEST_RETRY_MS} bounds that: if no reply has arrived by then, ask
 * again.</p>
 *
 * <p>Steering reuses {@link AIPlayer}'s approach: rotate until the piece's shape
 * actually matches, then walk it to the target column. The model refuses a
 * rotation that would push a cell above the field — which is every piece on its
 * spawn row — so counting rotations instead of verifying them leaves pieces
 * unrotated.</p>
 */
public class ExternalPlayer extends AbstractPlayer {

    /** Gap between commands. Short, or the piece lands before it is in position. */
    private static final long MOVE_INTERVAL_MS = 25;

    /** How long to wait for a reply before asking again. */
    private static final double REQUEST_RETRY_MS = 500;

    /** Rows pushed down per tick once lined up. The model ignores extras once it rests. */
    private static final int DROP_STEPS_PER_TICK = 3;

    /** Give up rotating after this many refusals and place the piece as it is. */
    private static final int MAX_ROTATE_ATTEMPTS = 40;

    private final ExternalPlayerClient client;

    /** Piece the current request belongs to. Used to spot when a new piece spawns. */
    private AbstractTetromino requestingForPiece;

    /** Published by the client's IO thread when the server replies. */
    private volatile OpMove currentTarget;

    /** Shape signature the reply's rotation count corresponds to. */
    private volatile String targetOrientation;

    private double sinceRequest;
    private boolean requestSent;
    private int rotateAttempts;
    private double sinceLastMove;

    public ExternalPlayer(ExternalPlayerClient client) {
        this.client = client;
    }

    @Override
    public void attach(GameModel model) {
        super.attach(model);
        client.connectAsync();
    }

    @Override
    public PlayerType getType() {
        return PlayerType.EXTERNAL;
    }

    @Override
    public void update(double elapsedMs) {
        if (model == null || model.getActivePiece() == null) {
            return;
        }

        AbstractTetromino activePiece = model.getActivePiece();
        if (activePiece != requestingForPiece) {
            requestingForPiece = activePiece;
            requestSent = false;
            sinceRequest = 0;
            currentTarget = null;
            targetOrientation = null;
            rotateAttempts = 0;
        }

        // Re-checked every frame, never remembered: this is what lets control
        // resume the instant the server is started mid-game.
        if (!client.isConnected()) {
            return;
        }

        OpMove target = currentTarget;

        if (target == null) {
            sinceRequest += elapsedMs;

            if (!requestSent || sinceRequest >= REQUEST_RETRY_MS) {
                requestSent = true;
                sinceRequest = 0;
                requestMove();
            }
            return; // waiting on the server
        }

        sinceLastMove += elapsedMs;
        if (sinceLastMove < MOVE_INTERVAL_MS) {
            return;
        }
        sinceLastMove = 0;

        if (needsRotation() && rotateAttempts < MAX_ROTATE_ATTEMPTS) {
            attemptRotation();
            return;
        }

        int currentCol = leftmostColumn(model.getActivePiece());
        if (currentCol < target.opX()) {
            submit(commands.create(CommandFactory.Action.MOVE_RIGHT));
        } else if (currentCol > target.opX()) {
            submit(commands.create(CommandFactory.Action.MOVE_LEFT));
        } else {
            drop();
        }
    }

    /**
     * Sends the board state, and works out which shape the server's rotation
     * count refers to while still on the JavaFX thread.
     */
    private void requestMove() {
        AbstractTetromino piece = requestingForPiece;

        // Copied here, on the JavaFX thread, before anything crosses to the IO
        // thread: the game mutates its piece and board as it runs.
        List<Position> pieceCells = List.copyOf(piece.getCells());
        PureGame snapshot = PureGameFactory.from(model);

        client.requestMoveAsync(snapshot, move -> {
            if (piece != requestingForPiece) {
                return; // a new piece spawned while the server was thinking
            }

            List<List<Position>> orientations = new TetrisAI().distinctRotations(pieceCells);

            // floorMod, because the server counts rotations for its own shape
            // table and may send more turns than this piece has distinct ones.
            int index = Math.floorMod(move.opRotate(), orientations.size());

            targetOrientation = TetrisAI.orientationSignature(orientations.get(index));
            currentTarget = move; // written last so it publishes the orientation too
        });
    }

    /** True while the piece is not yet in the orientation the server asked for. */
    private boolean needsRotation() {
        String wanted = targetOrientation;
        return wanted != null
                && !wanted.equals(TetrisAI.orientationSignature(model.getActivePiece().getCells()));
    }

    /**
     * Rotates once, then checks whether the shape actually changed. A refusal at
     * the top of the field is normal, so we drop a row for clearance and retry.
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

    /** True while the server is reachable; drives the on-screen warning. */
    public boolean isConnected() {
        return client.isConnected();
    }

    private static int leftmostColumn(AbstractTetromino piece) {
        int min = Integer.MAX_VALUE;
        for (Position cell : piece.getCells()) {
            min = Math.min(min, cell.col());
        }
        return min;
    }

    @Override
    public void dispose() {
        client.disconnect();
    }
}