package au.edu.Griffith.legacy;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HighScores {

    // Hardcoded placeholder scores until real scores are saved

    // Using java record type for score entry
    private static final ScoreEntry[] DUMMY_SCORES = {
            new ScoreEntry("Alex", 12000),
            new ScoreEntry("Sam",9800),
            new ScoreEntry("Jordan",7600),
            new ScoreEntry("Riley",5400),
            new ScoreEntry("Casey",3200),
            new ScoreEntry("Morgan",1500),
    };

    public Scene createHighScoresScene(Stage stage) {

        //heading
        Label heading = new Label("High Scores");
        heading.setTextFill(Color.WHITE);
        heading.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        //score table
        GridPane scoreTable = new GridPane();
        scoreTable.setAlignment(Pos.CENTER);
        scoreTable.setHgap(80);
        scoreTable.setVgap(12);
        scoreTable.setPadding(new Insets(20));
        scoreTable.setStyle("-fx-background-color: #222; -fx-border-color: white;");

        // Two equal-width columns: Name | Score
        ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setPrefWidth(200);
        nameColumn.setHalignment(HPos.LEFT);

        ColumnConstraints scoreColumn = new ColumnConstraints();
        scoreColumn.setPrefWidth(200);
        scoreColumn.setHalignment(HPos.RIGHT);

        scoreTable.getColumnConstraints().addAll(nameColumn, scoreColumn);

        //column headings
        Label nameHeader = new Label("Name");
        nameHeader.setTextFill(Color.WHITE);
        nameHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label scoreHeader = new Label("Score");
        scoreHeader.setTextFill(Color.WHITE);
        scoreHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        scoreTable.add(nameHeader, 0, 0);
        scoreTable.add(scoreHeader, 1, 0);

        //rows
        for (int i = 0; i < DUMMY_SCORES.length; i++) {

            Label name = new Label(DUMMY_SCORES[i].name());
            name.setTextFill(Color.WHITE);
            name.setStyle("-fx-font-size: 18px;");

            Label score = new Label(String.valueOf(DUMMY_SCORES[i].score()));
            score.setTextFill(Color.WHITE);
            score.setStyle("-fx-font-size: 18px;");

            // Row 0 holds the column headings, so scores start at row 1
            scoreTable.add(name, 0, i + 1);
            scoreTable.add(score, 1, i + 1);
        }

        //back button
        Button backButton = new Button("Back");
        backButton.setPrefWidth(150);
        backButton.setStyle(
                "-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px;"
        );
        backButton.setOnAction(e -> {
            Main main = new Main();
            main.showMainMenu(stage);
        });

        VBox layout = new VBox(30, heading, scoreTable, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(layout, 800, 700);

        stage.setTitle("High Scores");
        return scene;
    }
}
