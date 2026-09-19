package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreConfig;
import au.edu.Griffith.model.ScoreEntry;
import au.edu.Griffith.service.ConfigService;
import au.edu.Griffith.player.ExternalPlayer;
import au.edu.Griffith.player.HumanPlayer;
import au.edu.Griffith.player.Player;
import au.edu.Griffith.service.AudioManager;
import au.edu.Griffith.service.HighScoreService;
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

        public Field(
                GameModel model,
                Player player,
                InputHandler keys) {

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

    /**
     * Compatibility constructor for a single human-controlled field.
     */
    public GameController(
            ScreenNavigator navigator,
            GameModel model) {

        this(
                navigator,
                List.of(
                        new Field(
                                model,
                                new HumanPlayer(),
                                new InputHandler(
                                        InputHandler.DEFAULT_KEYS))));

        fields.getFirst().player.attach(model);
    }

    /**
     * Creates a controller for one or more playing fields.
     */
    public GameController(
            ScreenNavigator navigator,
            List<Field> fields) {

        this.navigator = navigator;
        this.fields = List.copyOf(fields);
    }

    public GameModel getModel() {
        return fields.getFirst().model;
    }

    /**
     * Builds the screen, starts every field, starts the music and starts
     * the game clock.
     */
    public void start() {
        List<GameModel> models = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<Runnable> replays = new ArrayList<>();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            models.add(field.model);
            titles.add(
                    fieldTitle(
                            i,
                            field.player.getType()));
            replays.add(fieldRestart(field));
        }

        screen = new GameScreen(
                models,
                titles,
                this::onBackToMenu,
                replays);

        navigator.show(screen);

        bindInput(screen.getRoot().getScene());

        for (Field field : fields) {
            field.model.start();
        }

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
                    if (field.model.getStatus()
                            == GameStatus.RUNNING) {

                        anyRunning = true;
                        break;
                    }
                }

                double elapsedMs =
                        (now - last) / 1_000_000.0;

                last = now;

                if (anyRunning) {
                    for (Field field : fields) {
                        if (field.model.getStatus()
                                == GameStatus.RUNNING) {

                            field.model.tick(elapsedMs);
                            field.player.update(elapsedMs);
                        }
                    }
                }

                for (Field field : fields) {
                    if (field.model.getStatus()
                            == GameStatus.GAME_OVER) {

                        offerHighScoreOnce(field);
                    }
                }

                refreshServerWarnings();
                screen.render();
            }
        };

        clock.start();
    }

    /**
     * Routes key presses to the appropriate field.
     *
     * <p>{@code P} toggles pause for all active fields. Other keys are
     * translated through each human player's configured key bindings.</p>
     */
    private void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            AudioManager audio =
                    AudioManager.getInstance();

            // Session-level keys, handled here rather than in InputHandler
            // because they act on the whole game, not on one piece.
            if (event.getCode() == KeyCode.S) {
                audio.setEffectsOn(!audio.isEffectsOn());
                screen.render();
                return;
            }

            if (event.getCode() == KeyCode.M) {
                audio.setMusicOn(!audio.isMusicOn());
                screen.render();
                return;
            }

            if (event.getCode() == KeyCode.P) {
                boolean shouldPause = false;

                for (Field field : fields) {
                    if (field.model.getStatus()
                            == GameStatus.RUNNING) {

                        shouldPause = true;
                        break;
                    }
                }

                for (Field field : fields) {
                    if (shouldPause
                            && field.model.getStatus()
                            == GameStatus.RUNNING) {

                        field.model.togglePause();

                    } else if (!shouldPause
                            && field.model.getStatus()
                            == GameStatus.PAUSED) {

                        field.model.togglePause();
                    }
                }

                if (shouldPause) {
                    audio.pauseMusic();
                } else {
                    audio.resumeMusic();
                }

                screen.render();
                return;
            }

            for (Field field : fields) {
                if (field.keys == null
                        || !(field.player instanceof HumanPlayer human)) {
                    continue;
                }

                CommandFactory.Action action =
                        field.keys.resolve(event.getCode());

                if (action != null) {
                    human.onAction(action);
                    audio.playEffect(
                            AudioManager.Effect.MOVE);
                }
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

    /**
     * Game-over path: prompts on the next pulse of the FX thread, because this is
     * called from inside the animation timer.
     */
    private void offerHighScoreOnce(Field field) {
        if (field.highScorePrompted) {
            return;
        }

        field.highScorePrompted = true;

        Platform.runLater(
                () -> promptForHighScore(field));
    }

    /**
     * Prompts for a name and saves the score if it makes the top ten.
     *
     * <p>{@link HighScoreService#record} writes {@code data/scores.json}
     * immediately, so a score survives even if the app is closed straight
     * after.</p>
     */
    private void promptForHighScore(Field field) {
        int points =
                field.model.getScore().getPoints();

        if (!HighScoreService.getInstance()
                .qualifies(points)) {

            return;
        }

        TextInputDialog dialog =
                new TextInputDialog();

        dialog.setTitle("High Score");
        dialog.setHeaderText(
                "Score: " + points + " - you made the top 10!");
        dialog.setContentText(
                "Enter your name:");

        dialog.initOwner(
                navigator.getStage());

        dialog.showAndWait()
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .ifPresent(name ->
                        HighScoreService
                                .getInstance()
                                .record(
                                        new ScoreEntry(
                                                name,
                                                points,
                                                configOf(field))));
    }

    /**
     * Stops the clock, stops music and disposes all players.
     */
    public void stop() {
        if (clock != null) {
            clock.stop();
        }

        AudioManager.getInstance()
                .stopMusic();

        for (Field field : fields) {
            field.player.dispose();
        }
    }

    /**
     * Confirms with the player, then returns to the menu.
     *
     * <p>Running fields are paused while the confirmation dialog is open.
     * Cancelling restores only the fields that were running before the
     * dialog appeared.</p>
     *
     * <p>On confirm, each field is offered the high-score prompt before we
     * navigate away. Without this a player who quits part-way through loses a
     * qualifying score, because the game-over prompt never fires.</p>
     */
    public void onBackToMenu() {
        List<GameStatus> before =
                new ArrayList<>();

        boolean musicWasRunning = false;

        for (Field field : fields) {
            GameStatus status =
                    field.model.getStatus();

            before.add(status);

            if (status == GameStatus.RUNNING) {
                field.model.togglePause();
                musicWasRunning = true;
            }
        }

        if (musicWasRunning) {
            AudioManager.getInstance()
                    .pauseMusic();
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION);

        alert.setTitle("Confirm");
        alert.setHeaderText(
                "Return to Main Menu?");
        alert.setContentText(
                "Cancel to resume game");

        alert.initOwner(
                navigator.getStage());

        ButtonType yes =
                new ButtonType("Yes");

        alert.getButtonTypes().setAll(
                yes,
                ButtonType.CANCEL);

        alert.showAndWait().ifPresent(response -> {

            if (response == yes) {

                stop();

                // Save whatever was earned before leaving. Already on the FX
                // thread and the clock is stopped, so these can run directly.
                for (Field field : fields) {
                    if (!field.highScorePrompted) {
                        field.highScorePrompted = true;
                        promptForHighScore(field);
                    }
                }

                navigator.show(
                        new MainMenuScreen(
                                new MainMenuController(
                                        navigator)));

            } else {

                boolean resumeMusic = false;

                for (int i = 0;
                     i < fields.size();
                     i++) {

                    if (before.get(i)
                            == GameStatus.RUNNING
                            && fields.get(i).model
                            .getStatus()
                            == GameStatus.PAUSED) {

                        fields.get(i).model.togglePause();
                        resumeMusic = true;
                    }
                }

                if (resumeMusic) {
                    AudioManager.getInstance()
                            .resumeMusic();
                }
            }
        });
    }

    private static String fieldTitle(
            int index,
            PlayerType type) {

        return "Player "
                + (index + 1)
                + " ("
                + type.displayName()
                + ")";
    }

    /** The settings this field played under, saved beside its score. */
    private ScoreConfig configOf(Field field) {
        return new ScoreConfig(
                field.model.getBoard().getWidth(),
                field.model.getBoard().getHeight(),
                ConfigService.getInstance().getConfig().getStartingLevel(),
                field.player.getType(),
                fields.size() > 1);
    }

    /**
     * Keeps each external field's warning banner in step with its connection.
     *
     * <p>Polled from the clock rather than driven by an event, because the client
     * connects and drops on its own background thread. That is what makes the
     * banner clear by itself when the server is started mid-game.</p>
     */
    private void refreshServerWarnings() {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).player instanceof ExternalPlayer external) {
                screen.setServerWarningVisible(i, !external.isConnected());
            }
        }
    }
}