package au.edu.Griffith.model;

import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoType;

import java.util.List;

/**
 * The playfield: a grid of locked cells, plus the rules about what may occupy them.
 *
 * <p>Information Expert (GRASP): the board owns the grid, so the board — not the
 * controller and certainly not the view — answers "does this piece fit?", locks
 * a piece and clears full rows. In the Milestone 1 code those checks lived
 * inside the JavaFX class alongside the {@code Rectangle}s they drew; here the
 * grid holds {@link TetrominoType} values and knows nothing about pixels.</p>
 *
 * <p>Milestone 1 spread its bounds checking across four methods — {@code canFall}
 * tested only the bottom edge, {@code canMove} only the side edges,
 * {@code rotate} and {@code canKick} tested all four. {@link #canPlace(List)}
 * replaces all of them with one full check, which is equivalent because a move
 * that does not change a coordinate cannot push it out of bounds.</p>
 */
public class Board {

    /** Columns across, as in Milestone 1. */
    public static final int DEFAULT_WIDTH = 10;

    /** Rows down, as in Milestone 1. */
    public static final int DEFAULT_HEIGHT = 20;

    private final int width;
    private final int height;

    /** {@code null} means the cell is empty. Indexed {@code [row][col]}. */
    private final TetrominoType[][] grid;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new TetrominoType[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** The shape locked at a cell, or {@code null} if it is empty or out of bounds. */
    public TetrominoType cellAt(int col, int row) {
        if (!isInBounds(col, row)) {
            return null;
        }
        return grid[row][col];
    }

    /** True if the coordinate lies inside the field. */
    public boolean isInBounds(int col, int row) {
        return col >= 0 && col < width && row >= 0 && row < height;
    }

    /**
     * True if every one of the candidate cells is in bounds and empty.
     *
     * <p>The single collision test behind moving, rotating and spawning, so those
     * three operations cannot drift apart.</p>
     */
    public boolean canPlace(List<Position> cells) {
        for (Position cell : cells) {
            if (!isInBounds(cell.col(), cell.row())) {
                return false;
            }
            if (grid[cell.row()][cell.col()] != null) {
                return false;
            }
        }
        return true;
    }

    /** Writes a piece's cells into the grid permanently. */
    public void lock(AbstractTetromino piece) {
        for (Position cell : piece.getCells()) {
            grid[cell.row()][cell.col()] = piece.getType();
        }
    }

    /**
     * Removes every completed row and drops the rows above down.
     *
     * <p>Ported from Milestone 1's {@code clearLines}: scan top to bottom, and on
     * a full row shift everything above it down by one. Rows below the cleared
     * one are untouched, so a later full row is still found on a subsequent pass
     * of the same loop.</p>
     *
     * @return how many rows were cleared
     */
    public int clearCompletedRows() {
        int cleared = 0;

        for (int row = 0; row < height; row++) {
            if (!isRowFull(row)) {
                continue;
            }
            cleared++;
            collapseInto(row);
        }

        return cleared;
    }

    /** Empties every cell, for a replay. */
    public void clear() {
        for (int row = 0; row < height; row++) {
            java.util.Arrays.fill(grid[row], null);
        }
    }

    private boolean isRowFull(int row) {
        for (int col = 0; col < width; col++) {
            if (grid[row][col] == null) {
                return false;
            }
        }
        return true;
    }

    /** Overwrites {@code targetRow} with the row above it, repeating up to the top. */
    private void collapseInto(int targetRow) {
        for (int row = targetRow; row > 0; row--) {
            System.arraycopy(grid[row - 1], 0, grid[row], 0, width);
        }
        java.util.Arrays.fill(grid[0], null);
    }
}
