package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.observer.GameEvent;
import au.edu.Griffith.model.observer.GameObserver;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * One playfield plus its side panel: used once in single-player, twice in extend mode.
 */
public class PlayFieldView implements GameObserver {

    private final GameModel model;
    private final Runnable onReplay;

    private final Canvas boardCanvas;
    private final BoardRenderer renderer;
    private final SidePanel sidePanel;
    private final HBox root;

    private final Label pausedLabel = new Label("PAUSED");
    private final VBox gameOverBox = new VBox(20);

    public PlayFieldView(
            GameModel model,
            String playerLabel,
            PlayerType playerType,
            Runnable onReplay) {

        this(model, playerLabel, playerType, onReplay, ScreenSizes.TILE);
    }

    /**
     * @param tileSize side of one board cell, in pixels - shrunk below
     *                 {@link ScreenSizes#TILE} by {@link GameScreen} when the
     *                 configured board is too large to fit the screen at
     *                 full size
     */
    public PlayFieldView(
            GameModel model,
            String playerLabel,
            PlayerType playerType,
            Runnable onReplay,
            double tileSize) {

        this.model = model;
        this.onReplay = onReplay;
        this.boardCanvas = new Canvas(
                model.getBoard().getWidth() * tileSize,
                model.getBoard().getHeight() * tileSize);
        this.renderer = new BoardRenderer(boardCanvas, tileSize);
        this.sidePanel = new SidePanel(model, playerType, playerLabel);
        this.root = build();
    }

    public HBox getRoot() {
        return root;
    }

    public double getPrefWidth() {
        return boardCanvas.getWidth() + ScreenSizes.SIDEBAR_WIDTH;
    }

    public double getPrefHeight() {
        return boardCanvas.getHeight();
    }

    public void attach() {
        model.addObserver(this);
        sidePanel.refresh();
        render();
        updateOverlays();
    }

    public void detach() {
        model.removeObserver(this);
    }

    public void render() {
        renderer.render(model.getBoard(), model.getActivePiece(), model.getFallProgress());
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.type()) {
            case SCORE_CHANGED, PIECE_SPAWNED, LEVEL_CHANGED -> sidePanel.refresh();
            case STATUS_CHANGED -> updateOverlays();
            case PIECE_MOVED, PIECE_LOCKED, LINES_CLEARED -> {
                // The per-frame render already covers these.
            }
        }
    }

    private HBox build() {
        pausedLabel.getStyleClass().add("overlay-paused");
        pausedLabel.setVisible(false);
        // On the narrowest board (5 cells = 150px) the playfield StackPane is
        // pinned smaller than this text needs; without a min-width floor,
        // StackPane shrinks the label to fit and its text ellipsizes to
        // "PAUS...". Overflowing the board is preferable to that.
        pausedLabel.setMinWidth(Region.USE_PREF_SIZE);

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("overlay-game-over");

        Button replayButton = new Button("Replay");
        replayButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        replayButton.setFocusTraversable(false);
        replayButton.setOnAction(event -> onReplay.run());

        gameOverBox.getChildren().addAll(gameOverLabel, replayButton);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setVisible(false);
        // Same fix as pausedLabel above, for the same reason.
        gameOverBox.setMinWidth(Region.USE_PREF_SIZE);

        StackPane field = new StackPane(boardCanvas, pausedLabel, gameOverBox);
        field.setAlignment(Pos.CENTER);
        field.getStyleClass().add("playfield");

        // A StackPane sizes itself to its widest child, and the GAME OVER
        // overlay is wider than the board. Left alone it pads the canvas with a
        // black margin either side, which reads as an empty column at each edge.
        // Pinning the pane to the canvas keeps the board flush; the overlays are
        // centred text and simply sit on top.
        field.setMinSize(boardCanvas.getWidth(), boardCanvas.getHeight());
        field.setPrefSize(boardCanvas.getWidth(), boardCanvas.getHeight());
        field.setMaxSize(boardCanvas.getWidth(), boardCanvas.getHeight());

        HBox layout = new HBox(field, sidePanel.getRoot());
        layout.setAlignment(Pos.TOP_LEFT);
        return layout;
    }

    private void updateOverlays() {
        GameStatus status = model.getStatus();
        pausedLabel.setVisible(status == GameStatus.PAUSED);
        gameOverBox.setVisible(status == GameStatus.GAME_OVER);
    }
}