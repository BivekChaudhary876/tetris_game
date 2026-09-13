package au.edu.Griffith.model.tetromino;

import au.edu.Griffith.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Common base for the seven tetromino pieces.
 *
 * <p>Holds the four board cells the piece currently occupies and the rules for
 * projecting where those cells would move to. Positions are absolute board
 * coordinates, matching the Milestone 1 {@code gx[]}/{@code gy[]} arrays this
 * was ported from.</p>
 *
 * <p>Every projection method returns a <em>candidate</em> list and changes
 * nothing. The board tests the candidate for collisions and only then calls
 * {@link #apply(List)}. That is what makes an illegal move a no-op rather than
 * something that has to be undone.</p>
 *
 * <p>Open/Closed in practice: an eighth piece means one new subclass plus one
 * enum constant, and nothing already written has to change.</p>
 */
public abstract class AbstractTetromino {

    /** Which of the seven shapes this piece is. */
    protected final TetrominoType type;

    /** The four board cells the piece occupies, in spawn order. */
    protected final List<Position> cells;

    /**
     * @param type     shape to build
     * @param spawnCol column the shape's leftmost cell starts at
     */
    protected AbstractTetromino(TetrominoType type, int spawnCol) {
        this.type = type;
        this.cells = new ArrayList<>(4);
        for (int[] offset : type.spawnOffsets()) {
            cells.add(new Position(offset[0] + spawnCol, offset[1]));
        }
    }

    public TetrominoType getType() {
        return type;
    }

    /** The cells currently occupied. Unmodifiable. */
    public List<Position> getCells() {
        return List.copyOf(cells);
    }

    /** Where the piece would sit if shifted by the given deltas. */
    public List<Position> projectTranslated(int dCol, int dRow) {
        List<Position> projected = new ArrayList<>(4);
        for (Position cell : cells) {
            projected.add(cell.translate(dCol, dRow));
        }
        return projected;
    }

    /**
     * Where the piece would sit after a 90-degree clockwise turn.
     *
     * <p>Ported verbatim from Milestone 1: the piece turns about its own second
     * cell, computed from the live coordinates rather than looked up from a
     * table of rotation states. Keeping the pivot as "whatever cell index 1
     * currently is" is what reproduces the original feel exactly, including the
     * slight drift the I-piece shows when turned repeatedly.</p>
     *
     * <p>Overridable so {@link TetrominoO} can decline to turn at all.</p>
     */
    public List<Position> projectRotated() {
        Position pivot = cells.get(1);

        List<Position> projected = new ArrayList<>(4);
        for (Position cell : cells) {
            int relativeCol = cell.col() - pivot.col();
            int relativeRow = cell.row() - pivot.row();
            projected.add(new Position(
                    pivot.col() - relativeRow,
                    pivot.row() + relativeCol));
        }
        return projected;
    }

    /** Commits a projection the board has already accepted. */
    public void apply(List<Position> projected) {
        cells.clear();
        cells.addAll(projected);
    }
}
