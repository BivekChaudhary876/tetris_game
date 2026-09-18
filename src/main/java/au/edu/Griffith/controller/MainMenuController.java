package au.edu.Griffith.controller;

import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.tetromino.SharedSequenceGenerator;
import au.edu.Griffith.player.Player;
import au.edu.Griffith.player.PlayerFactory;
import au.edu.Griffith.service.ConfigService;
import au.edu.Griffith.view.ConfigurationScreen;
import au.edu.Griffith.view.HighScoreScreen;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles what the main-menu buttons mean.
 *
 * <p>Controller (GRASP): a use-case handler that receives the user's intent
 * from the view, decides what should happen and delegates to the model and
 * other controllers. It holds no UI nodes, so it can be tested by calling
 * its methods directly.</p>
 */
public class MainMenuController {

    private final ScreenNavigator navigator;

    public MainMenuController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    /**
     * Builds a fresh game using the configured field size and player types.
     *
     * <p>When Extend Mode is disabled, only Player 1 is created. When it is
     * enabled, both players receive the same sequence seed so that they play
     * the same tetromino sequence.</p>
     */
    public void onPlay() {
        // Read when Play is pressed so settings changed this session apply.
        GameConfig config =
                ConfigService.getInstance().getConfig();

        long seed = System.nanoTime();

        List<GameController.Field> fields =
                new ArrayList<>();

        fields.add(
                createField(
                        config,
                        config.getPlayerOneType(),
                        true,
                        seed));

        if (config.isExtendMode()) {
            fields.add(
                    createField(
                            config,
                            config.getPlayerTwoType(),
                            false,
                            seed));
        }

        new GameController(
                navigator,
                fields).start();
    }

    private GameController.Field createField(
            GameConfig config,
            PlayerType type,
            boolean playerOne,
            long seed) {

        GameModel model = new GameModel(
                new Board(
                        config.getFieldWidth(),
                        config.getFieldHeight()),
                new SharedSequenceGenerator(seed));

        Player player =
                PlayerFactory.create(type);

        player.attach(model);

        InputHandler keys = null;

        if (type == PlayerType.HUMAN) {
            keys = new InputHandler(
                    playerOne
                            ? InputHandler.DEFAULT_KEYS
                            : InputHandler.PLAYER_TWO_KEYS);
        }

        return new GameController.Field(
                model,
                player,
                keys);
    }

    public void onConfigure() {
        navigator.show(
                new ConfigurationScreen(
                        new ConfigurationController(navigator)));
    }

    public void onHighScores() {
        navigator.show(
                new HighScoreScreen(
                        new HighScoreController(navigator)));
    }

    /** Asks for confirmation, then shuts down. */
    public void onExit() {
        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirm");
        alert.setHeaderText("Exit the game?");
        alert.initOwner(
                navigator.getStage());

        ButtonType yes =
                new ButtonType("Yes");

        alert.getButtonTypes().setAll(
                yes,
                ButtonType.CANCEL);

        alert.showAndWait().ifPresent(response -> {
            if (response == yes) {
                Platform.exit();
            }
        });
    }
}