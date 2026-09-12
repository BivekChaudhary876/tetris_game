package au.edu.Griffith.legacy;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;


public class Tetris implements Movable {

    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int TILE = 30;

    private Pane gameRoot;
    private Pane nextPiecePane;
    private Label scoreLabel;

    private Rectangle[][] grid = new Rectangle[ROWS][COLS];

    private Rectangle[] piece = new Rectangle[4];
    private int[] gx = new int[4];
    private int[] gy = new int[4];

    private double fallAccumulator = 0;
    private static final double FALL_SPEED = 0.02; // tiles per ms

    private TetrominoType currentType;
    private TetrominoType nextType;

    private enum TetrominoType { I, O, T, L, J, S, Z }

    private int score = 0;

    private boolean isGameOver = false;
    private boolean isPaused = false; //Tracks whether the game is currently paused
    private AnimationTimer timer;

    private Label pauseLabel; //Displays the PAUSED message on the game board

    //countdown
    private boolean isStartingDelay = true;

    public Scene createTetrisScene(Stage stage) {

        //root
        gameRoot = new Pane();
        gameRoot.setPrefSize(COLS * TILE, ROWS * TILE);
        gameRoot.setStyle("-fx-background-color: black;");

        //Pause Function to display "PAUSED" label when the game is paused
        pauseLabel = new Label("PAUSED");
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setStyle(
                "-fx-font-size: 32px; " +
                "-fx-font-weight: bold;" +
                "-fx-background-color: rgba(0, 0, 0, 0.75);" +
                "-fx-padding: 20px;"
        );

        pauseLabel.setPrefWidth(COLS * TILE);
        pauseLabel.setAlignment(Pos.CENTER);
        pauseLabel.setLayoutY((ROWS * TILE) / 2.0 - 45);
        pauseLabel.setVisible(false);

        gameRoot.getChildren().add(pauseLabel);
        pauseLabel.toFront();

        //vbox
        VBox sideBar = new VBox(20);
        sideBar.setPrefWidth(200);
        sideBar.setStyle("-fx-background-color: #222;");
        sideBar.setAlignment(Pos.TOP_CENTER);

        //score
        scoreLabel = new Label("Score: 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // SCORE BOX (square)
        VBox scoreBox = new VBox(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPrefSize(150, 150);
        scoreBox.setStyle("-fx-background-color: #444; -fx-border-color: white;");

        //Sidebar buttons
        //back button
        Button backButton = new Button("Back");
        backButton.setPrefWidth(150);
        backButton.setStyle(
                "-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px;"
        );
        backButton.setFocusTraversable(false);

        //replayButton
        Button replayButton = new Button("Replay");
        replayButton.setPrefWidth(150);
        replayButton.setStyle(
                "-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px;"
        );
        replayButton.setFocusTraversable(false);
        backButton.setOnAction(e -> {

            //Pause if game while the dialog is open
            boolean oldPauseState = isPaused;
            isPaused = true;
            pauseLabel.setVisible(true);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm");
            alert.setHeaderText("Return to Main Menu?");
            alert.setContentText("Cancel to resume game");

            ButtonType yes = new ButtonType("Yes");
            ButtonType cancel = ButtonType.CANCEL;

            alert.getButtonTypes().setAll(yes, cancel);

            alert.showAndWait().ifPresent(response -> {

                if (response == yes) {
                    //back to main menu
                    isGameOver = false;
                    fallAccumulator = 0;

                    if (timer != null) timer.stop();
                    gameRoot.getChildren().clear();

                    Main main = new Main();
                    main.showMainMenu(stage);

                } else {
                    //resume game
                    isPaused = oldPauseState;
                    pauseLabel.setVisible(isPaused);
                }
            });
        });


        // Button container
        VBox buttonBox = new VBox(10, backButton);
        buttonBox.setAlignment(Pos.CENTER);


        //next piece
        Label nextLabel = new Label("Next Piece");
        nextLabel.setTextFill(Color.WHITE);
        nextLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        nextLabel.setTranslateX(5); // upper-left position

        nextPiecePane = new Pane();
        nextPiecePane.setPrefSize(150, 120);
        nextPiecePane.setStyle("-fx-background-color: #444; -fx-border-color: white;");

        VBox nextBox = new VBox(5, nextLabel, nextPiecePane);
        nextBox.setAlignment(Pos.TOP_LEFT);
        nextBox.setPrefSize(150, 165);

        //side bar
        sideBar.getChildren().addAll(scoreBox, buttonBox, nextBox);

        Pane root = new Pane();
        root.setPrefSize(COLS * TILE + 200, ROWS * TILE);

        gameRoot.setTranslateX(0);
        sideBar.setTranslateX(COLS * TILE);

        root.getChildren().addAll(gameRoot, sideBar);

        Scene scene = new Scene(root);

        nextType = getRandomType();
        spawnPiece();


        scene.setOnKeyPressed(e -> {
            if (isGameOver) return;

            if (e.getCode() == KeyCode.P) {
                togglePause();
                return;
            }

            if (isPaused) return; // P key toggles between pause and resume by disabling other controls

            switch (e.getCode()) {
                case LEFT -> moveLeft();
                case RIGHT -> moveRight();
                case UP -> rotatePiece();
                case DOWN -> softDropPiece();
            }
        });

        //movement animation
        timer = new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (isGameOver) return; // stop updating when game over

                //Prevents movement from continuing when the game is paused, and ensures that the last timestamp is updated to the current time when resuming.
                if (isPaused) {
                    last = now;
                    return;
                }
                if (last == 0) { last = now; return; }

                double deltaMs = (now - last) / 1_000_000.0;
                last = now;

                update(deltaMs);
            }
        };
        timer.start();

        stage.setTitle("Tetris");
        return scene;
    }

