package au.edu.Griffith.legacy;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;

public class Main extends Application {

    // How long the loading image stays on screen before the menu appears
    private static final Duration SPLASH_HOLD = Duration.seconds(2.5);

    // Lives in src/main/resources/ - leading slash means "root of the classpath"
    private static final String SPLASH_IMAGE_NAME = "/splash-image.png";

    @Override
    public void start(Stage initialStage) {
        // Start with splash screen
        showSplashScreen(initialStage, () -> showMainMenu(initialStage));
    }

    //  SPLASH SCREEN — image if present, otherwise fall back to the video
    private void showSplashScreen(Stage mainStage, Runnable onFinished) {

        URL imageUrl = getClass().getResource(SPLASH_IMAGE_NAME);
        if (imageUrl != null) {
            showImageSplash(imageUrl, onFinished);
        } else {
            showVideoSplash(mainStage, onFinished);
        }
    }

    //  IMAGE SPLASH
    private void showImageSplash(URL imageUrl, Runnable onFinished) {

        Stage splashStage = new Stage(StageStyle.UNDECORATED);

        Image image = new Image(imageUrl.toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);

        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        StackPane.setAlignment(loadingLabel, Pos.BOTTOM_CENTER);

        StackPane splashLayout = new StackPane(imageView, loadingLabel);
        splashLayout.setStyle("-fx-background-color: black;");

        Scene splashScene = new Scene(splashLayout);

        // Make sure the splash only ever hands over to the menu once
        Runnable finishOnce = new Runnable() {
            private boolean done = false;

            @Override
            public void run() {
                if (done) {
                    return;
                }
                done = true;
                splashStage.close();
                onFinished.run();
            }
        };

        // Click or ESC to skip
        splashScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                finishOnce.run();
            }
        });
        splashLayout.setOnMouseClicked(event -> finishOnce.run());

        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), splashLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(SPLASH_HOLD);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), splashLayout);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition splashSequence = new SequentialTransition(fadeIn, hold, fadeOut);
        splashSequence.setOnFinished(event -> finishOnce.run());
        splashSequence.play();
    }

    //  VIDEO SPLASH
    private void showVideoSplash(Stage mainStage, Runnable onFinished) {

        Stage splashStage = new Stage(StageStyle.UNDECORATED);

        URL mediaUrl = getClass().getResource("assets/splash_vid.mp4");
        if (mediaUrl == null) {
            System.err.println("No splash image or video found - skipping splash screen.");
            onFinished.run();
            return;
        }

        Media media = new Media(mediaUrl.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        mediaPlayer.setOnReady(() -> {

            Label skipLabel = new Label("Press ESC to skip");

            StackPane splashLayout = new StackPane(mediaView, skipLabel);
            Scene splashScene = new Scene(splashLayout);

            // ESC key to skip
            splashScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    mediaPlayer.stop();
                    splashStage.close();
                    onFinished.run();
                }
            });

            splashStage.setScene(splashScene);
            splashStage.show();

            // Auto-finish when video ends
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.stop();
                splashStage.close();
                onFinished.run();
            });

            mediaPlayer.play();
        });
    }

    //  MAIN MENU
    public void showMainMenu(Stage stage) {

        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);

        Button playButton = new Button("Play Tetris");
        Button highScoresButton = new Button("High Scores");
        Button configButton = new Button("Configuration");
        Button exitButton = new Button("Exit");

        playButton.setOnAction(event -> {
            Tetris tetris = new Tetris();
            Scene tetrisScene = tetris.createTetrisScene(stage);
            stage.setScene(tetrisScene);
        });

        highScoresButton.setOnAction(event -> {
            HighScores highScores = new HighScores();
            Scene highScoresScene = highScores.createHighScoresScene(stage);
            stage.setScene(highScoresScene);
        });
        configButton.setOnAction(event -> {
            Configuration configuration = new Configuration();
            Scene configurationScene = configuration.createConfigurationScene(stage);
            stage.setScene(configurationScene);
        });

        //Confirmation before Application Exit
        exitButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm");
            alert.setHeaderText("Exit the game?");

            ButtonType yes = new ButtonType("Yes");
            ButtonType cancel = ButtonType.CANCEL;

            alert.getButtonTypes().setAll(yes, cancel);
            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    //exit application
                    javafx.application.Platform.exit();
                }
            });
        });

        menuLayout.getChildren().addAll(
                playButton,
                highScoresButton,
                configButton,
                exitButton
        );

        Scene mainScene = new Scene(menuLayout, 800, 700);
        stage.setScene(mainScene);
        stage.setTitle("Main Menu");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
