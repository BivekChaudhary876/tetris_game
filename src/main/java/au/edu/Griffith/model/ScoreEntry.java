package au.edu.Griffith.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;

/**
 * One row of the high-score table.
 *
 * <p>Implements {@link Comparable} so a natural ordering (highest score first)
 * is built in, while {@link #BY_NAME} offers an alternative {@link Comparator}
 * for name-sorted views. This pair — {@code Comparable} for the one obvious
 * order, {@code Comparator} for the rest — is the standard Java idiom.</p>
 *
 * <p>The JSON key is {@code name}, not {@code playerName}, so the saved file
 * matches the reference build's format.</p>
 *
 * @param playerName name typed in when the score was recorded
 * @param score      final score
 * @param config     the settings that game was played under
 */
public record ScoreEntry(
        @JsonProperty("name") String playerName,
        @JsonProperty("score") int score,
        @JsonProperty("config") ScoreConfig config)
        implements Comparable<ScoreEntry> {

    /** Alternative ordering: alphabetical by player name, case-insensitive. */
    public static final Comparator<ScoreEntry> BY_NAME =
            Comparator.comparing(ScoreEntry::playerName, String.CASE_INSENSITIVE_ORDER);

    /**
     * Who set this score. Kept so existing callers — {@link HighScoreTable#entriesFor}
     * and the high-score screen — keep working unchanged.
     *
     * <p>{@code @JsonIgnore} because it is derived from {@link #config}, not a
     * field of its own; without it Jackson may write a duplicate
     * {@code playerType} key beside {@code config}.</p>
     */
    @JsonIgnore
    public PlayerType playerType() {
        return config.playerType();
    }

    /** Natural order is descending by score, so the best entry sorts first. */
    @Override
    public int compareTo(ScoreEntry other) {
        return Integer.compare(other.score, this.score);
    }
}