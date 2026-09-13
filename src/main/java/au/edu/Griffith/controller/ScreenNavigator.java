package au.edu.Griffith.controller;

import au.edu.Griffith.view.ScreenView;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Facade over the JavaFX {@link Stage}: the one class that knows how to put a
 * screen on screen.
 *
 * <p>Facade pattern. In Milestone 1 every screen created the next one and called
 * {@code stage.setScene(...)} itself, so {@code Configuration} imported
 * {@code Main}, {@code HighScores} imported {@code Main}, and {@code Tetris}
 * imported {@code Main} — a cycle of mutual dependencies with no single owner of
 * navigation. Routing every transition through here collapses that into a star:
 * screens depend on the navigator, and the navigator depends on screens.</p>
 */
public class ScreenNavigator {

    private final Stage stage;
    private ScreenView current;

    public ScreenNavigator(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public ScreenView getCurrent() {
        return current;
    }

    /**
     * Replaces the current screen.
     *
     * <p>Calls {@code onHide()} on the outgoing screen before {@code onShow()} on
     * the incoming one, which is what guarantees the game screen unsubscribes
     * from its model rather than leaking as a stale observer.</p>
     *
     * <p>A fresh {@link Scene} is built per screen so each one gets the size it
     * asks for — the playfield is narrower than the menu, exactly as in
     * Milestone 1.</p>
     */
    public void show(ScreenView screen) {
        if (current != null) {
            current.onHide();
        }
        current = screen;

        stage.setScene(new Scene(screen.getRoot(), screen.getPrefWidth(), screen.getPrefHeight()));
        stage.setTitle(screen.getTitle());

        if (!stage.isShowing()) {
            stage.show();
        }

        screen.onShow();
        stage.centerOnScreen();
    }
}
