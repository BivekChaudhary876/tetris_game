package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.PlayerType;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The panel beside the playfield: player info and the next-piece preview.
 *
 * <p>Player info - type, initial level, current level and lines erased -
 * plus the running score, laid out as the reference Game Info panel does.
 * It reads from the model and writes nothing back.</p>
 */
public class SidePanel {

    private static final double PREVIEW_WIDTH = 150;
    private static final double PREVIEW_HEIGHT = 120;

    private final GameModel model;
    private final PlayerType playerType;
    private final String playerLabel;

    private final VBox root = new VBox(ScreenSizes.MENU_SPACING);
    private final Label currentLevelLabel = new Label("Current Level: 1");
    private final Label lineErasedLabel = new Label("Line Erased: 0");
    private final Label scoreLabel = new Label("Score: 0");
    private final Canvas previewCanvas = new Canvas(PREVIEW_WIDTH, PREVIEW_HEIGHT);
    private final BoardRenderer previewRenderer = new BoardRenderer(previewCanvas);

    public SidePanel(GameModel model, PlayerType playerType, String playerLabel) {
        this.model = model;
        this.playerType = playerType;
        this.playerLabel = playerLabel;
        build();
    }

    public VBox getRoot() {
        return root;
    }

    private void build() {
        root.setPrefWidth(ScreenSizes.SIDEBAR_WIDTH);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("sidebar");

        Label heading = new Label("Game Info (" + playerLabel + ")");
        heading.getStyleClass().add("next-label");
        heading.setWrapText(true);

        Label playerTypeLabel = new Label("Player Type: " + playerType.displayName());
        Label initialLevelLabel = new Label("Initial Level: " + model.getStartingLevel());

        currentLevelLabel.setText("Current Level: " + model.getLevel());
        lineErasedLabel.setText("Line Erased: " + model.getLinesCleared());
        scoreLabel.setText("Score: " + model.getScore().getPoints());
        scoreLabel.getStyleClass().add("score-label");

        VBox info = new VBox(
                10,
                heading,
                playerTypeLabel,
                initialLevelLabel,
                currentLevelLabel,
                lineErasedLabel,
                scoreLabel);

        info.setAlignment(Pos.TOP_LEFT);

        Label nextLabel = new Label("Next Tetromino:");
        nextLabel.getStyleClass().add("next-label");

        VBox previewBox = new VBox(previewCanvas);
        previewBox.setPrefSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        previewBox.getStyleClass().add("panel-box");

        VBox nextBox = new VBox(5, nextLabel, previewBox);
        nextBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().add(info);
        root.getChildren().add(nextBox);
    }

    /** Re-reads the model and updates the current level, lines erased, score and preview. */
    public void refresh() {
        currentLevelLabel.setText("Current Level: " + model.getLevel());
        lineErasedLabel.setText("Line Erased: " + model.getLinesCleared());
        scoreLabel.setText("Score: " + model.getScore().getPoints());
        previewRenderer.renderPreview(model.getNextType());
    }
}
