package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The panel beside the playfield: score, a Back button and the next-piece preview.
 *
 * <p>Exactly the three boxes Milestone 1 showed. It reads from the model and
 * writes nothing back.</p>
 */
public class SidePanel {

    private static final double BOX_SIZE = 150;
    private static final double PREVIEW_WIDTH = 150;
    private static final double PREVIEW_HEIGHT = 120;

    private final GameModel model;
    private final Runnable onBack;

    private final VBox root = new VBox(ScreenSizes.MENU_SPACING);
    private final Label scoreLabel = new Label("Score: 0");
    private final Canvas previewCanvas = new Canvas(PREVIEW_WIDTH, PREVIEW_HEIGHT);
    private final BoardRenderer previewRenderer = new BoardRenderer(previewCanvas);

    public SidePanel(GameModel model, Runnable onBack) {
        this.model = model;
        this.onBack = onBack;
        build();
    }

    public VBox getRoot() {
        return root;
    }

    private void build() {
        root.setPrefWidth(ScreenSizes.SIDEBAR_WIDTH);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("sidebar");

        scoreLabel.getStyleClass().add("score-label");

        VBox scoreBox = new VBox(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);
        scoreBox.setPrefSize(BOX_SIZE, BOX_SIZE);
        scoreBox.getStyleClass().add("panel-box");

        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setFocusTraversable(false);
        backButton.setOnAction(event -> onBack.run());

        VBox buttonBox = new VBox(10, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        Label nextLabel = new Label("Next Piece");
        nextLabel.getStyleClass().add("next-label");

        VBox previewBox = new VBox(previewCanvas);
        previewBox.setPrefSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
        previewBox.getStyleClass().add("panel-box");

        VBox nextBox = new VBox(5, nextLabel, previewBox);
        nextBox.setAlignment(Pos.TOP_LEFT);

        root.getChildren().addAll(scoreBox, buttonBox, nextBox);
    }

    /** Re-reads the model and updates the score and preview. */
    public void refresh() {
        scoreLabel.setText("Score: " + model.getScore().getPoints());
        previewRenderer.renderPreview(model.getNextType());
    }
}
