package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoI extends AbstractTetromino {

    public TetrominoI() {
        initializeShape();
    }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {3,0},{4,0},{5,0},{6,0}
        };
        color = Color.CYAN;
    }
}