    // spawn piece
    private void spawnPiece() {
        currentType = nextType;
        nextType = getRandomType();
        drawNextPiecePreview();

        // Create tetromino subclass
        AbstractTetromino tetromino = switch (currentType) {
            case I -> new TetrominoI();
            case O -> new TetrominoO();
            case T -> new TetrominoT();
            case L -> new TetrominoL();
            case J -> new TetrominoJ();
            case S -> new TetrominoS();
            case Z -> new TetrominoZ();
        };

        // Get shape + color from abstract class
        int[][] coords = tetromino.getCoords();
        Color c = tetromino.getColor();

        // Apply tetromino blocks to your gx/gy arrays
        for (int i = 0; i < 4; i++) {
            gx[i] = coords[i][0];
            gy[i] = coords[i][1];

            // GAME OVER CHECK
            if (grid[gy[i]][gx[i]] != null) {
                gameOver();
                return;
            }

            Rectangle r = new Rectangle(TILE, TILE, c);
            r.setStroke(Color.GRAY);
            piece[i] = r;
            gameRoot.getChildren().add(r);
            pauseLabel.toFront();
        }

        renderPiece();
    }


    // piece gravity
    private void update(double deltaMs) {

        // Smooth falling only while NOT colliding
        fallAccumulator += FALL_SPEED * (deltaMs / 16.0);

        // If the next fractional fall would collide, lock immediately
        if (!canFall()) {

            // stop smoothfall
            fallAccumulator = 0;

            // snap
            renderPiece();

            // lock and spawn
            lockPiece();
            clearLines();
            spawnPiece();
            return;
        }

        // Apply full-tile falling
        while (fallAccumulator >= 1.0) {
            fallAccumulator -= 1.0;

            if (!canFall()) {

                fallAccumulator = 0;
                renderPiece();

                lockPiece();
                clearLines();
                spawnPiece();
                return;
            }

            for (int i = 0; i < 4; i++) gy[i]++;
        }
        renderPiece();
    }


    //Toggles the game state and PAUSED label visibility when the "P" key is pressed.
    private void togglePause() {

        if (isGameOver) {
            return;
        }

        isPaused = !isPaused;

        if (isPaused) {
            //Make sure the pause label exists on the game board
            if (!gameRoot.getChildren().contains(pauseLabel)) {
                gameRoot.getChildren().add(pauseLabel);
            }

            pauseLabel.setVisible(true);
            pauseLabel.toFront();
        } else {
            pauseLabel.setVisible(false);
        }
    }

