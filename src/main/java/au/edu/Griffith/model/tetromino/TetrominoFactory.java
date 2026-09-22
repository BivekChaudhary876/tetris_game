package au.edu.Griffith.model.tetromino;

/**
 * Factory Method: turns a {@link TetrominoType} into the matching concrete piece.
 *
 * <p>This is the <em>only</em> place in the project allowed to call
 * {@code new TetrominoI()} and friends. Everything else asks for a
 * {@code TetrominoType} and receives an {@link AbstractTetromino}, so no caller
 * ever needs to know the seven subclasses exist — that is the Dependency
 * Inversion principle applied to object creation, and it means adding a piece
 * touches this class and nothing else.</p>
 */
public final class TetrominoFactory {

    /** Board width Milestone 1's field was fixed at. */
    private static final int MILESTONE_1_WIDTH = 10;

    private TetrominoFactory() {
        // Static factory only.
    }

    /**
     * Creates a piece of the requested type, centred on a Milestone 1-sized
     * (10-column) board.
     *
     * @param type shape to create
     * @return a new piece, never {@code null}
     */
    public static AbstractTetromino create(TetrominoType type) {
        return create(type, MILESTONE_1_WIDTH);
    }

    /**
     * Creates a piece of the requested type, horizontally centred on a board
     * of the given width.
     *
     * <p>Milestone 1 spawned every piece at a fixed absolute column, which
     * only worked because its board was always 10 wide. The field width is
     * now configurable (5-15 columns), so the spawn column is derived from
     * the piece's own width instead - this reproduces Milestone 1's spawn
     * columns exactly on a 10-wide board and keeps every piece on-field on
     * narrower or wider ones.</p>
     *
     * @param type       shape to create
     * @param boardWidth width of the board the piece is spawning on
     * @return a new piece, never {@code null}
     */
    public static AbstractTetromino create(TetrominoType type, int boardWidth) {
        int spawnCol = Math.max(0, (boardWidth - type.width()) / 2);

        return switch (type) {
            case I -> new TetrominoI(spawnCol);
            case J -> new TetrominoJ(spawnCol);
            case L -> new TetrominoL(spawnCol);
            case O -> new TetrominoO(spawnCol);
            case S -> new TetrominoS(spawnCol);
            case T -> new TetrominoT(spawnCol);
            case Z -> new TetrominoZ(spawnCol);
        };
    }
}
