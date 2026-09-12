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

    /**
     * Column the O-piece spawns at in Milestone 1 — it is two cells wide and sits
     * one column further right than the three-wide pieces.
     */
    private static final int SPAWN_COL_O = 4;

    /** Column every other shape spawns at in Milestone 1. */
    private static final int SPAWN_COL_DEFAULT = 3;

    private TetrominoFactory() {
        // Static factory only.
    }

    /**
     * Creates a piece of the requested type at its Milestone 1 spawn position.
     *
     * @param type shape to create
     * @return a new piece, never {@code null}
     */
    public static AbstractTetromino create(TetrominoType type) {
        int spawnCol = (type == TetrominoType.O) ? SPAWN_COL_O : SPAWN_COL_DEFAULT;

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
