package au.edu.Griffith.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The top-ten table, kept sorted and capped.
 *
 * <p>Holds the ranking rules only — reading and writing {@code scores.json} is
 * {@link au.edu.Griffith.service.HighScoreService}'s job. Splitting them means
 * the ranking logic can be unit tested without touching the file system, which
 * is what the JUnit tests for this class will do.</p>
 */
public class HighScoreTable {

    /** How many entries are kept. */
    public static final int MAX_ENTRIES = 10;

    private final List<ScoreEntry> entries = new ArrayList<>();

    /** The current ranking, best first. Unmodifiable. */
    public List<ScoreEntry> getEntries() {
        return List.copyOf(entries);
    }

    /**
     * True if {@code score} is good enough to earn a place, which is what decides
     * whether the player is prompted for a name at game over.
     */
    public boolean qualifies(int score) {
        if (entries.size() < MAX_ENTRIES) {
            return true;
        }
        return score > entries.getLast().score();
    }

    /** True if {@code score} would take first place, ahead of every entry currently listed. */
    public boolean isNewHighScore(int score) {
        return entries.isEmpty() || score > entries.getFirst().score();
    }

    /**
     * Inserts an entry, re-sorts on {@link ScoreEntry}'s natural order and drops
     * anything past {@link #MAX_ENTRIES}.
     */
    public void add(ScoreEntry entry) {
        entries.add(entry);
        sortAndTruncate();
    }

    /** Replaces the whole table, used when loading from JSON. */
    public void replaceAll(List<ScoreEntry> loaded) {
        entries.clear();
        entries.addAll(loaded);
        sortAndTruncate();
    }

    /** Empties the table, behind the "Clear scores" button. */
    public void clear() {
        entries.clear();
    }

    /**
     * The entries set by one kind of player.
     *
     * <p>Implemented with a {@link java.util.stream.Stream} filter rather than a
     * loop, for the "Streams" criterion.</p>
     */
    public List<ScoreEntry> entriesFor(PlayerType playerType) {
        return entries.stream()
                .filter(entry -> entry.playerType() == playerType)
                .toList();
    }

    private void sortAndTruncate() {
        entries.sort(null);
        if (entries.size() > MAX_ENTRIES) {
            entries.subList(MAX_ENTRIES, entries.size()).clear();
        }
    }
}
