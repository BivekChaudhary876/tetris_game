package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.model.observer.GameEvent;
import au.edu.Griffith.model.observer.GameObserver;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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

    public PlayFieldView(GameModel model, String title, Runnable onBack, Runnable onReplay) {
        this.model = model;
        this.onReplay = onReplay;
        this.boardCanvas = new Canvas(
                model.getBoard().getWidth() * ScreenSizes.TILE,
                model.getBoard().getHeight() * ScreenSizes.TILE);
        this.renderer = new BoardRenderer(boardCanvas);
        this.sidePanel = new SidePanel(model, onBack, title);
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
            case SCORE_CHANGED, PIECE_SPAWNED -> sidePanel.refresh();
            case STATUS_CHANGED -> updateOverlays();
            case PIECE_MOVED, PIECE_LOCKED, LINES_CLEARED -> {
                // The per-frame render already covers these.
            }
        }
    }

    private HBox build() {
        pausedLabel.getStyleClass().add("overlay-paused");
        pausedLabel.setVisible(false);

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("overlay-game-over");

        Button replayButton = new Button("Replay");
        replayButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        replayButton.setFocusTraversable(false);
        replayButton.setOnAction(event -> onReplay.run());

        gameOverBox.getChildren().addAll(gameOverLabel, replayButton);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setVisible(false);

        StackPane field = new StackPane(boardCanvas, pausedLabel, gameOverBox);
        field.setAlignment(Pos.CENTER);
        field.getStyleClass().add("playfield");

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
