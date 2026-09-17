package au.edu.Griffith.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ranking rules only — no files, no JavaFX. Persistence is {@code HighScoreService}'s job.
 */
class HighScoreTableTest {

    private HighScoreTable table;

    @BeforeEach
    void setUp() {
        table = new HighScoreTable();
    }

    @Test
    void emptyTableQualifiesAnyScore() {
        assertTrue(table.qualifies(0));
        assertTrue(table.qualifies(50));
    }

    @Test
    void addKeepsEntriesSortedHighestFirst() {
        table.add(entry("Sam", 200));
        table.add(entry("Alex", 500));
        table.add(entry("Riley", 300));

        List<ScoreEntry> entries = table.getEntries();
        assertEquals(List.of("Alex", "Riley", "Sam"), names(entries));
        assertEquals(List.of(500, 300, 200), scores(entries));
    }

    @Test
    void addTruncatesToMaxEntries() {
        for (int i = 1; i <= HighScoreTable.MAX_ENTRIES + 2; i++) {
            table.add(entry("P" + i, i * 100));
        }

        List<ScoreEntry> entries = table.getEntries();
        assertEquals(HighScoreTable.MAX_ENTRIES, entries.size());
        assertEquals(1200, entries.getFirst().score());
        assertEquals(300, entries.getLast().score());
    }

    @Test
    void fullTableOnlyQualifiesScoresThatBeatTheLowest() {
        fillWithTenScoresFrom100To1000();

        assertFalse(table.qualifies(100), "equal to the lowest does not beat it");
        assertFalse(table.qualifies(50));
        assertTrue(table.qualifies(101));
        assertTrue(table.qualifies(2000));
    }

    @Test
    void notFullTableStillQualifiesALowScore() {
        table.add(entry("Alex", 500));

        assertTrue(table.qualifies(1));
    }

    @Test
    void replaceAllSortsAndTruncates() {
        table.replaceAll(List.of(
                entry("Low", 10),
                entry("High", 90),
                entry("Mid", 50)));

        assertEquals(List.of("High", "Mid", "Low"), names(table.getEntries()));
    }

    @Test
    void replaceAllCapsAtMaxEntries() {
        List<ScoreEntry> overflow = new java.util.ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            overflow.add(entry("P" + i, i));
        }

        table.replaceAll(overflow);

        assertEquals(HighScoreTable.MAX_ENTRIES, table.getEntries().size());
        assertEquals(12, table.getEntries().getFirst().score());
        assertEquals(3, table.getEntries().getLast().score());
    }

    @Test
    void clearEmptiesTheTable() {
        table.add(entry("Alex", 100));
        table.clear();

        assertTrue(table.getEntries().isEmpty());
        assertTrue(table.qualifies(0));
    }

    @Test
    void entriesForFiltersByPlayerType() {
        table.add(new ScoreEntry("Human", 300, PlayerType.HUMAN));
        table.add(new ScoreEntry("Bot", 200, PlayerType.AI));
        table.add(new ScoreEntry("Net", 100, PlayerType.EXTERNAL));
        table.add(new ScoreEntry("Human2", 50, PlayerType.HUMAN));

        List<ScoreEntry> humans = table.entriesFor(PlayerType.HUMAN);
        assertEquals(List.of("Human", "Human2"), names(humans));
    }

    @Test
    void getEntriesIsUnmodifiableCopy() {
        table.add(entry("Alex", 100));
        List<ScoreEntry> snapshot = table.getEntries();

        table.add(entry("Sam", 200));

        assertEquals(1, snapshot.size());
        assertEquals("Alex", snapshot.getFirst().playerName());
    }

    private void fillWithTenScoresFrom100To1000() {
        for (int i = 1; i <= HighScoreTable.MAX_ENTRIES; i++) {
            table.add(entry("P" + i, i * 100));
        }
    }

    private static ScoreEntry entry(String name, int score) {
        return new ScoreEntry(name, score, PlayerType.HUMAN);
    }

    private static List<String> names(List<ScoreEntry> entries) {
        return entries.stream().map(ScoreEntry::playerName).toList();
    }

    private static List<Integer> scores(List<ScoreEntry> entries) {
        return entries.stream().map(ScoreEntry::score).toList();
    }
}
