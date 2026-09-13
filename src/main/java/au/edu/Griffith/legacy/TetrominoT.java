package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoT extends AbstractTetromino {
    public TetrominoT() { initializeShape(); }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {3,0},{4,0},{5,0},{4,1}
        };
        color = Color.PURPLE;
    }
}

