package au.edu.Griffith.network;

/**
 * The board state sent to {@code TetrisServer.jar}, in the shape the server's
 * protocol expects.
 *
 * <p>A separate record rather than serialising {@link au.edu.Griffith.model.Board}
 * directly: the wire format is the server's to define, and tying the model's
 * fields to it would mean an internal refactor could silently break the
 * protocol.</p>
 *
 * @param width      field width in cells
 * @param height     field height in cells
 * @param cells      occupancy grid, {@code [row][col]}, 0 empty and non-zero filled
 * @param currentShape the active piece as a 2D block layout
 * @param nextShape  the queued piece as a 2D block layout
 */
public record PureGame(int width,
                       int height,
                       int[][] cells,
                       int[][] currentShape,
                       int[][] nextShape) {
}
