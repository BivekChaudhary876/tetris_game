package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.service.AudioManager;
import au.edu.Griffith.view.GameScreen;
import au.edu.Griffith.view.MainMenuScreen;
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

    /** Builds the screen, starts the game, the music and the clock. */
    public void start() {
        screen = new GameScreen(model, this::onBackToMenu, this::restart);
        navigator.show(screen);

        bindInput(screen.getRoot().getScene());
        model.start();
        AudioManager.getInstance().startMusic();
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
     *
     * <p>The move sound fires here rather than on the model's {@code PIECE_MOVED}
     * event, because that event also fires on every gravity step and would give a
     * constant tick rather than a response to the player.</p>
     */
    private void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            AudioManager audio = AudioManager.getInstance();

            if (event.getCode() == KeyCode.P) {
                model.togglePause();
                if (model.getStatus() == GameStatus.PAUSED) {
                    audio.pauseMusic();
                } else {
                    audio.resumeMusic();
                }
                return;
            }

            CommandFactory.Action action = inputHandler.resolve(event.getCode());
            if (action != null && acceptsInput()) {
                commands.create(action).execute();
                audio.playEffect(AudioManager.Effect.MOVE);
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

    /** Stops the clock and the music. */
    public void stop() {
        if (clock != null) {
            clock.stop();
        }
        AudioManager.getInstance().stopMusic();
    }

    /**
     * Confirms with the player, then returns to the menu.
     *
     * <p>Pauses while the dialog is open and restores the previous state on
     * Cancel, as Milestone 1 did. The music follows the same path, so it does not
     * play on over a frozen game.</p>
     */
    public void onBackToMenu() {
        GameStatus statusBeforeDialog = model.getStatus();
        boolean wasRunning = statusBeforeDialog == GameStatus.RUNNING;

        if (wasRunning) {
            model.togglePause();
            AudioManager.getInstance().pauseMusic();
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
                navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
            } else if (wasRunning) {
                // Cancel: put the game and the music back as they were.
                model.togglePause();
                AudioManager.getInstance().resumeMusic();
            }
        });
    }
}