package au.edu.Griffith.model;

import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoFactory;
import au.edu.Griffith.model.tetromino.TetrominoType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Board}.
 *
 * <p>Worth noting what is <em>not</em> here: no JavaFX, no {@code Stage}, no
 * toolkit startup. Because the board holds {@link TetrominoType} values instead
 * of {@code Rectangle} nodes, the collision and line-clear rules can be tested as
 * ordinary Java. Under the Milestone 1 design these same checks would have needed
 * a running JavaFX runtime.</p>
 */
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(Board.DEFAULT_WIDTH, Board.DEFAULT_HEIGHT);
    }

    @Test
    void newBoardIsEmpty() {
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                assertNull(board.cellAt(col, row), "cell " + col + "," + row + " should start empty");
            }
        }
    }

    @Test
    void canPlaceRejectsCellsOutsideTheField() {
        assertFalse(board.canPlace(List.of(new Position(-1, 0))), "left of the field");
        assertFalse(board.canPlace(List.of(new Position(board.getWidth(), 0))), "right of the field");
        assertFalse(board.canPlace(List.of(new Position(0, board.getHeight()))), "below the field");
        assertFalse(board.canPlace(List.of(new Position(0, -1))), "above the field");
    }

    @Test
    void canPlaceRejectsCellsAlreadyOccupied() {
        AbstractTetromino piece = TetrominoFactory.create(TetrominoType.O);
        board.lock(piece);

        assertFalse(board.canPlace(piece.getCells()), "cells just locked should be occupied");
    }

    @Test
    void lockStampsThePieceTypeIntoEveryCell() {
        AbstractTetromino piece = TetrominoFactory.create(TetrominoType.T);
        board.lock(piece);

        for (Position cell : piece.getCells()) {
            assertEquals(TetrominoType.T, board.cellAt(cell.col(), cell.row()));
        }
    }

    @Test
    void clearCompletedRowsRemovesAFullRowAndReturnsOne() {
        fillRow(board.getHeight() - 1);

        assertEquals(1, board.clearCompletedRows());
        for (int col = 0; col < board.getWidth(); col++) {
            assertNull(board.cellAt(col, board.getHeight() - 1));
        }
    }

    @Test
    void clearCompletedRowsDropsTheRowsAbove() {
        int bottom = board.getHeight() - 1;
        fillRow(bottom);
        // A single marker block sitting directly above the full row.
        board.lock(new StubPiece(TetrominoType.Z, new Position(0, bottom - 1)));

        assertEquals(1, board.clearCompletedRows());
        assertEquals(TetrominoType.Z, board.cellAt(0, bottom), "the marker should have fallen one row");
        assertNull(board.cellAt(0, bottom - 1), "its old position should now be empty");
    }

    @Test
    void clearCompletedRowsHandlesTwoAdjacentFullRows() {
        fillRow(board.getHeight() - 1);
        fillRow(board.getHeight() - 2);

        assertEquals(2, board.clearCompletedRows());
    }

    @Test
    void clearReturnsTheBoardToEmpty() {
        fillRow(board.getHeight() - 1);
        board.clear();

        assertTrue(board.canPlace(List.of(new Position(0, board.getHeight() - 1))));
    }

    private void fillRow(int row) {
        for (int col = 0; col < board.getWidth(); col++) {
            board.lock(new StubPiece(TetrominoType.I, new Position(col, row)));
        }
    }

    /** A one-cell piece, so a test can place blocks exactly where it needs them. */
    private static final class StubPiece extends AbstractTetromino {
        private StubPiece(TetrominoType type, Position where) {
            super(type, 0);
            cells.clear();
            cells.add(where);
        }
    }
}
