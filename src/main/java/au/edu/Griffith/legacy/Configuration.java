package au.edu.Griffith.legacy;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Configuration screen.
 *
 * The sliders and checkboxes are fully interactive - dragging a slider updates
 * the value shown beside it and ticking a box flips its On/Off label. Nothing is
 * saved or applied to the game yet; wiring these to the actual game settings is
 * a later task.
 */
public class Configuration {

    // Field width slider (number of cells across)
    private static final int WIDTH_MIN = 5;
    private static final int WIDTH_MAX = 15;
    private static final int WIDTH_DEFAULT = 10;

    // Field height slider (number of cells down)
    private static final int HEIGHT_MIN = 15;
    private static final int HEIGHT_MAX = 30;
    private static final int HEIGHT_DEFAULT = 20;

    // Game level slider
    private static final int LEVEL_MIN = 1;
    private static final int LEVEL_MAX = 10;
    private static final int LEVEL_DEFAULT = 6;

    private static final int SLIDER_WIDTH = 260;

    private static final String LABEL_STYLE = "-fx-font-size: 16px;";
    private static final String VALUE_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold;";

    public Scene createConfigurationScene(Stage stage) {

        //heading
        Label heading = new Label("Configuration");
        heading.setTextFill(Color.WHITE);
        heading.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        //settings table
        GridPane settings = new GridPane();
        settings.setAlignment(Pos.CENTER);
        settings.setHgap(40);
        settings.setVgap(25);
        settings.setPadding(new Insets(30));
        settings.setStyle("-fx-background-color: #222; -fx-border-color: white;");

        // Three columns: setting name | control | current value
        ColumnConstraints nameColumn = new ColumnConstraints();
        nameColumn.setPrefWidth(220);
        nameColumn.setHalignment(HPos.LEFT);

        ColumnConstraints controlColumn = new ColumnConstraints();
        controlColumn.setPrefWidth(260);
        controlColumn.setHalignment(HPos.LEFT);

        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setPrefWidth(60);
        valueColumn.setHalignment(HPos.RIGHT);

        settings.getColumnConstraints().addAll(nameColumn, controlColumn, valueColumn);

        int row = 0;
        addSliderRow(settings, row++, "Field Width (No of cells):", WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT);
        addSliderRow(settings, row++, "Field Height (No of cells):", HEIGHT_MIN, HEIGHT_MAX, HEIGHT_DEFAULT);
        addSliderRow(settings, row++, "Game Level:", LEVEL_MIN, LEVEL_MAX, LEVEL_DEFAULT);
        addCheckBoxRow(settings, row++, "Music (On/Off):", true);
        addCheckBoxRow(settings, row++, "Sound Effect (On/Off):", true);
        addCheckBoxRow(settings, row++, "AI Play (On/Off):", false);
        addCheckBoxRow(settings, row++, "Extend Mode (On/Off):", false);

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

        //footer
        Label author = new Label("Author: Group 5");
        author.setTextFill(Color.WHITE);
        author.setStyle("-fx-font-size: 12px;");

        VBox layout = new VBox(25, heading, settings, backButton, author);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(layout, 800, 700);

        stage.setTitle("Configuration");
        return scene;
    }

    /**
     * Adds one slider setting: name on the left, the slider in the middle and
     * the live value on the right.
     */
    private void addSliderRow(GridPane grid, int row, String text, int min, int max, int initial) {

        Label name = new Label(text);
        name.setTextFill(Color.WHITE);
        name.setStyle(LABEL_STYLE);

        Slider slider = new Slider(min, max, initial);
        slider.setPrefWidth(SLIDER_WIDTH);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1);

        Label value = new Label(String.valueOf(initial));
        value.setTextFill(Color.WHITE);
        value.setStyle(VALUE_STYLE);

        // Sliders report doubles, so round back to a whole number of cells
        slider.valueProperty().addListener((observable, oldValue, newValue) ->
                value.setText(String.valueOf(Math.round(newValue.doubleValue())))
        );

        VBox control = new VBox(2, slider, buildScale(min, max));
        control.setPrefWidth(SLIDER_WIDTH);

        grid.add(name, 0, row);
        grid.add(control, 1, row);
        grid.add(value, 2, row);
    }

    /**
     * Builds the row of numbers shown under a slider.
     *
     * The slider's own setShowTickLabels() draws these through an axis nested in
     * the control, and that axis renders them near-black no matter what style is
     * applied to the slider - unreadable on this dark panel. Laying the numbers
     * out here keeps them under our control.
     */
    private HBox buildScale(int min, int max) {

        HBox scale = new HBox();
        scale.setPrefWidth(SLIDER_WIDTH);

        for (int i = min; i <= max; i++) {

            Label tick = new Label(String.valueOf(i));
            tick.setTextFill(Color.WHITE);
            tick.setStyle("-fx-font-size: 11px;");

            // Equal share of the width each, so the numbers spread evenly
            tick.setMaxWidth(Double.MAX_VALUE);
            tick.setAlignment(Pos.CENTER);
            HBox.setHgrow(tick, Priority.ALWAYS);

            scale.getChildren().add(tick);
        }

        return scale;
    }

    /**
     * Adds one on/off setting: name on the left, the tick box in the middle and
     * the "On"/"Off" text on the right.
     */
    private void addCheckBoxRow(GridPane grid, int row, String text, boolean selected) {

        Label name = new Label(text);
        name.setTextFill(Color.WHITE);
        name.setStyle(LABEL_STYLE);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);

        Label state = new Label(selected ? "On" : "Off");
        state.setTextFill(Color.WHITE);
        state.setStyle(VALUE_STYLE);

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setText(newValue ? "On" : "Off")
        );

        grid.add(name, 0, row);
        grid.add(checkBox, 1, row);
        grid.add(state, 2, row);
    }
}
