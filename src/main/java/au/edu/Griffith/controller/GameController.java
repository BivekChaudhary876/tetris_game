package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreEntry;
import au.edu.Griffith.service.HighScoreService;
import au.edu.Griffith.view.GameScreen;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;

/**
 * Drives a play session: owns the clock and routes input.
 *
 * <p>The <strong>Controller</strong> of MVC, and the class that replaces the
 * middle of Milestone 1's {@code Tetris}. It is the only place that holds both a
 * {@link GameModel} and a {@link GameScreen}; the model does not know the screen
 * exists and the screen reaches the model only through its Observer
 * subscription.</p>
 */
public class GameController {

    private final ScreenNavigator navigator;
    private final GameModel model;
    private final InputHandler inputHandler = new InputHandler(InputHandler.DEFAULT_KEYS);
    private final CommandFactory commands;

    private GameScreen screen;
    private AnimationTimer clock;
    private boolean highScorePrompted;

    public GameController(ScreenNavigator navigator, GameModel model) {
        this.navigator = navigator;
        this.model = model;
        this.commands = new CommandFactory(model);
    }

    public GameModel getModel() {
        return model;
    }

    /** Builds the screen, starts the game and starts the clock. */
    public void start() {
        screen = new GameScreen(model, this::onBackToMenu, this::restart);
        navigator.show(screen);

        bindInput(screen.getRoot().getScene());
        model.start();
        startClock();
    }

    /**
     * The per-frame tick.
     *
     * <p>Ported from Milestone 1's {@code AnimationTimer}, including resetting the
     * timestamp while paused so that un-pausing does not hand the model one huge
     * elapsed time and drop the piece several rows at once.</p>
     */
    private void startClock() {
        clock = new AnimationTimer() {
            private long last;

            @Override
            public void handle(long now) {
                if (model.getStatus() == GameStatus.PAUSED || last == 0) {
                    last = now;
                    return;
                }

                double elapsedMs = (now - last) / 1_000_000.0;
                last = now;

                model.tick(elapsedMs);
                screen.render();
                if (model.getStatus() == GameStatus.GAME_OVER) {
                    offerHighScoreOnce();
                }
            }
        };
        clock.start();
    }

    /**
     * Routes a key press: {@code P} toggles pause, the rest become movement
     * commands when the current state accepts input.
     */
    private void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.P) {
                model.togglePause();
                return;
            }

            CommandFactory.Action action = inputHandler.resolve(event.getCode());
            if (action != null && acceptsInput()) {
                commands.create(action).execute();
                screen.render();
            }
        });
    }

    private boolean acceptsInput() {
        return model.getStatus() == GameStatus.RUNNING;
    }

    /** Restarts the field with a fresh piece sequence, behind the Replay button. */
    public void restart() {
        highScorePrompted = false;
        model.restart();
        screen.render();
    }

    /**
     * Once per finished game: if the score earns a top-ten place, ask for a name
     * and persist it. Cancel or a blank name skips the record.
     */
    private void offerHighScoreOnce() {
        if (highScorePrompted) {
            return;
        }
        highScorePrompted = true;
        Platform.runLater(this::promptForHighScore);
    }

    private void promptForHighScore() {
        int points = model.getScore().getPoints();
        if (!HighScoreService.getInstance().qualifies(points)) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("High Score");
        dialog.setHeaderText("Score: " + points + " — you made the top 10!");
        dialog.setContentText("Enter your name:");
        dialog.initOwner(navigator.getStage());

        dialog.showAndWait()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .ifPresent(name -> HighScoreService.getInstance()
                        .record(new ScoreEntry(name, points, PlayerType.HUMAN)));
    }

    /** Stops the clock and restores the default window size. */
    public void stop() {
        if (clock != null) {
            clock.stop();
        }
    }

    /**
     * Confirms with the player, then returns to the menu.
     *
     * <p>Pauses while the dialog is open and restores the previous state on
     * Cancel, as Milestone 1 did.</p>
     */
    public void onBackToMenu() {
        GameStatus statusBeforeDialog = model.getStatus();
        if (statusBeforeDialog == GameStatus.RUNNING) {
            model.togglePause();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Return to Main Menu?");
        alert.setContentText("Cancel to resume game");
        alert.initOwner(navigator.getStage());

        ButtonType yes = new ButtonType("Yes");
        alert.getButtonTypes().setAll(yes, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(response -> {
            if (response == yes) {
                stop();
                navigator.show(new au.edu.Griffith.view.MainMenuScreen(new MainMenuController(navigator)));
            } else if (statusBeforeDialog == GameStatus.RUNNING) {
                model.togglePause();
            }
        });
    }
}
