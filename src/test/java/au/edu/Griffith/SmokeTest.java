package au.edu.Griffith;

import au.edu.Griffith.controller.MainMenuController;
import au.edu.Griffith.controller.ScreenNavigator;
import au.edu.Griffith.view.ConfigurationScreen;
import au.edu.Griffith.view.HighScoreScreen;
import au.edu.Griffith.view.MainMenuScreen;
import au.edu.Griffith.controller.ConfigurationController;
import au.edu.Griffith.controller.HighScoreController;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots the real JavaFX toolkit and walks every screen transition the menu can
 * trigger.
 *
 * <p>The unit tests cover the game rules with no UI at all. This one covers the
 * opposite risk: that the rules are right but the JavaFX wiring throws on the
 * way to showing them. It clicks nothing, but it exercises the same controller
 * methods the buttons are bound to.</p>
 */
class SmokeTest {

    @Test
    void everyScreenBuildsAndNavigatesWithoutThrowing() throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        startToolkit();

        Platform.runLater(() -> {
            try {
                ScreenNavigator navigator = new ScreenNavigator(new Stage());
                MainMenuController menu = new MainMenuController(navigator);

                navigator.show(new MainMenuScreen(menu));
                navigator.show(new ConfigurationScreen(new ConfigurationController(navigator)));
                navigator.show(new HighScoreScreen(new HighScoreController(navigator)));
                navigator.show(new MainMenuScreen(menu));

                // The Play button path: builds the model, screen, clock and input binding.
                menu.onPlay();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(30, TimeUnit.SECONDS), "JavaFX thread did not finish in time");
        assertNull(failure.get(), () -> "screen navigation threw: " + failure.get());
    }

    /** Starts the toolkit once; a second call would throw. */
    private static void startToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyRunning) {
            started.countDown();
        }
        assertTrue(started.await(30, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }
}
