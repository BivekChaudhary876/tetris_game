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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The high-score table.
 *
 * <p>Reads its rows from {@link HighScoreController}, which in turn reads the
 * persisted {@code scores.json} table. The view still owns no ranking rules.</p>
 */
public class HighScoreScreen extends AbstractScreen {

    private static final double NAME_COLUMN_WIDTH = 180;
    private static final double SCORE_COLUMN_WIDTH = 120;
    private static final double TYPE_COLUMN_WIDTH = 120;

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
        table.setHgap(40);
        table.setVgap(12);
        table.setPadding(new Insets(20));
        table.getStyleClass().add("panel-box");

        ColumnConstraints nameColumn = new ColumnConstraints(NAME_COLUMN_WIDTH);
        nameColumn.setHalignment(HPos.LEFT);
        ColumnConstraints scoreColumn = new ColumnConstraints(SCORE_COLUMN_WIDTH);
        scoreColumn.setHalignment(HPos.RIGHT);
        ColumnConstraints typeColumn = new ColumnConstraints(TYPE_COLUMN_WIDTH);
        typeColumn.setHalignment(HPos.LEFT);
        table.getColumnConstraints().addAll(nameColumn, scoreColumn, typeColumn);

        table.add(headerLabel("Name"), 0, 0);
        table.add(headerLabel("Score"), 1, 0);
        table.add(headerLabel("Type"), 2, 0);

        List<ScoreEntry> entries = controller.getEntries();
        if (entries.isEmpty()) {
            Label empty = new Label("No scores yet");
            empty.getStyleClass().add("label-muted");
            table.add(empty, 0, 1, 3, 1);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                ScoreEntry entry = entries.get(i);
                // Row 0 holds the column headings, so scores start at row 1.
                table.add(rowLabel(entry.playerName()), 0, i + 1);
                table.add(rowLabel(String.valueOf(entry.score())), 1, i + 1);
                table.add(rowLabel(entry.playerType().displayName()), 2, i + 1);
            }
        }

        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setOnAction(event -> controller.onBack());

        Button clearButton = new Button("Clear Scores");
        clearButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        clearButton.setOnAction(event -> controller.onClear());
        clearButton.setDisable(entries.isEmpty());

        HBox buttons = new HBox(20, backButton, clearButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(30, heading, table, buttons);
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
