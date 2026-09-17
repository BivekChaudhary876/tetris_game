package au.edu.Griffith.model;

import au.edu.Griffith.model.observer.GameEvent;
import au.edu.Griffith.model.observer.GameEventType;
import au.edu.Griffith.model.tetromino.StubTetrominoGenerator;
import au.edu.Griffith.model.tetromino.TetrominoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GameModel}, driven by a {@link StubTetrominoGenerator} so the
 * piece sequence is known in advance.
 *
 * <p>Also covers the Observer wiring: a lambda registered as a
 * {@link au.edu.Griffith.model.observer.GameObserver} records the events the
 * model publishes, which is how the view's contract gets tested without a
 * view.</p>
 */
class GameModelTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        model = new GameModel(
                new Board(Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT),
                new StubTetrominoGenerator(TetrominoType.I, TetrominoType.O));
        model.start();
    }

    @Test
    void startSpawnsTheFirstPieceFromTheGenerator() {
        assertEquals(TetrominoType.I, model.getActivePiece().getType());
        assertEquals(GameStatus.RUNNING, model.getStatus());
    }

    @Test
    void startingPieceSitsAtTheMilestoneOneSpawnPosition() {
        // Milestone 1 spawned the I-piece across columns 3 to 6 on the top row.
        assertEquals(
                List.of(new Position(3, 0), new Position(4, 0),
                        new Position(5, 0), new Position(6, 0)),
                model.getActivePiece().getCells());
    }

    @Test
    void movingLeftShiftsThePieceOneColumn() {
        List<Position> before = model.getActivePiece().getCells();

        model.moveLeft();

        assertEquals(before.get(0).col() - 1, model.getActivePiece().getCells().get(0).col());
    }

    @Test
    void movingLeftAtTheWallIsRejected() {
        for (int i = 0; i < 10; i++) {
            model.moveLeft();
        }
        List<Position> atWall = model.getActivePiece().getCells();

        model.moveLeft();

        assertEquals(atWall, model.getActivePiece().getCells(), "the piece should not pass the left edge");
        assertEquals(0, atWall.get(0).col());
    }

    @Test
    void theOPieceDoesNotRotate() {
        GameModel oGame = new GameModel(
                new Board(Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT),
                new StubTetrominoGenerator(TetrominoType.O));
        oGame.start();
        List<Position> before = oGame.getActivePiece().getCells();

        oGame.rotate();

        assertEquals(before, oGame.getActivePiece().getCells());
    }

    /**
     * Turning the flat I-piece about its second cell would put one block on row
     * -1, and both wall kicks only shift sideways, so the rotation is refused.
     * Milestone 1 behaved the same way — its rotate and canKick both rejected
     * {@code ny < 0}.
     */
    @Test
    void theIPieceCannotRotateOnTheSpawnRow() {
        List<Position> before = model.getActivePiece().getCells();

        model.rotate();

        assertEquals(before, model.getActivePiece().getCells());
    }

    @Test
    void theIPieceRotatesUprightOnceItHasRoomAbove() {
        model.softDrop();
        List<Position> before = model.getActivePiece().getCells();

        model.rotate();

        List<Position> after = model.getActivePiece().getCells();
        assertNotEquals(before, after);
        // A vertical bar: every cell in the same column.
        assertTrue(after.stream().allMatch(cell -> cell.col() == after.get(0).col()));
    }

    @Test
    void aPausedGameIgnoresGravity() {
        List<Position> before = model.getActivePiece().getCells();

        model.togglePause();
        model.tick(10_000);

        assertEquals(GameStatus.PAUSED, model.getStatus());
        assertEquals(before, model.getActivePiece().getCells());
    }

    @Test
    void unpausingResumesTheGame() {
        model.togglePause();
        model.togglePause();

        assertEquals(GameStatus.RUNNING, model.getStatus());
    }

    @Test
    void acceptsInputOnlyWhileRunning() {
        assertTrue(model.acceptsInput());

        model.togglePause();
        assertEquals(false, model.acceptsInput());

        model.togglePause();
        assertTrue(model.acceptsInput());
    }

    @Test
    void observersAreNotifiedWhenThePieceMoves() {
        List<GameEventType> received = new ArrayList<>();
        model.addObserver(event -> received.add(event.type()));

        model.moveLeft();

        assertTrue(received.contains(GameEventType.PIECE_MOVED));
    }

    @Test
    void removedObserversStopReceivingEvents() {
        List<GameEvent> received = new ArrayList<>();
        var observer = (au.edu.Griffith.model.observer.GameObserver) received::add;

        model.addObserver(observer);
        model.removeObserver(observer);
        model.moveLeft();

        assertTrue(received.isEmpty());
    }

    @Test
    void spawningIntoAnOccupiedCellEndsTheGame() {
        // Drop pieces until the stack reaches the spawn row.
        for (int i = 0; i < 500 && model.getStatus() == GameStatus.RUNNING; i++) {
            model.tick(10_000);
        }

        assertEquals(GameStatus.GAME_OVER, model.getStatus());
    }

    @Test
    void restartClearsTheScoreAndReturnsToRunning() {
        model.getScore().addClearedLines(3);

        model.restart();

        assertEquals(0, model.getScore().getPoints());
        assertEquals(GameStatus.RUNNING, model.getStatus());
    }
}
