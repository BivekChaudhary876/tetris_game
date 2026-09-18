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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.PlayerType;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The settings screen: field size, level, volume, music, sound, AI play and
 * extend mode.
 *
 * <p>The screen opens showing the current settings and reports every change to
 * {@link ConfigurationController}, which holds them in a draft and commits that
 * draft to disk when the user navigates back. The screen itself stores nothing
 * and knows nothing about persistence.</p>
 */
public class ConfigurationScreen extends AbstractScreen {

    // Bounds come from the model so the sliders and GameConfig.validate() cannot drift apart.
    private static final int WIDTH_MIN = GameConfig.MIN_WIDTH;
    private static final int WIDTH_MAX = GameConfig.MAX_WIDTH;

    private static final int HEIGHT_MIN = GameConfig.MIN_HEIGHT;
    private static final int HEIGHT_MAX = GameConfig.MAX_HEIGHT;

    private static final int LEVEL_MIN = GameConfig.MIN_LEVEL;
    private static final int LEVEL_MAX = GameConfig.MAX_LEVEL;

    private static final int VOLUME_MIN = GameConfig.MIN_VOLUME;
    private static final int VOLUME_MAX = GameConfig.MAX_VOLUME;

    /** Volume moves in tens, so its scale shows 0, 10, 20 … rather than every value. */
    private static final int VOLUME_STEP = 10;

    private static final double SLIDER_WIDTH = 260;
    private static final double PLAYER_PANEL_WIDTH = 280;

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

        VBox playerOne = buildPlayerPanel("Player 1", draft.getPlayerOneType(), draft::setPlayerOneType);
        VBox playerTwo = buildPlayerPanel("Player 2", draft.getPlayerTwoType(), draft::setPlayerTwoType);
        applyPlayerTwoEnabled(playerTwo, draft.isExtendMode());

        HBox players = new HBox(24, playerOne, playerTwo);
        players.setAlignment(Pos.CENTER);

        VBox shared = buildSharedSettings(draft, playerTwo);

        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setOnAction(event -> controller.onBack());

        Label author = new Label("Author: Group 5");
        author.getStyleClass().add("label-muted");

        VBox layout = new VBox(18, heading, shared, players, backButton, author);
        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    private VBox buildSharedSettings(GameConfig draft, VBox playerTwo) {
        settings = new GridPane();
        nextRow = 0;
        settings.setAlignment(Pos.CENTER);
        settings.setHgap(40);
        settings.setVgap(12);
        settings.setPadding(new Insets(20));
        settings.getStyleClass().add("panel-box");

        ColumnConstraints nameColumn = new ColumnConstraints(220);
        nameColumn.setHalignment(HPos.LEFT);
        ColumnConstraints controlColumn = new ColumnConstraints(SLIDER_WIDTH);
        controlColumn.setHalignment(HPos.LEFT);
        ColumnConstraints valueColumn = new ColumnConstraints(60);
        valueColumn.setHalignment(HPos.RIGHT);
        settings.getColumnConstraints().addAll(nameColumn, controlColumn, valueColumn);

        // Every control starts at the saved value, not a hardcoded one.
        GameConfig draft = controller.getDraft();

        // field width is now being get in the drafts
        addSliderRow("Field Width (No of cells):", WIDTH_MIN, WIDTH_MAX,
                draft.getFieldWidth(), controller::setFieldWidth);
        addSliderRow("Field Height (No of cells):", HEIGHT_MIN, HEIGHT_MAX,
                draft.getFieldHeight(), controller::setFieldHeight);
        addSliderRow("Game Level:", LEVEL_MIN, LEVEL_MAX,
                draft.getStartingLevel(), controller::setStartingLevel);
        addSliderRow("Volume (%):", VOLUME_MIN, VOLUME_MAX,
                draft.getVolume(), controller::setVolume, VOLUME_STEP);

        addCheckBoxRow("Music (On/Off):",
                draft.isMusicOn(), controller::setMusic);
        addCheckBoxRow("Sound Effect (On/Off):",
                draft.isSoundEffectsOn(), controller::setSoundEffects);
        addCheckBoxRow("AI Play (On/Off):",
                draft.getPlayerTwoType() == PlayerType.AI, controller::setAiPlay);
        addCheckBoxRow("Extend Mode (On/Off):",
                draft.isExtendMode(), controller::setExtendMode);

        //runs the onBack functions
        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setOnAction(event -> controller.onBack());

    private VBox buildPlayerPanel(String title, PlayerType selected, Consumer<PlayerType> onChange) {
        Label heading = new Label(title);
        heading.getStyleClass().add("subheading");

        ToggleGroup group = new ToggleGroup();
        VBox radios = new VBox(12);
        for (PlayerType type : PlayerType.values()) {
            RadioButton radio = new RadioButton(type.displayName());
            radio.setToggleGroup(group);
            radio.setSelected(type == selected);
            radio.setUserData(type);
            radio.getStyleClass().add("player-type-radio");
            radios.getChildren().add(radio);
        }
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onChange.accept((PlayerType) newValue.getUserData());
            }
        });

        VBox panel = new VBox(16, heading, radios);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(PLAYER_PANEL_WIDTH);
        panel.getStyleClass().addAll("panel-box", "player-panel");
        return panel;
    }

    private static void applyPlayerTwoEnabled(VBox playerTwo, boolean extendMode) {
        playerTwo.setDisable(!extendMode);
        playerTwo.setOpacity(extendMode ? 1.0 : 0.45);
    }

    private HBox compactCheckBox(String text, boolean selected, Consumer<Boolean> onChange) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);

        Label name = new Label(text);
        Label state = new Label(selected ? "On" : "Off");
        state.getStyleClass().add("value-label");

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            state.setText(newValue ? "On" : "Off");
            onChange.accept(newValue);
        });

        HBox row = new HBox(8, checkBox, name, state);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Adds a slider whose scale shows every value, which suits the small ranges. */
    private void addSliderRow(String text, int min, int max, int initial, IntConsumer onChange) {
        addSliderRow(text, min, max, initial, onChange, 1);
    }

    /**
     * Adds one slider setting: name on the left, the slider in the middle and the
     * live value on the right.
     *
     * @param onChange   notified with the new whole-number value on every move
     * @param scaleStep  gap between the numbers drawn under the slider; a wide
     *                   range such as volume needs more than 1 or the labels are
     *                   too dense to read
     */
    private void addSliderRow(String text, int min, int max, int initial,
                              IntConsumer onChange, int scaleStep) {
        Label name = new Label(text);

        Slider slider = new Slider(min, max, initial);
        slider.setPrefWidth(SLIDER_WIDTH);
        slider.setMajorTickUnit(scaleStep);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(scaleStep);

        Label value = new Label(String.valueOf(initial));
        value.getStyleClass().add("value-label");

        // Sliders report doubles, so round back to a whole number.
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int rounded = (int) Math.round(newValue.doubleValue());
            value.setText(String.valueOf(rounded));
            onChange.accept(rounded);
        });

        VBox control = new VBox(2, slider, buildScale(min, max, scaleStep));
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
     *
     * @param step gap between the numbers drawn, so a 0–100 range shows eleven
     *             labels rather than a hundred and one
     */
    private HBox buildScale(int min, int max, int step) {
        HBox scale = new HBox();
        scale.setPrefWidth(SLIDER_WIDTH);

        for (int i = min; i <= max; i += step) {
            Label tick = new Label(String.valueOf(i));
            tick.getStyleClass().add("tick-label");
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
     *
     * @param onChange notified with the new state every time the box is ticked
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