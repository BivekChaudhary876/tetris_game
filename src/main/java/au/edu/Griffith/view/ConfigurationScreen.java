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

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * The settings screen, split into shared options plus Player 1 / Player 2.
 *
 * <p>Edits a draft {@link GameConfig}. Back asks the controller to persist it
 * to {@code config.json}. Player 2 is only enabled when extend mode is on.</p>
 */
public class ConfigurationScreen extends AbstractScreen {

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

        addSliderRow("Field Width (No of cells):",
                GameConfig.MIN_WIDTH, GameConfig.MAX_WIDTH, draft.getFieldWidth(), draft::setFieldWidth);
        addSliderRow("Field Height (No of cells):",
                GameConfig.MIN_HEIGHT, GameConfig.MAX_HEIGHT, draft.getFieldHeight(), draft::setFieldHeight);
        addSliderRow("Game Level:",
                GameConfig.MIN_LEVEL, GameConfig.MAX_LEVEL, draft.getStartingLevel(), draft::setStartingLevel);

        HBox toggles = new HBox(28,
                compactCheckBox("Music", draft.isMusicOn(), draft::setMusicOn),
                compactCheckBox("Sound Effect", draft.isSoundEffectsOn(), draft::setSoundEffectsOn),
                compactCheckBox("Extend Mode", draft.isExtendMode(), enabled -> {
                    draft.setExtendMode(enabled);
                    applyPlayerTwoEnabled(playerTwo, enabled);
                }));
        toggles.setAlignment(Pos.CENTER);
        toggles.setPadding(new Insets(8, 0, 0, 0));

        VBox shared = new VBox(10, settings, toggles);
        shared.setAlignment(Pos.CENTER);
        shared.setMaxWidth(620);
        return shared;
    }

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

    private HBox buildScale(int min, int max) {
        HBox scale = new HBox();
        scale.setPrefWidth(SLIDER_WIDTH);

        for (int i = min; i <= max; i++) {
            Label tick = new Label(String.valueOf(i));
            tick.getStyleClass().add("tick-label");
            tick.setMaxWidth(Double.MAX_VALUE);
            tick.setAlignment(Pos.CENTER);
            HBox.setHgrow(tick, Priority.ALWAYS);
            scale.getChildren().add(tick);
        }

        return scale;
    }

    @Override
    protected String rootStyleClass() {
        return "config-screen";
    }
}
