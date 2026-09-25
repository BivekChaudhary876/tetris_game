package au.edu.Griffith.view;

import au.edu.Griffith.controller.HighScoreController;
import au.edu.Griffith.model.HighScoreTable;
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

    private static final double RANK_COLUMN_WIDTH = 50;
    private static final double NAME_COLUMN_WIDTH = 180;
    private static final double SCORE_COLUMN_WIDTH = 120;
    private static final double TYPE_COLUMN_WIDTH = 120;

    /** Shown in place of a name, score or type for a rank not yet earned. */
    private static final String PLACEHOLDER = "----";

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

        ColumnConstraints rankColumn = new ColumnConstraints(RANK_COLUMN_WIDTH);
        rankColumn.setHalignment(HPos.LEFT);
        ColumnConstraints nameColumn = new ColumnConstraints(NAME_COLUMN_WIDTH);
        nameColumn.setHalignment(HPos.LEFT);
        ColumnConstraints scoreColumn = new ColumnConstraints(SCORE_COLUMN_WIDTH);
        scoreColumn.setHalignment(HPos.RIGHT);
        ColumnConstraints typeColumn = new ColumnConstraints(TYPE_COLUMN_WIDTH);
        typeColumn.setHalignment(HPos.LEFT);
        table.getColumnConstraints().addAll(rankColumn, nameColumn, scoreColumn, typeColumn);

        table.add(headerLabel("#"), 0, 0);
        table.add(headerLabel("Name"), 1, 0);
        table.add(headerLabel("Score"), 2, 0);
        table.add(headerLabel("Type"), 3, 0);

        // Always MAX_ENTRIES rows, even with no scores yet - real entries fill
        // from the top, and every rank below them shows as a placeholder.
        List<ScoreEntry> entries = controller.getEntries();
        for (int i = 0; i < HighScoreTable.MAX_ENTRIES; i++) {
            // Row 0 holds the column headings, so ranks start at row 1.
            int row = i + 1;

            table.add(rowLabel("(" + row + ")"), 0, row);

            if (i < entries.size()) {
                ScoreEntry entry = entries.get(i);
                table.add(rowLabel(entry.playerName()), 1, row);
                table.add(rowLabel(String.valueOf(entry.score())), 2, row);
                table.add(rowLabel(entry.playerType().displayName()), 3, row);
            } else {
                table.add(rowLabel(PLACEHOLDER), 1, row);
                table.add(rowLabel("0"), 2, row);
                table.add(rowLabel(PLACEHOLDER), 3, row);
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
