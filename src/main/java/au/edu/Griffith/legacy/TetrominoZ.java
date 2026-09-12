package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public class TetrominoZ extends AbstractTetromino {
    public TetrominoZ() { initializeShape(); }

    @Override
    protected void initializeShape() {
        coords = new int[][] {
                {3,0},{4,0},{4,1},{5,1}
        };
        color = Color.RED;
    }
}

