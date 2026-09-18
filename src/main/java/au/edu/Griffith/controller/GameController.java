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

import java.util.ArrayList;
import java.util.List;

/**
 * Drives a play session: owns the clock and routes input for one or two fields.
 */
public class GameController {

    /** One board in the session, with the player that drives it. */
    public static final class Field {
        private final GameModel model;
        private final Player player;
        private final InputHandler keys;
        private boolean highScorePrompted;

        public Field(GameModel model, Player player, InputHandler keys) {
            this.model = model;
            this.player = player;
            this.keys = keys;
        }

        public GameModel getModel() {
            return model;
        }

        public Player getPlayer() {
            return player;
        }
    }

    private final ScreenNavigator navigator;
    private final List<Field> fields;

    private GameScreen screen;
    private AnimationTimer clock;

    public GameController(ScreenNavigator navigator, GameModel model) {
        this(navigator, List.of(new Field(model, new HumanPlayer(), new InputHandler(InputHandler.DEFAULT_KEYS))));
        fields.getFirst().player.attach(model);
    }

    public GameController(ScreenNavigator navigator, List<Field> fields) {
        this.navigator = navigator;
        this.fields = List.copyOf(fields);
    }

    public GameModel getModel() {
        return fields.getFirst().model;
    }

    /** Builds the screen, starts the game, the music and the clock. */
    public void start() {
        List<GameModel> models = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Runnable> replays = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            models.add(field.model);
            titles.add(fieldTitle(i, field.player.getType()));
            replays.add(fieldRestart(field));
        }

        screen = new GameScreen(models, titles, this::onBackToMenu, replays);
        navigator.show(screen);

        bindInput(screen.getRoot().getScene());
        model.start();
        AudioManager.getInstance().startMusic();
        startClock();
    }

    private void startClock() {
        clock = new AnimationTimer() {
            private long last;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }

                boolean anyRunning = false;
                for (Field field : fields) {
                    if (field.model.getStatus() == GameStatus.RUNNING) {
                        anyRunning = true;
                        break;
                    }
                }

                double elapsedMs = (now - last) / 1_000_000.0;
                last = now;

                if (anyRunning) {
                    for (Field field : fields) {
                        if (field.model.getStatus() == GameStatus.RUNNING) {
                            field.model.tick(elapsedMs);
                            field.player.update(elapsedMs);
                        }
                    }
                }

                for (Field field : fields) {
                    if (field.model.getStatus() == GameStatus.GAME_OVER) {
                        offerHighScoreOnce(field);
                    }
                }
                screen.render();
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
            screen.render();
        });
    }

    private Runnable fieldRestart(Field field) {
        return () -> {
            field.highScorePrompted = false;
            field.model.restart();
            screen.render();
        };
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
