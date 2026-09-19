package au.edu.Griffith.model;

/**
 * The settings a {@link ScoreEntry} was played under.
 *
 * <p>Saved with the score so the high-score table can show its Config column,
 * e.g. {@code "8x20(1) Human Single"}. Storing the numbers rather than the
 * formatted string keeps the data useful — scores can be filtered per field size
 * later — and leaves {@link #summary()} as the only place that decides how it
 * reads on screen.</p>
 *
 * @param fieldWidth  cells wide
 * @param fieldHeight cells tall
 * @param level       starting level of that game
 * @param playerType  who played the field this score came from
 * @param extendMode  true if this was a two-field Extend Mode game
 */
public record ScoreConfig(
        int fieldWidth,
        int fieldHeight,
        int level,
        PlayerType playerType,
        boolean extendMode) {

    /** What the Config column shows, e.g. {@code "8x20(1) Human Single"}. */
    public String summary() {
        return fieldWidth + "x" + fieldHeight
                + "(" + level + ") "
                + playerType.displayName()
                + " " + (extendMode ? "Extend" : "Single");
    }
}