package au.edu.Griffith.view;

import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.Position;
import au.edu.Griffith.model.tetromino.AbstractTetromino;
import au.edu.Griffith.model.tetromino.TetrominoType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Draws one playfield onto a {@link Canvas}.
 *
 * <p>This is the only place that converts a {@link TetrominoType} into a
 * {@link Color} — which is what allows the entire model package to stay free of
 * JavaFX.</p>
 *
 * <p>Canvas rather than Milestone 1's grid of {@code Rectangle} nodes: the old
 * approach created and removed hundreds of scene-graph nodes per game and had to
 * re-parent every surviving rectangle after a line clear. Repainting from the
 * board state each frame is both simpler and cheaper. The result on screen is
 * the same — filled cells with a grey outline on a black field.</p>
 */
public class BoardRenderer {

    private static final Color BACKGROUND = Color.BLACK;
    private static final Color CELL_BORDER = Color.GRAY;

    private final Canvas canvas;
    private final double tileSize;

    public BoardRenderer(Canvas canvas) {
        this(canvas, ScreenSizes.TILE);
    }

    /**
     * @param tileSize side of one board cell, in pixels - smaller than
     *                 {@link ScreenSizes#TILE} on boards configured too large
     *                 to fit the screen at full size
     */
    public BoardRenderer(Canvas canvas, double tileSize) {
        this.canvas = canvas;
        this.tileSize = tileSize;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Repaints the whole field: background, locked cells, then the active piece.
     *
     * @param board        the field to draw
     * @param activePiece  the falling piece, or {@code null} when there is none
     * @param fallProgress sub-tile offset from 0 to 1, so the piece slides
     *                     smoothly rather than jumping a whole cell at a time
     */
    public void render(Board board, AbstractTetromino activePiece, double fallProgress) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                TetrominoType locked = board.cellAt(col, row);
                if (locked != null) {
                    drawCell(gc, col * tileSize, row * tileSize,
                            tileSize, toColor(locked));
                }
            }
        }

        if (activePiece != null) {
            Color colour = toColor(activePiece.getType());
            for (Position cell : activePiece.getCells()) {
                drawCell(gc,
                        cell.col() * tileSize,
                        (cell.row() + fallProgress) * tileSize,
                        tileSize, colour);
            }
        }
    }

    /**
     * Draws the next-piece thumbnail, in the piece's own colour, inset from the
     * top-left of the panel.
     */
    public void renderPreview(TetrominoType type) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (type == null) {
            return;
        }

        double inset = ScreenSizes.PREVIEW_BLOCK / 2;
        Color colour = toColor(type);
        for (int[] offset : type.spawnOffsets()) {
            drawCell(gc,
                    offset[0] * ScreenSizes.PREVIEW_BLOCK + inset,
                    offset[1] * ScreenSizes.PREVIEW_BLOCK + inset,
                    ScreenSizes.PREVIEW_BLOCK, colour);
        }
    }

    private void drawCell(GraphicsContext gc, double x, double y, double size, Color fill) {
        gc.setFill(fill);
        gc.fillRect(x, y, size, size);
        gc.setStroke(CELL_BORDER);
        gc.strokeRect(x, y, size, size);
    }

    /** The single translation point between model data and UI colour. */
    protected Color toColor(TetrominoType type) {
        return Color.web(type.colorHex());
    }
}
