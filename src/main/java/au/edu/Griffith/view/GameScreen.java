package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.model.observer.GameEvent;
import au.edu.Griffith.model.observer.GameObserver;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import au.edu.Griffith.service.AudioManager;

/**
 * The playing screen: the field, its side panel, and the pause and game-over overlays.
 *
 * <p>This class is the concrete payoff of the Observer pattern. It implements
 * {@link GameObserver} and subscribes to the model; the model pushes events and
 * this screen updates. Nothing in the model layer knows this class exists, which
 * is why the same model can be driven headlessly in a JUnit test.</p>
 */
public class GameScreen extends AbstractScreen implements GameObserver {

    private final GameModel model;
    private final Runnable onBack;
    private final Runnable onReplay;

    private final Canvas boardCanvas;
    private final BoardRenderer renderer;
    private SidePanel sidePanel;

    private final Label pausedLabel = new Label("PAUSED");
    private final VBox gameOverBox = new VBox(20);

    public GameScreen(GameModel model, Runnable onBack, Runnable onReplay) {
        this.model = model;
        this.onBack = onBack;
        this.onReplay = onReplay;
        this.boardCanvas = new Canvas(
                model.getBoard().getWidth() * ScreenSizes.TILE,
                model.getBoard().getHeight() * ScreenSizes.TILE);
        this.renderer = new BoardRenderer(boardCanvas);
    }

    @Override
    public String getTitle() {
        return "Tetris";
    }

    @Override
    public double getPrefWidth() {
        return boardCanvas.getWidth() + ScreenSizes.SIDEBAR_WIDTH;
    }

    @Override
    public double getPrefHeight() {
        return boardCanvas.getHeight();
    }

    @Override
    protected Parent buildLayout() {
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

        sidePanel = new SidePanel(model, onBack);

        HBox layout = new HBox(field, sidePanel.getRoot());
        layout.setAlignment(Pos.TOP_LEFT);
        return layout;
    }

    @Override
    protected String rootStyleClass() {
        return "game-screen";
    }

    @Override
    public void onShow() {
        getRoot();
        model.addObserver(this);
        sidePanel.refresh();
        render();
    }

    @Override
    public void onHide() {
        model.removeObserver(this);
    }

    /**
     * Repaints the field.
     *
     * <p>Called every frame by the controller rather than only on events, because
     * the piece slides smoothly between rows and so its drawn position changes
     * even on frames where no event fires.</p>
     */
    public void render() {
        renderer.render(model.getBoard(), model.getActivePiece(), model.getFallProgress());
    }

    @Override
    public void onGameEvent(GameEvent event) {
        AudioManager audio = AudioManager.getInstance();

        switch (event.type()) {
            case SCORE_CHANGED, PIECE_SPAWNED -> sidePanel.refresh();
            case LEVEL_CHANGED -> {
                sidePanel.refresh();
                audio.playEffect(AudioManager.Effect.LEVEL_UP);
            }
            case LINES_CLEARED -> audio.playEffect(AudioManager.Effect.LINE_CLEAR);
            case STATUS_CHANGED -> updateOverlays();
            case PIECE_MOVED, PIECE_LOCKED -> {
                // The per-frame render already covers these.
            }
        }
    }

    private void updateOverlays() {
        GameStatus status = model.getStatus();
        pausedLabel.setVisible(status == GameStatus.PAUSED);
        gameOverBox.setVisible(status == GameStatus.GAME_OVER);

        if (status == GameStatus.GAME_OVER) {
            AudioManager.getInstance().playEffect(AudioManager.Effect.GAME_OVER);
        }
    }
}
