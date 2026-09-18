package au.edu.Griffith;

import au.edu.Griffith.controller.MainMenuController;
import au.edu.Griffith.controller.ScreenNavigator;
import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.service.AudioManager;
import au.edu.Griffith.service.ConfigService;
import au.edu.Griffith.view.MainMenuScreen;
import au.edu.Griffith.view.SplashScreen;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Application entry point.
 *
 * <p>Deliberately small. Its whole job is composition: create the
 * {@link ScreenNavigator}, then show the splash screen and hand over to the
 * menu. Milestone 1's {@code Main} also built the menu, ran the splash animation
 * and decided what each button did — all of which now lives in the view and
 * controller layers.</p>
 *
 * <p>Wiring the objects together here, rather than letting each class construct
 * its own collaborators, is what keeps the dependencies pointing one way:
 * controllers receive the navigator, views receive their controller, and nothing
 * reaches back up.</p>
 */
public class TetrisApp extends Application {

    @Override
    public void start(Stage stage) {
        ScreenNavigator navigator = new ScreenNavigator(stage);

        GameConfig config = ConfigService.getInstance().getConfig();
        AudioManager audio = AudioManager.getInstance();
        audio.setMusicOn(config.isMusicOn());
        audio.setEffectsOn(config.isSoundEffectsOn());

        SplashScreen splash = new SplashScreen();
        splash.setOnFinished(() ->
                navigator.show(new MainMenuScreen(new MainMenuController(navigator))));

        navigator.show(splash);
    }

    @Override
    public void stop() {
        // TODO: AudioManager.getInstance().stopMusic() once playback is implemented.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
