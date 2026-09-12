package au.edu.Griffith.model;

/**
 * The running score for one field.
 *
 * <p>Information Expert (GRASP): the class that owns the score data is the class
 * that knows how to update it, so the scoring rule lives here rather than in the
 * controller or the view.</p>
 *
 * <p>The rule is Milestone 1's: a flat {@value #POINTS_PER_LINE} per line
 * cleared, regardless of how many came down at once.</p>
 */
public class Score {

    /** Points awarded per row removed. */
    public static final int POINTS_PER_LINE = 100;

    private int points;

    public int getPoints() {
        return points;
    }

    /**
     * Awards points for a line-clear event.
     *
     * @param linesCleared how many rows were removed at once
     * @return points added by this clear
     */
    public int addClearedLines(int linesCleared) {
        int awarded = linesCleared * POINTS_PER_LINE;
        points += awarded;
        return awarded;
    }

    /** Returns to zero for a replay. */
    public void reset() {
        points = 0;
    }
}
