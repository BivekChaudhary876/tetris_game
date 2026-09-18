package au.edu.Griffith.controller;

import au.edu.Griffith.model.ScoreEntry;
import au.edu.Griffith.service.HighScoreService;
import au.edu.Griffith.view.HighScoreScreen;
import au.edu.Griffith.view.MainMenuScreen;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.List;

/**
 * Supplies the high-score screen with its rows, and handles clear.
 *
 * <p>Rows come from {@link HighScoreService} — the same table a finished game
 * writes into — so the screen shows persisted scores, not placeholders.</p>
 */
public class HighScoreController {

    private final ScreenNavigator navigator;

    public HighScoreController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    /** The table, best first. */
    public List<ScoreEntry> getEntries() {
        return HighScoreService.getInstance().getTable().getEntries();
    }

    public void onBack() {
        navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
    }

    /** Confirms, then empties the persisted table and rebuilds this screen. */
    public void onClear() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText("Clear all high scores?");
        alert.initOwner(navigator.getStage());

        ButtonType yes = new ButtonType("Yes");
        alert.getButtonTypes().setAll(yes, ButtonType.CANCEL);

        alert.showAndWait().ifPresent(response -> {
            if (response == yes) {
                HighScoreService.getInstance().clearAll();
                navigator.show(new HighScoreScreen(this));
            }
        });
    }
}
