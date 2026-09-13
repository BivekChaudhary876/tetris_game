package au.edu.Griffith.legacy;

import javafx.scene.paint.Color;

public abstract class AbstractTetromino {

    protected int[][] coords;     // 4 block positions
    protected Color color;        // piece color
//    protected String name;        //  debugging piece name (TetrominoI, TetrominoO, TetrominoT, TetrominoL, etc.)

    public int[][] getCoords() { return coords; }
    public Color getColor() { return color; }
//    public String getName() { return name; } //debugging purposes

    protected abstract void initializeShape();
}
