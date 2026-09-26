package au.edu.Griffith.view;

import au.edu.Griffith.controller.ConfigurationController;
import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.PlayerType;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The settings screen: one column of labelled rows - sliders, on/off
 * checkboxes and inline player-type radio groups - laid out the way the
 * reference Configuration mockup does, with a full-width Back bar beneath.
 *
 * <p>Edits a draft {@link GameConfig}. Back asks the controller to persist it
 * to JavaTetrisConfig.json. Player 2 is only enabled when extend mode is on.</p>
 *
 * <p>The screen reports changes to {@link ConfigurationController}, which
 * applies audio settings immediately and persists the configuration when
 * the user navigates back.</p>
 */
public class ConfigurationScreen extends AbstractScreen {

    // Bounds come from the model so the sliders and GameConfig.validate()
    // cannot drift apart.
    private static final int WIDTH_MIN = GameConfig.MIN_WIDTH;
    private static final int WIDTH_MAX = GameConfig.MAX_WIDTH;

    private static final int HEIGHT_MIN = GameConfig.MIN_HEIGHT;
    private static final int HEIGHT_MAX = GameConfig.MAX_HEIGHT;

    private static final int LEVEL_MIN = GameConfig.MIN_LEVEL;
    private static final int LEVEL_MAX = GameConfig.MAX_LEVEL;

    private static final int VOLUME_MIN = GameConfig.MIN_VOLUME;
    private static final int VOLUME_MAX = GameConfig.MAX_VOLUME;

    /** Volume moves in tens so its scale shows 0, 10, 20 … */
    private static final int VOLUME_STEP = 10;

    private static final double SLIDER_WIDTH = 260;
    private static final double PANEL_WIDTH = 620;

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
        GameConfig draft = controller.getDraft();

        Label heading = new Label("Configuration");
        heading.getStyleClass().add("heading");

        VBox shared = buildSharedSettings(draft);

        Button backButton = new Button("Back");
        backButton.setPrefWidth(PANEL_WIDTH);
        backButton.setOnAction(event -> controller.onBack());

        Label author = new Label("Author: Group 5");
        author.getStyleClass().add("label-muted");

