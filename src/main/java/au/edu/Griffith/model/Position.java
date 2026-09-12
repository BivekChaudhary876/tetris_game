package au.edu.Griffith.model;

/**
 * An immutable cell coordinate on the playfield, measured in cells (not pixels).
 *
 * <p>A {@code record} because a position is pure value data: two positions with
 * the same column and row are the same position, and nothing should ever mutate
 * one in place.</p>
 *
 * @param col zero-based column, increasing to the right
 * @param row zero-based row, increasing downwards
 */
public record Position(int col, int row) {

    /** Returns a new position offset by the given deltas. */
    public Position translate(int dCol, int dRow) {
        return new Position(col + dCol, row + dRow);
    }
}
