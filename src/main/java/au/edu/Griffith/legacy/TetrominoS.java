package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoS extends AbstractTetromino {
    public TetrominoS() { initializeShape(); }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {4,0},{5,0},{3,1},{4,1}
        };
        color = Color.GREEN;
    }
}