    //gameover
    private void gameOver() {
        isGameOver = true;

        if (timer != null) {
            timer.stop();
        }

        Label over = new Label("GAME OVER");
        over.setTextFill(Color.RED);
        over.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        // Center on the Tetris board
        over.setTranslateX((COLS * TILE) / 2 - 80);
        over.setTranslateY((ROWS * TILE) / 2 - 60);

        //replay
        Button replayButton = new Button("Replay");
        replayButton.setPrefWidth(150);
        replayButton.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px;");
        replayButton.setTranslateX((COLS * TILE) / 2 - 75);
        replayButton.setTranslateY((ROWS * TILE) / 2 - 0);

        replayButton.setOnAction(e -> restartGame());

        gameRoot.getChildren().addAll(over, replayButton);
    }

    //restart game logic
    private void restartGame() {

        // Reset pause state when Replay starts a new game
        isPaused = false;
        pauseLabel.setVisible(false);

        // Remove active falling piece
        for (Rectangle r : piece) {
            if (r != null) {
                gameRoot.getChildren().remove(r);
            }
        }
        // Clear grid
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (grid[y][x] != null) {
                    gameRoot.getChildren().remove(grid[y][x]);
                    grid[y][x] = null;
                }
            }
        }

        // Remove GAME OVER + replay button
        gameRoot.getChildren().removeIf(node ->
                (node instanceof Label && node != pauseLabel)
                        || node instanceof Button
        );

        score = 0;
        scoreLabel.setText("Score: 0");

        isGameOver = false;

        nextType = getRandomType();
        spawnPiece();

        // Make sure the PAUSED label still exists board after replay
        if (!gameRoot.getChildren().contains(pauseLabel)) {
            gameRoot.getChildren().add(pauseLabel);
        }

        // Keep PAUSED message above all tetrominoes
        pauseLabel.toFront();
        pauseLabel.setVisible(false);

        timer.start();
    }

    // Collision
    private boolean canFall() {
        for (int i = 0; i < 4; i++) {

            int nx = gx[i];
            int ny = gy[i] + 1;

            // Bottom boundary
            if (ny >= ROWS) return false;

            // Block below grid
            if (grid[ny][nx] != null) return false;
        }
        return true;
    }

    private boolean canMove(int dx) {
        for (int i = 0; i < 4; i++) {
            int nx = gx[i] + dx;
            int ny = gy[i];

            if (nx < 0 || nx >= COLS) return false;
            if (grid[ny][nx] != null) return false;
        }
        return true;
    }

    // movement logic
    private void move(int dx) {
        if (canMove(dx)) {
            for (int i = 0; i < 4; i++) gx[i] += dx;
            renderPiece();
        }
    }

    private void softDrop() {
        if (canFall()) {
            for (int i = 0; i < 4; i++) gy[i]++;
            fallAccumulator = 0;
            renderPiece();
        }
    }

    //rotate piece
    private void rotate() {
        if (currentType == TetrominoType.O) return;

        int px = gx[1];
        int py = gy[1];

        int[] newGX = new int[4];
        int[] newGY = new int[4];

        boolean fail = false;

        // Try normal rotation
        for (int i = 0; i < 4; i++) {
            int rx = gx[i] - px;
            int ry = gy[i] - py;

            int nx = px - ry;
            int ny = py + rx;

            newGX[i] = nx;
            newGY[i] = ny;

            if (nx < 0 || nx >= COLS || ny < 0 || ny >= ROWS || grid[ny][nx] != null) {
                fail = true;
            }
        }

        // If normal rotation works
        if (!fail) {
            gx = newGX;
            gy = newGY;
            renderPiece();
            return;
        }

        // Try wall kick 1 tile right
        if (canKick(newGX, newGY, 1)) {
            for (int i = 0; i < 4; i++) newGX[i] += 1;
            gx = newGX;
            gy = newGY;
            renderPiece();
            return;
        }

        // Try wall kick 1 tile left
        if (canKick(newGX, newGY, -1)) {
            for (int i = 0; i < 4; i++) newGX[i] -= 1;
            gx = newGX;
            gy = newGY;
            renderPiece();
            return;
        }

    }

    // wall back 1 tile
    private boolean canKick(int[] newGX, int[] newGY, int dx) {
        for (int i = 0; i < 4; i++) {
            int nx = newGX[i] + dx;
            int ny = newGY[i];

            //Prevent negative row
            if (ny < 0 || ny >= ROWS) return false;

            //Prevent negative column
            if (nx < 0 || nx >= COLS) return false;

            if (grid[ny][nx] != null) return false;
        }
        return true;
    }

    //end rotate piece

    // Lock Piece
    private void lockPiece() {
        for (int i = 0; i < 4; i++) {
            grid[gy[i]][gx[i]] = piece[i];
        }
    }

    // clear line
    private void clearLines() {
        int cleared = 0;

        for (int y = 0; y < ROWS; y++) {
            boolean full = true;

            for (int x = 0; x < COLS; x++) {
                if (grid[y][x] == null) {
                    full = false;
                    break;
                }
            }

            if (full) {
                cleared++;
                for (int x = 0; x < COLS; x++) {
                    gameRoot.getChildren().remove(grid[y][x]);
                    grid[y][x] = null;
                }

                for (int row = y - 1; row >= 0; row--) {
                    for (int x = 0; x < COLS; x++) {
                        Rectangle r = grid[row][x];
                        if (r != null) {
                            grid[row + 1][x] = r;
                            grid[row][x] = null;
                            r.setTranslateY((row + 1) * TILE);
                        }
                    }
                }
            }
        }


        if (cleared > 0) {
            score += cleared * 100;
            scoreLabel.setText("Score: " + score);
        }
    }

    // Smoothmovement
    private void renderPiece() {
        for (int i = 0; i < 4; i++) {

            double px = gx[i] * TILE;
            double py;

            // If fallAccumulator is 0 (collision), snap to grid
            if (fallAccumulator == 0) {
                py = gy[i] * TILE;
            } else {
                py = (gy[i] + fallAccumulator) * TILE;
            }

            piece[i].setTranslateX(px);
            piece[i].setTranslateY(py);
        }
    }

    // Next piece
    private void drawNextPiecePreview() {
        nextPiecePane.getChildren().clear();

        int[][] coords = switch (nextType) {
            case I -> new int[][] {{0,0},{1,0},{2,0},{3,0}};
            case O -> new int[][] {{0,0},{1,0},{0,1},{1,1}};
            case T -> new int[][] {{0,0},{1,0},{2,0},{1,1}};
            case L -> new int[][] {{0,0},{1,0},{2,0},{2,1}};
            case J -> new int[][] {{0,0},{1,0},{2,0},{0,1}};
            case S -> new int[][] {{1,0},{2,0},{0,1},{1,1}};
            case Z -> new int[][] {{0,0},{1,0},{1,1},{2,1}};
        };

        int blockSize = 28;
        Color c = Color.WHITE;

        for (int[] cxy : coords) {
            Rectangle r = new Rectangle(blockSize, blockSize, c);
            r.setStroke(Color.GRAY);
            r.setTranslateX(cxy[0] * blockSize + 40);
            r.setTranslateY(cxy[1] * blockSize + 40);
            nextPiecePane.getChildren().add(r);
        }
    }

    private TetrominoType getRandomType() {
        TetrominoType[] v = TetrominoType.values();
        return v[(int)(Math.random() * v.length)];
    }

    //movement interface
    @Override
    public void moveLeft() {
        move(-1);
    }

    @Override
    public void moveRight() {
        move(1);
    }

    @Override
    public void softDropPiece() {
        softDrop();
    }

    @Override
    public void rotatePiece() {
        rotate();
    }

}
