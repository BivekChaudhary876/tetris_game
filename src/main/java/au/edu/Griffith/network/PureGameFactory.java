package au.edu.Griffith.network;

import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.Position;
import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoType;

import java.util.List;

/**
 * Builds the {@link PureGame} snapshot {@code TetrisServer.jar} expects out of
 * a live {@link GameModel}.
 *
 * <p>Kept separate from {@link au.edu.Griffith.player.ExternalPlayer} so the
 * "how do I describe the board to the wire protocol" logic can be unit tested
 * without a socket.</p>
 */
public final class PureGameFactory {

    private PureGameFactory() {
        // Static utility only.
    }

    public static PureGame from(GameModel model) {
        Board board = model.getBoard();
        int width = board.getWidth();
        int height = board.getHeight();

        int[][] cells = new int[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                cells[row][col] = board.cellAt(col, row) != null ? 1 : 0;
            }
        }

        int[][] currentShape = shapeOf(model.getActivePiece());
        int[][] nextShape = spawnShapeOf(model.getNextType());

        return new PureGame(width, height, cells, currentShape, nextShape);
    }

    /** The active piece's cells, normalised into a tight 0/1 bounding-box matrix. */
    private static int[][] shapeOf(AbstractTetromino piece) {
        return toMatrix(piece.getCells());
    }

    /** The next piece's spawn layout, normalised the same way. */
    private static int[][] spawnShapeOf(TetrominoType type) {
        int[][] offsets = type.spawnOffsets();
        return toMatrix(offsets);
    }

    private static int[][] toMatrix(List<Position> cells) {
        int minCol = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        int maxRow = Integer.MIN_VALUE;
        for (Position cell : cells) {
            minCol = Math.min(minCol, cell.col());
            maxCol = Math.max(maxCol, cell.col());
            minRow = Math.min(minRow, cell.row());
            maxRow = Math.max(maxRow, cell.row());
        }

        int[][] matrix = new int[maxRow - minRow + 1][maxCol - minCol + 1];
        for (Position cell : cells) {
            matrix[cell.row() - minRow][cell.col() - minCol] = 1;
        }
        return matrix;
    }

    private static int[][] toMatrix(int[][] offsets) {
        int minCol = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;
        int maxCol = Integer.MIN_VALUE;
        int maxRow = Integer.MIN_VALUE;
        for (int[] offset : offsets) {
            minCol = Math.min(minCol, offset[0]);
            maxCol = Math.max(maxCol, offset[0]);
            minRow = Math.min(minRow, offset[1]);
            maxRow = Math.max(maxRow, offset[1]);
        }

        int[][] matrix = new int[maxRow - minRow + 1][maxCol - minCol + 1];
        for (int[] offset : offsets) {
            matrix[offset[1] - minRow][offset[0] - minCol] = 1;
        }
        return matrix;
    }
}