package au.edu.Griffith.view;

import au.edu.Griffith.controller.HighScoreController;
import au.edu.Griffith.model.ScoreEntry;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The high-score table.
 *
 * <p>Reads its rows from {@link HighScoreController} rather than from the
 * hard-coded array the Milestone 1 screen held directly — the data is the same,
 * but the view no longer owns it.</p>
 */
public class HighScoreScreen extends AbstractScreen {

    private static final double COLUMN_WIDTH = 200;

    private final HighScoreController controller;

    public HighScoreScreen(HighScoreController controller) {
        this.controller = controller;
    }

    @Override
    public String getTitle() {
        return "High Scores";
    }

    @Override
    protected Parent buildLayout() {
        Label heading = new Label("High Scores");
        heading.getStyleClass().add("heading");

        GridPane table = new GridPane();
        table.setAlignment(Pos.CENTER);
        table.setHgap(80);
        table.setVgap(12);
        table.setPadding(new Insets(20));
        table.getStyleClass().add("panel-box");

        ColumnConstraints nameColumn = new ColumnConstraints(COLUMN_WIDTH);
        nameColumn.setHalignment(HPos.LEFT);
        ColumnConstraints scoreColumn = new ColumnConstraints(COLUMN_WIDTH);
        scoreColumn.setHalignment(HPos.RIGHT);
        table.getColumnConstraints().addAll(nameColumn, scoreColumn);

        table.add(headerLabel("Name"), 0, 0);
        table.add(headerLabel("Score"), 1, 0);

        List<ScoreEntry> entries = controller.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            ScoreEntry entry = entries.get(i);
            // Row 0 holds the column headings, so scores start at row 1.
            table.add(rowLabel(entry.playerName()), 0, i + 1);
            table.add(rowLabel(String.valueOf(entry.score())), 1, i + 1);
        }

        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setOnAction(event -> controller.onBack());

        VBox layout = new VBox(30, heading, table, backButton);
        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    private Label headerLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("table-header");
        return label;
    }

    private Label rowLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("table-cell");
        return label;
    }

    @Override
    protected String rootStyleClass() {
        return "highscore-screen";
    }
}
