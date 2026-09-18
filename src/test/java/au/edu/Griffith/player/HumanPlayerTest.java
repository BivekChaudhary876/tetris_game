package au.edu.Griffith.player;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.Position;
import au.edu.Griffith.model.tetromino.StubTetrominoGenerator;
import au.edu.Griffith.model.tetromino.TetrominoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HumanPlayerTest {

    private GameModel model;
    private HumanPlayer player;

    @BeforeEach
    void setUp() {
        model = new GameModel(
                new Board(Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT),
                new StubTetrominoGenerator(TetrominoType.I, TetrominoType.O));
        model.start();
        player = new HumanPlayer();
        player.attach(model);
    }

    @Test
    void onActionMovesThePieceWhenTheGameIsRunning() {
        List<Position> before = model.getActivePiece().getCells();

        player.onAction(CommandFactory.Action.MOVE_LEFT);

        assertEquals(before.getFirst().col() - 1, model.getActivePiece().getCells().getFirst().col());
    }

    @Test
    void onActionIsIgnoredWhilePaused() {
        model.togglePause();
        List<Position> before = model.getActivePiece().getCells();

        player.onAction(CommandFactory.Action.MOVE_LEFT);

        assertEquals(before, model.getActivePiece().getCells());
    }
}
