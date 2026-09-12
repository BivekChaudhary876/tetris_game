package au.edu.Griffith.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;

/**
 * Opening screen showing the group and course details before the menu appears.
 *
 * <p>Carried over from Milestone 1's {@code Main}, but as a screen in its own
 * right rather than two private methods on the {@code Application} subclass.</p>
 *
 * <p>One behavioural change: the splash now runs on the main {@link javafx.stage.Stage}
 * rather than a second undecorated stage of its own. That is what lets it go
 * through {@link au.edu.Griffith.controller.ScreenNavigator} like every other
 * screen — the old version had to manage and close its own window, which is why
 * it needed the run-once guard to avoid handing over twice.</p>
 */
public class SplashScreen extends AbstractScreen {

    /** How long the splash holds before handing over. */
    public static final double HOLD_SECONDS = 2.5;

    private static final Duration FADE = Duration.millis(400);
    private static final String SPLASH_IMAGE = "/splash-image.png";

    private Runnable onFinished;
    private boolean handedOver;

    /** Sets the action to run when the splash finishes or is skipped. */
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    public String getTitle() {
        return "Tetris";
    }

    @Override
    protected Parent buildLayout() {
        StackPane layout = new StackPane();

        URL imageUrl = getClass().getResource(SPLASH_IMAGE);
        if (imageUrl != null) {
            ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm()));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(ScreenSizes.SPLASH_IMAGE_WIDTH);
            layout.getChildren().add(imageView);
        }

        Label loading = new Label("Loading...");
        loading.getStyleClass().add("value-label");
        StackPane.setAlignment(loading, Pos.BOTTOM_CENTER);
        layout.getChildren().add(loading);

        return layout;
    }

    @Override
    protected String rootStyleClass() {
        return "splash-screen";
    }

    @Override
    public void onShow() {
        Parent root = getRoot();

        // Click anywhere, or press ESC, to skip.
        root.setOnMouseClicked(event -> finish());
        if (root.getScene() != null) {
            root.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    finish();
                }
            });
        }

        FadeTransition fadeIn = new FadeTransition(FADE, root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(HOLD_SECONDS));

        FadeTransition fadeOut = new FadeTransition(FADE, root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        sequence.setOnFinished(event -> finish());
        sequence.play();
    }

    /**
     * Hands over to the menu, at most once.
     *
     * <p>The guard matters: the timeline finishing and the player clicking to
     * skip can both fire, and navigating twice would leave a half-faded root.</p>
     */
    private void finish() {
        if (handedOver) {
            return;
        }
        handedOver = true;

        // The fade-out leaves the node transparent; the next screen reuses it.
        getRoot().setOpacity(1);

        if (onFinished != null) {
            onFinished.run();
        }
    }
}