        VBox layout = new VBox(
                18,
                heading,
                shared,
                backButton,
                author);

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        return layout;
    }

    /**
     * Builds every setting as one row in a single grid: sliders, then the
     * on/off checkboxes, then the two player-type rows - matching the
     * reference mockup's single-column layout.
     */
    private VBox buildSharedSettings(GameConfig draft) {
        settings = new GridPane();
        nextRow = 0;

        settings.setAlignment(Pos.CENTER);
        settings.setHgap(24);
        settings.setVgap(14);
        settings.setPadding(new Insets(24));
        settings.getStyleClass().add("panel-box");

        ColumnConstraints nameColumn = new ColumnConstraints(220);
        nameColumn.setHalignment(HPos.LEFT);

        ColumnConstraints controlColumn = new ColumnConstraints(SLIDER_WIDTH);
        controlColumn.setHalignment(HPos.LEFT);

        ColumnConstraints valueColumn = new ColumnConstraints(60);
        valueColumn.setHalignment(HPos.RIGHT);

        settings.getColumnConstraints().addAll(
                nameColumn,
                controlColumn,
                valueColumn);

        addSliderRow(
                "Field Width (No of cells):",
                WIDTH_MIN,
                WIDTH_MAX,
                draft.getFieldWidth(),
                controller::setFieldWidth);

        addSliderRow(
                "Field Height (No of cells):",
                HEIGHT_MIN,
                HEIGHT_MAX,
                draft.getFieldHeight(),
                controller::setFieldHeight);

        addSliderRow(
                "Game Level:",
                LEVEL_MIN,
                LEVEL_MAX,
                draft.getStartingLevel(),
                controller::setStartingLevel);

        addSliderRow(
                "Volume (%):",
                VOLUME_MIN,
                VOLUME_MAX,
                draft.getVolume(),
                controller::setVolume,
                VOLUME_STEP);

        addCheckboxRow(
                "Music (On|Off):",
                draft.isMusicOn(),
                controller::setMusic);

        addCheckboxRow(
                "Sound Effect (On|Off):",
                draft.isSoundEffectsOn(),
                controller::setSoundEffects);

        // Built ahead of the Extend Mode row so that row's checkbox can
        // enable/disable it - Player Two's own row is added further below.
        HBox playerTwoRadios = buildPlayerTypeRadios(
                draft.getPlayerTwoType(),
                draft::setPlayerTwoType);

        addCheckboxRow(
                "Extend Mode (On|Off):",
                draft.isExtendMode(),
                enabled -> {
                    controller.setExtendMode(enabled);
                    applyPlayerTwoEnabled(playerTwoRadios, enabled);
                });

        HBox playerOneRadios = buildPlayerTypeRadios(
                draft.getPlayerOneType(),
                draft::setPlayerOneType);

        addRow("Player One Type:", playerOneRadios);
        addRow("Player Two Type:", playerTwoRadios);

        applyPlayerTwoEnabled(playerTwoRadios, draft.isExtendMode());

        VBox shared = new VBox(settings);
        shared.setAlignment(Pos.CENTER);
        shared.setMaxWidth(PANEL_WIDTH);

        return shared;
    }

    /**
     * Builds an inline Human / AI / External radio group for one player.
     *
     * <p>Returned rather than placed directly, so the Extend Mode checkbox
     * can capture Player Two's group before Player Two's row is added to
     * the grid.</p>
     */
    private HBox buildPlayerTypeRadios(
            PlayerType selected,
            Consumer<PlayerType> onChange) {

        ToggleGroup group = new ToggleGroup();
        HBox radios = new HBox(20);
        radios.setAlignment(Pos.CENTER_LEFT);

        for (PlayerType type : PlayerType.values()) {
            RadioButton radio = new RadioButton(type.displayName());

            radio.setToggleGroup(group);
            radio.setSelected(type == selected);
            radio.setUserData(type);
            radio.getStyleClass().add("player-type-radio");

            radios.getChildren().add(radio);
        }

        group.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue != null) {
                        onChange.accept(
                                (PlayerType) newValue.getUserData());
                    }
                });

        return radios;
    }

    /**
     * Enables Player 2's row only when Extend Mode is active.
     */
    private static void applyPlayerTwoEnabled(
            HBox playerTwoRadios,
            boolean extendMode) {

        playerTwoRadios.setDisable(!extendMode);
        playerTwoRadios.setOpacity(extendMode ? 1.0 : 0.45);
    }

    /**
     * Adds a name/control row with no value column, for the player-type
     * radio groups.
     */
    private void addRow(String text, Node control) {
        Label name = new Label(text);

        settings.add(name, 0, nextRow);
        settings.add(control, 1, nextRow);

        nextRow++;
    }

    /**
     * Adds an on/off checkbox row: name on the left, checkbox in the
     * middle, and the live On/Off state on the right.
     */
    private void addCheckboxRow(
            String text,
            boolean selected,
            Consumer<Boolean> onChange) {

        Label name = new Label(text);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);

        Label state = new Label(selected ? "On" : "Off");
        state.getStyleClass().add("value-label");

        checkBox.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {

                    state.setText(newValue ? "On" : "Off");
                    onChange.accept(newValue);
                });

        settings.add(name, 0, nextRow);
        settings.add(checkBox, 1, nextRow);
        settings.add(state, 2, nextRow);

        nextRow++;
    }

    /**
     * Adds a slider setting using a scale step of one.
     */
    private void addSliderRow(
            String text,
            int min,
            int max,
            int initial,
            IntConsumer onChange) {

        addSliderRow(
                text,
                min,
                max,
                initial,
                onChange,
                1);
    }

    /**
     * Adds one slider setting: name on the left, slider in the middle,
     * and the live value on the right.
     *
     * @param onChange notified with the new whole-number value
     * @param scaleStep gap between the numbers drawn under the slider
     */
    private void addSliderRow(
            String text,
            int min,
            int max,
            int initial,
            IntConsumer onChange,
            int scaleStep) {

        Label name = new Label(text);

        Slider slider = new Slider(
                min,
                max,
                initial);

        slider.setPrefWidth(
                SLIDER_WIDTH);

        slider.setMajorTickUnit(
                scaleStep);

        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(
                scaleStep);

        Label value = new Label(
                String.valueOf(initial));

        value.getStyleClass().add(
                "value-label");

        // Sliders report doubles, so round back to a whole number.
        slider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {

                    int rounded =
                            (int) Math.round(
                                    newValue.doubleValue());

                    value.setText(
                            String.valueOf(rounded));

                    onChange.accept(rounded);
                });

        VBox control = new VBox(
                2,
                slider,
                buildScale(
                        min,
                        max,
                        scaleStep));

        control.setPrefWidth(
                SLIDER_WIDTH);

        settings.add(
                name,
                0,
                nextRow);

        settings.add(
                control,
                1,
                nextRow);

        settings.add(
                value,
                2,
                nextRow);

        nextRow++;
    }

    /**
     * Builds the row of numbers shown under a slider.
     *
     * <p>Laying the numbers out ourselves keeps the scale readable
     * on the dark configuration panel.</p>
     */
    private HBox buildScale(
            int min,
            int max,
            int step) {

        HBox scale = new HBox();

        scale.setPrefWidth(
                SLIDER_WIDTH);

        for (int i = min; i <= max; i += step) {
            Label tick =
                    new Label(String.valueOf(i));

            tick.getStyleClass().add(
                    "tick-label");

            tick.setMaxWidth(
                    Double.MAX_VALUE);

            tick.setAlignment(
                    Pos.CENTER);

            HBox.setHgrow(
                    tick,
                    Priority.ALWAYS);

            scale.getChildren().add(tick);
        }

        return scale;
    }

    @Override
    protected String rootStyleClass() {
        return "config-screen";
    }
}
