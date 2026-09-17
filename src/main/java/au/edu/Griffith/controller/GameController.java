package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.GameStatus;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreEntry;
import au.edu.Griffith.player.HumanPlayer;
import au.edu.Griffith.player.Player;
import au.edu.Griffith.service.HighScoreService;
import au.edu.Griffith.view.GameScreen;
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

    /** Builds the screen, starts every field and starts the clock. */
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
        for (Field field : fields) {
            field.model.start();
        }
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

    private void bindInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.P) {
                for (Field field : fields) {
                    field.model.togglePause();
                }
                return;
            }

            for (Field field : fields) {
                if (field.keys == null || !(field.player instanceof HumanPlayer human)) {
                    continue;
                }
                CommandFactory.Action action = field.keys.resolve(event.getCode());
                if (action != null) {
                    human.onAction(action);
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

    private void offerHighScoreOnce(Field field) {
        if (field.highScorePrompted) {
            return;
        }
        field.highScorePrompted = true;
        Platform.runLater(() -> promptForHighScore(field));
    }

    private void promptForHighScore(Field field) {
        int points = field.model.getScore().getPoints();
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
                        .record(new ScoreEntry(name, points, field.player.getType())));
    }

    public void stop() {
        if (clock != null) {
            clock.stop();
        }
        for (Field field : fields) {
            field.player.dispose();
        }
    }

    public void onBackToMenu() {
        List<GameStatus> before = new ArrayList<>();
        for (Field field : fields) {
            before.add(field.model.getStatus());
            if (field.model.getStatus() == GameStatus.RUNNING) {
                field.model.togglePause();
            }
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
            } else {
                for (int i = 0; i < fields.size(); i++) {
                    if (before.get(i) == GameStatus.RUNNING
                            && fields.get(i).model.getStatus() == GameStatus.PAUSED) {
                        fields.get(i).model.togglePause();
                    }
                }
            }
        });
    }

    private static String fieldTitle(int index, PlayerType type) {
        return "Player " + (index + 1) + " (" + type.displayName() + ")";
    }
}
