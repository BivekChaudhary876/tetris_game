package au.edu.Griffith.view;

import au.edu.Griffith.controller.ConfigurationController;
import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.PlayerType;
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

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The settings screen: field size, level, music, sound, AI play and extend mode.
 *
 * <p>Edits a draft {@link GameConfig}. Back asks the controller to persist it.</p>
 */
public class ConfigurationScreen extends AbstractScreen {

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

        GameConfig draft = controller.getDraft();
        addSliderRow("Field Width (No of cells):",
                GameConfig.MIN_WIDTH, GameConfig.MAX_WIDTH, draft.getFieldWidth(), draft::setFieldWidth);
        addSliderRow("Field Height (No of cells):",
                GameConfig.MIN_HEIGHT, GameConfig.MAX_HEIGHT, draft.getFieldHeight(), draft::setFieldHeight);
        addSliderRow("Game Level:",
                GameConfig.MIN_LEVEL, GameConfig.MAX_LEVEL, draft.getStartingLevel(), draft::setStartingLevel);
        addCheckBoxRow("Music (On/Off):", draft.isMusicOn(), draft::setMusicOn);
        addCheckBoxRow("Sound Effect (On/Off):", draft.isSoundEffectsOn(), draft::setSoundEffectsOn);
        addCheckBoxRow("AI Play (On/Off):",
                draft.getPlayerOneType() == PlayerType.AI,
                enabled -> draft.setPlayerOneType(enabled ? PlayerType.AI : PlayerType.HUMAN));
        addCheckBoxRow("Extend Mode (On/Off):", draft.isExtendMode(), draft::setExtendMode);

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
    private void addSliderRow(String text, int min, int max, int initial, IntConsumer onChange) {
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
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int rounded = (int) Math.round(newValue.doubleValue());
            value.setText(String.valueOf(rounded));
            onChange.accept(rounded);
        });

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
    private void addCheckBoxRow(String text, boolean selected, Consumer<Boolean> onChange) {
        Label name = new Label(text);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);

        Label state = new Label(selected ? "On" : "Off");
        state.getStyleClass().add("value-label");

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            state.setText(newValue ? "On" : "Off");
            onChange.accept(newValue);
        });

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
