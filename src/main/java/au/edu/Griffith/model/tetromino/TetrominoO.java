package au.edu.Griffith.model.tetromino;

import au.edu.Griffith.model.Position;

import java.util.List;

/**
 * The O-piece: a 2x2 square.
 *
 * <p>Rotationally symmetric, so it declines to turn. Milestone 1 expressed this
 * as {@code if (currentType == O) return;} at the top of the shared rotate
 * method; overriding here says the same thing without the shared code needing to
 * know the O-piece exists (GRASP Polymorphism).</p>
 */
public class TetrominoO extends AbstractTetromino {

    public TetrominoO(int spawnCol) {
        super(TetrominoType.O, spawnCol);
    }

    @Override
    public List<Position> projectRotated() {
        return getCells();
    }
}
