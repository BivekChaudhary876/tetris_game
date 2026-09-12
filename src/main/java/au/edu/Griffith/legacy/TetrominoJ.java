package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoJ extends AbstractTetromino {
    public TetrominoJ() { initializeShape(); }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {3,0},{4,0},{5,0},{3,1}
        };
        color = Color.BLUE;
    }
}
