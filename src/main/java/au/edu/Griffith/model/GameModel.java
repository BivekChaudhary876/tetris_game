package au.edu.Griffith.model;

import au.edu.Griffith.model.observer.GameEvent;
import au.edu.Griffith.model.observer.GameEventType;
import au.edu.Griffith.model.observer.Observable;
import au.edu.Griffith.model.state.GameOverState;
import au.edu.Griffith.model.state.GameState;
import au.edu.Griffith.model.state.PausedState;
import au.edu.Griffith.model.state.RunningState;
import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoFactory;
import au.edu.Griffith.model.tetromino.TetrominoGenerator;
import au.edu.Griffith.model.tetromino.TetrominoType;

import java.util.ArrayList;
import java.util.List;

/**
 * The complete state of one Tetris field, and the rules that change it.
 *
 * <p>This is the <strong>Model</strong> of the MVC split, and the single most
 * important structural change from Milestone 1. Previously {@code Tetris.java}
 * held the grid, the score, the timer, the key handler, the {@code Rectangle}
 * objects and the scene graph in one 650-line class. Everything below is state
 * and rules only — there is not one JavaFX import in this file, or anywhere else
 * in the {@code model} package.</p>
 *
 * <p>The gravity, collision, rotation and line-clear behaviour is ported from
 * Milestone 1 unchanged, including the fractional fall used for smooth
 * movement. Level-based speed is new: Milestone 1 fell at one fixed rate.</p>
 */
public class GameModel extends Observable implements Movable {

    /**
     * Tiles fallen per 16ms frame at level 1, from Milestone 1's {@code FALL_SPEED}.
     *
     * <p>Milestone 1 had no level-based speed, so this was constant for the whole
     * game. It is now the level-1 baseline that {@link #getFallSpeed()} scales.</p>
     */
    public static final double BASE_FALL_SPEED = 0.02;

    /** Each level above the first falls this much faster. */
    private static final double SPEED_PER_LEVEL = 0.15;

    /** Lines that must clear before the level advances. */
    private static final int LINES_PER_LEVEL = 10;

    /** Wall-kick offsets tried after a rotation is blocked, in Milestone 1's order. */
    private static final int[] KICK_OFFSETS = {1, -1};

    private final Board board;
    private final Score score = new Score();
    private final TetrominoGenerator generator;

    /** Level the player chose in the configuration screen; the floor a restart returns to. */
    private final int startingLevel;

    private int level;
    private int linesCleared;

    private AbstractTetromino activePiece;
    private GameState state = new RunningState();

    /**
     * How far the piece has fallen into the row below, from 0 to 1.
     *
     * <p>Model state rather than a view concern: it is what Milestone 1's
     * {@code fallAccumulator} drove the smooth rendering from, and the renderer
     * reads it to offset the piece by a fraction of a tile.</p>
     */
    private double fallProgress;

    /** Starts at level 1. Kept so existing callers and tests need no change. */
    public GameModel(Board board, TetrominoGenerator generator) {
        this(board, generator, 1);
    }

    public GameModel(Board board, TetrominoGenerator generator, int startingLevel) {
        this.board = board;
        this.generator = generator;
        this.startingLevel = startingLevel;
        this.level = startingLevel;
    }

    public Board getBoard() {
        return board;
    }

    public Score getScore() {
        return score;
    }

    public AbstractTetromino getActivePiece() {
        return activePiece;
    }

    /** The shape queued up next, for the preview panel. */
    public TetrominoType getNextType() {
        return generator.peek();
    }

    public GameStatus getStatus() {
        return state.status();
    }

    /** Whether a player may issue movement commands in the current state. */
    public boolean acceptsInput() {
        return state.acceptsInput();
    }

    /** Sub-tile fall offset, 0 to 1, for smooth rendering. */
    public double getFallProgress() {
        return fallProgress;
    }

    /** The level chosen in configuration, before any lines were cleared. */
    public int getStartingLevel() {
        return startingLevel;
    }

    /** The level now in play, which rises as lines are cleared. */
    public int getLevel() {
        return level;
    }

    /** Total rows cleared this game, across all line clears. */
    public int getLinesCleared() {
        return linesCleared;
    }

    /** Tiles fallen per 16ms frame at the current level. */
    public double getFallSpeed() {
        return BASE_FALL_SPEED * (1 + (level - 1) * SPEED_PER_LEVEL);
    }

    // ---------------------------------------------------------------- lifecycle

    /** Spawns the first piece and begins play. */
    public void start() {
        transitionTo(new RunningState());
        spawnNextPiece();
    }

    /** Clears the board, score and level progress, and starts a fresh game. */
    public void restart() {
        board.clear();
        score.reset();
        generator.reset();
        fallProgress = 0;
        activePiece = null;
        level = startingLevel;
        linesCleared = 0;
        notifyObservers(GameEvent.of(GameEventType.SCORE_CHANGED, this));
        notifyObservers(GameEvent.of(GameEventType.LEVEL_CHANGED, this));
        start();
    }

    /** Toggles between RUNNING and PAUSED; does nothing once the game is over. */
    public void togglePause() {
        switch (state.status()) {
            case RUNNING -> transitionTo(new PausedState());
            case PAUSED -> transitionTo(new RunningState());
            case GAME_OVER -> {
                // A finished game cannot be paused.
            }
        }
    }

