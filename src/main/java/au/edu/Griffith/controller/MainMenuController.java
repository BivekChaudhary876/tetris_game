package au.edu.Griffith.controller;

import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.tetromino.SharedSequenceGenerator;
import au.edu.Griffith.service.ConfigService;
import au.edu.Griffith.view.ConfigurationScreen;
import au.edu.Griffith.view.HighScoreScreen;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Handles what the main-menu buttons mean.
 *
 * <p>Controller (GRASP): a use-case handler that receives the user's intent from
 * the view, decides what should happen and delegates to the model and other
 * controllers. It holds no UI nodes, so it can be tested by calling its methods
 * directly.</p>
 */
public class MainMenuController {

    private final ScreenNavigator navigator;

    public MainMenuController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    /** Builds a fresh game and hands control to a {@link GameController}. */
    public void onPlay() {
        GameConfig config = ConfigService.getInstance().getConfig();
        GameModel model = new GameModel(
                new Board(config.getFieldWidth(), config.getFieldHeight()),
                new SharedSequenceGenerator());

        new GameController(navigator, model).start();
    }

    public void onConfigure() {
        navigator.show(new ConfigurationScreen(new ConfigurationController(navigator)));
    }

    public void onHighScores() {
        navigator.show(new HighScoreScreen(new HighScoreController(navigator)));
    }

    /** Asks for confirmation, then shuts down. */
    public void onExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Exit the game?");
        alert.initOwner(navigator.getStage());

        ButtonType yes = new ButtonType("Yes");
        alert.getButtonTypes().setAll(yes, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(response -> {
            if (response == yes) {
                Platform.exit();
            }
        });
    }
}
