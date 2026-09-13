package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoO extends AbstractTetromino {
    public TetrominoO() { initializeShape(); }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {4,0},{5,0},{4,1},{5,1}
        };
        color = Color.YELLOW;
    }
}
