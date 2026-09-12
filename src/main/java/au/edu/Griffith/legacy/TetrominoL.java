package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoL extends AbstractTetromino {
    public TetrominoL() { initializeShape(); }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {3,0},{4,0},{5,0},{5,1}
        };
        color = Color.ORANGE;
    }
}