    /** Advances the game by one frame. Called by the controller's clock, never by a view. */
    public void tick(double elapsedMs) {
        state.tick(this, elapsedMs);
    }

    /** Switches states and publishes a {@code STATUS_CHANGED} event. */
    public void transitionTo(GameState next) {
        state = next;
        notifyObservers(GameEvent.of(GameEventType.STATUS_CHANGED, this));
    }

    // ------------------------------------------------------------------ gravity

    /**
     * Applies one frame of falling.
     *
     * <p>Ported from Milestone 1's {@code update(deltaMs)}: accumulate fractional
     * progress, lock immediately if the piece is already resting, otherwise step
     * down one row per whole tile accumulated. The rate now comes from
     * {@link #getFallSpeed()} rather than a constant.</p>
     */
    public void applyGravity(double elapsedMs) {
        if (activePiece == null) {
            return;
        }

        fallProgress += getFallSpeed() * (elapsedMs / 16.0);

        if (!canFall()) {
            fallProgress = 0;
            lockAndSpawn();
            return;
        }

        while (fallProgress >= 1.0) {
            fallProgress -= 1.0;

            if (!canFall()) {
                fallProgress = 0;
                lockAndSpawn();
                return;
            }
            activePiece.apply(activePiece.projectTranslated(0, 1));
        }

        notifyObservers(GameEvent.of(GameEventType.PIECE_MOVED, this));
    }

    // ------------------------------------------------------------ piece control

    /**
     * Takes the next shape from the generator and places it at the top of the field.
     *
     * @return {@code false} if there was no room, which means game over
     */
    public boolean spawnNextPiece() {
        AbstractTetromino candidate = TetrominoFactory.create(generator.next(), board.getWidth());

        if (!board.canPlace(candidate.getCells())) {
            activePiece = null;
            transitionTo(new GameOverState());
            return false;
        }

        activePiece = candidate;
        fallProgress = 0;
        notifyObservers(GameEvent.of(GameEventType.PIECE_SPAWNED, this));
        return true;
    }

    /** Locks the active piece, clears any completed rows and spawns the next piece. */
    private void lockAndSpawn() {
        board.lock(activePiece);
        notifyObservers(GameEvent.of(GameEventType.PIECE_LOCKED, this));

        int cleared = board.clearCompletedRows();
        if (cleared > 0) {
            score.addClearedLines(cleared);
            notifyObservers(new GameEvent(GameEventType.LINES_CLEARED, this, cleared));
            notifyObservers(GameEvent.of(GameEventType.SCORE_CHANGED, this));
            advanceLevel(cleared);
        }

        spawnNextPiece();
    }

    /**
     * Raises the level once enough rows have gone.
     *
     * <p>Computed from the running total rather than incremented, so a four-line
     * clear that crosses two thresholds lands on the right level in one step.</p>
     */
    private void advanceLevel(int cleared) {
        linesCleared += cleared;

        int target = startingLevel + linesCleared / LINES_PER_LEVEL;
        if (target > level) {
            level = target;
            notifyObservers(GameEvent.of(GameEventType.LEVEL_CHANGED, this));
        }
    }

    private boolean canFall() {
        return board.canPlace(activePiece.projectTranslated(0, 1));
    }

    @Override
    public void moveLeft() {
        shift(-1);
    }

    @Override
    public void moveRight() {
        shift(1);
    }

    @Override
    public void softDrop() {
        if (activePiece == null || !canFall()) {
            return;
        }
        activePiece.apply(activePiece.projectTranslated(0, 1));
        fallProgress = 0;
        notifyObservers(GameEvent.of(GameEventType.PIECE_MOVED, this));
    }

    /**
     * Rotates clockwise, falling back to a one-column wall kick.
     *
     * <p>Milestone 1 tried the plain rotation, then the same rotation shifted one
     * column right, then one column left, and gave up if none fitted.</p>
     */
    @Override
    public void rotate() {
        if (activePiece == null) {
            return;
        }

        List<Position> rotated = activePiece.projectRotated();
        if (board.canPlace(rotated)) {
            activePiece.apply(rotated);
            notifyObservers(GameEvent.of(GameEventType.PIECE_MOVED, this));
            return;
        }

        for (int kick : KICK_OFFSETS) {
            List<Position> kicked = shiftAll(rotated, kick);
            if (board.canPlace(kicked)) {
                activePiece.apply(kicked);
                notifyObservers(GameEvent.of(GameEventType.PIECE_MOVED, this));
                return;
            }
        }
    }

    private void shift(int dCol) {
        if (activePiece == null) {
            return;
        }
        List<Position> moved = activePiece.projectTranslated(dCol, 0);
        if (board.canPlace(moved)) {
            activePiece.apply(moved);
            notifyObservers(GameEvent.of(GameEventType.PIECE_MOVED, this));
        }
    }

    private static List<Position> shiftAll(List<Position> cells, int dCol) {
        List<Position> shifted = new ArrayList<>(cells.size());
        for (Position cell : cells) {
            shifted.add(cell.translate(dCol, 0));
        }
        return shifted;
    }
}