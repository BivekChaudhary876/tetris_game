package au.edu.Griffith.model;

import java.util.Comparator;

/**
 * One row of the high-score table.
 *
 * <p>Implements {@link Comparable} so a natural ordering (highest score first)
 * is built in, while {@link #BY_NAME} offers an alternative {@link Comparator}
 * for name-sorted views. This pair — {@code Comparable} for the one obvious
 * order, {@code Comparator} for the rest — is the standard Java idiom.</p>
 *
 * @param playerName name typed in when the score was recorded
 * @param score      final score
 * @param playerType whether the score was set by a human, the AI or an external player
 */
public record ScoreEntry(String playerName, int score, PlayerType playerType)
        implements Comparable<ScoreEntry> {

    /** Alternative ordering: alphabetical by player name, case-insensitive. */
    public static final Comparator<ScoreEntry> BY_NAME =
            Comparator.comparing(ScoreEntry::playerName, String.CASE_INSENSITIVE_ORDER);

    /** Natural order is descending by score, so the best entry sorts first. */
    @Override
    public int compareTo(ScoreEntry other) {
        return Integer.compare(other.score, this.score);
    }
}
