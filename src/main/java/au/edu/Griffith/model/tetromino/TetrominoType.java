package au.edu.Griffith.model.tetromino;

/**
 * The seven tetromino shapes, together with the data that is the same for every
 * instance of a shape: its spawn layout and its colour.
 *
 * <p>Flyweight in spirit — the per-shape constants are stored once on the enum
 * instead of being duplicated on every piece that is spawned.</p>
 *
 * <p>Shapes and colours are copied exactly from the Milestone 1
 * {@code legacy.TetrominoX} classes (spawn coordinates normalised to start at
 * column 0; the original absolute spawn column is reproduced by
 * {@link TetrominoFactory}). The colour is held as a hex string rather than a
 * JavaFX {@code Color} so the whole model package stays independent of JavaFX;
 * the view converts it when rendering.</p>
 */
public enum TetrominoType {

    /** Legacy spawn: (3,0) (4,0) (5,0) (6,0), {@code Color.CYAN}. */
    I("#00FFFF", new int[][]{{0, 0}, {1, 0}, {2, 0}, {3, 0}}),

    /** Legacy spawn: (3,0) (4,0) (5,0) (3,1), {@code Color.BLUE}. */
    J("#0000FF", new int[][]{{0, 0}, {1, 0}, {2, 0}, {0, 1}}),

    /** Legacy spawn: (3,0) (4,0) (5,0) (5,1), {@code Color.ORANGE}. */
    L("#FFA500", new int[][]{{0, 0}, {1, 0}, {2, 0}, {2, 1}}),

    /** Legacy spawn: (4,0) (5,0) (4,1) (5,1), {@code Color.YELLOW}. */
    O("#FFFF00", new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}),

    /** Legacy spawn: (4,0) (5,0) (3,1) (4,1), {@code Color.GREEN} (#008000, not CSS lime). */
    S("#008000", new int[][]{{1, 0}, {2, 0}, {0, 1}, {1, 1}}),

    /** Legacy spawn: (3,0) (4,0) (5,0) (4,1), {@code Color.PURPLE}. */
    T("#800080", new int[][]{{0, 0}, {1, 0}, {2, 0}, {1, 1}}),

    /** Legacy spawn: (3,0) (4,0) (4,1) (5,1), {@code Color.RED}. */
    Z("#FF0000", new int[][]{{0, 0}, {1, 0}, {1, 1}, {2, 1}});

    private final String colorHex;
    private final int[][] spawnOffsets;

    TetrominoType(String colorHex, int[][] spawnOffsets) {
        this.colorHex = colorHex;
        this.spawnOffsets = spawnOffsets;
    }

    /** Web colour for this shape, for example {@code "#00FFFF"}. */
    public String colorHex() {
        return colorHex;
    }

    /**
     * The four {@code {col, row}} offsets that make up this shape at spawn,
     * relative to column 0 of its own bounding box.
     *
     * <p>Returns a defensive copy: the array is shared state on an enum constant
     * and must never be handed out in a form a caller could mutate.</p>
     */
    public int[][] spawnOffsets() {
        int[][] copy = new int[spawnOffsets.length][];
        for (int i = 0; i < spawnOffsets.length; i++) {
            copy[i] = spawnOffsets[i].clone();
        }
        return copy;
    }
}
