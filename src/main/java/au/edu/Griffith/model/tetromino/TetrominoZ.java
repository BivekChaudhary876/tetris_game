package au.edu.Griffith.model.tetromino;

/** The Z-piece: two offset horizontal pairs, stepping down to the right. */
public class TetrominoZ extends AbstractTetromino {

    public TetrominoZ(int spawnCol) {
        super(TetrominoType.Z, spawnCol);
    }
}
