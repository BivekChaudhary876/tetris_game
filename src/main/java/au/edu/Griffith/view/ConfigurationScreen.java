package au.edu.Griffith.view;

import au.edu.Griffith.controller.ConfigurationController;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The settings screen: field size, level, music, sound, AI play and extend mode.
 *
 * <p>Presentational only, exactly as in Milestone 1 — dragging a slider updates
 * the value beside it and ticking a box flips its On/Off label, but nothing is
 * saved or applied to the game. Wiring these to real settings is a later
 * milestone requirement.</p>
 */
public class ConfigurationScreen extends AbstractScreen {

    private static final int WIDTH_MIN = 5;
    private static final int WIDTH_MAX = 15;
    private static final int WIDTH_DEFAULT = 10;

    private static final int HEIGHT_MIN = 15;
    private static final int HEIGHT_MAX = 30;
    private static final int HEIGHT_DEFAULT = 20;

    private static final int LEVEL_MIN = 1;
    private static final int LEVEL_MAX = 10;
    private static final int LEVEL_DEFAULT = 6;

    private static final double SLIDER_WIDTH = 260;

    private final ConfigurationController controller;
    private GridPane settings;
    private int nextRow;

    public ConfigurationScreen(ConfigurationController controller) {
        this.controller = controller;
    }

    @Override
    public String getTitle() {
        return "Configuration";
    }

    @Override
    protected Parent buildLayout() {
        Label heading = new Label("Configuration");
        heading.getStyleClass().add("heading");

        settings = new GridPane();
        settings.setAlignment(Pos.CENTER);
        settings.setHgap(40);
        settings.setVgap(25);
        settings.setPadding(new Insets(30));
        settings.getStyleClass().add("panel-box");

        ColumnConstraints nameColumn = new ColumnConstraints(220);
        nameColumn.setHalignment(HPos.LEFT);
        ColumnConstraints controlColumn = new ColumnConstraints(SLIDER_WIDTH);
        controlColumn.setHalignment(HPos.LEFT);
        ColumnConstraints valueColumn = new ColumnConstraints(60);
        valueColumn.setHalignment(HPos.RIGHT);
        settings.getColumnConstraints().addAll(nameColumn, controlColumn, valueColumn);

        addSliderRow("Field Width (No of cells):", WIDTH_MIN, WIDTH_MAX, WIDTH_DEFAULT);
        addSliderRow("Field Height (No of cells):", HEIGHT_MIN, HEIGHT_MAX, HEIGHT_DEFAULT);
        addSliderRow("Game Level:", LEVEL_MIN, LEVEL_MAX, LEVEL_DEFAULT);
        addCheckBoxRow("Music (On/Off):", true);
        addCheckBoxRow("Sound Effect (On/Off):", true);
        addCheckBoxRow("AI Play (On/Off):", false);
        addCheckBoxRow("Extend Mode (On/Off):", false);

        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setOnAction(event -> controller.onBack());

        Label author = new Label("Author: Group 5");
        author.getStyleClass().add("label-muted");

        VBox layout = new VBox(25, heading, settings, backButton, author);
        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    /**
     * Adds one slider setting: name on the left, the slider in the middle and the
     * live value on the right.
     */
    private void addSliderRow(String text, int min, int max, int initial) {
        Label name = new Label(text);

        Slider slider = new Slider(min, max, initial);
        slider.setPrefWidth(SLIDER_WIDTH);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1);

        Label value = new Label(String.valueOf(initial));
        value.getStyleClass().add("value-label");

        // Sliders report doubles, so round back to a whole number of cells.
        slider.valueProperty().addListener((observable, oldValue, newValue) ->
                value.setText(String.valueOf(Math.round(newValue.doubleValue()))));

        VBox control = new VBox(2, slider, buildScale(min, max));
        control.setPrefWidth(SLIDER_WIDTH);

        settings.add(name, 0, nextRow);
        settings.add(control, 1, nextRow);
        settings.add(value, 2, nextRow);
        nextRow++;
    }

    /**
     * Builds the row of numbers shown under a slider.
     *
     * <p>The slider's own {@code setShowTickLabels()} draws these through a nested
     * axis that renders near-black whatever style is applied — unreadable on this
     * dark panel. Laying the numbers out here keeps them under our control.</p>
     */
    private HBox buildScale(int min, int max) {
        HBox scale = new HBox();
        scale.setPrefWidth(SLIDER_WIDTH);

        for (int i = min; i <= max; i++) {
            Label tick = new Label(String.valueOf(i));
            tick.getStyleClass().add("tick-label");

            // Equal share of the width each, so the numbers spread evenly.
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
    private void addCheckBoxRow(String text, boolean selected) {
        Label name = new Label(text);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);

        Label state = new Label(selected ? "On" : "Off");
        state.getStyleClass().add("value-label");

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                state.setText(newValue ? "On" : "Off"));

        settings.add(name, 0, nextRow);
        settings.add(checkBox, 1, nextRow);
        settings.add(state, 2, nextRow);
        nextRow++;
    }

    @Override
    protected String rootStyleClass() {
        return "config-screen";
    }
}
