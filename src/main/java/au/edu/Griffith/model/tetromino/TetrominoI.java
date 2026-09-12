package au.edu.Griffith.model.tetromino;

/** The I-piece: four cells in a line. Spawns across columns 3-6. */
public class TetrominoI extends AbstractTetromino {

    public TetrominoI(int spawnCol) {
        super(TetrominoType.I, spawnCol);
    }
}
