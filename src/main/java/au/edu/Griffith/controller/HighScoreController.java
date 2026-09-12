package au.edu.Griffith.controller;

import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreEntry;
import au.edu.Griffith.view.MainMenuScreen;

import java.util.List;

/**
 * Supplies the high-score screen with its rows.
 *
 * <p>The entries are the fixed placeholder list from Milestone 1. Recording real
 * results and persisting them is a later milestone requirement;
 * {@link au.edu.Griffith.service.HighScoreService} is the seam it will be built
 * on.</p>
 */
public class HighScoreController {

    /** The placeholder table shown in Milestone 1. */
    private static final List<ScoreEntry> PLACEHOLDER_SCORES = List.of(
            new ScoreEntry("Alex", 12000, PlayerType.HUMAN),
            new ScoreEntry("Sam", 9800, PlayerType.HUMAN),
            new ScoreEntry("Jordan", 7600, PlayerType.HUMAN),
            new ScoreEntry("Riley", 5400, PlayerType.HUMAN),
            new ScoreEntry("Casey", 3200, PlayerType.HUMAN),
            new ScoreEntry("Morgan", 1500, PlayerType.HUMAN));

    private final ScreenNavigator navigator;

    public HighScoreController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    /** The table, best first. */
    public List<ScoreEntry> getEntries() {
        return PLACEHOLDER_SCORES;
    }

    public void onBack() {
        navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
    }
}
