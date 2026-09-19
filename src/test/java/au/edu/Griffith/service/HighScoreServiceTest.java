package au.edu.Griffith.service;

import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreBoard;
import au.edu.Griffith.model.ScoreConfig;
import au.edu.Griffith.model.ScoreEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Persistence for the high-score table, against a temp file. */
class HighScoreServiceTest {

    @TempDir
    Path tempDir;

    private Path scoresFile;
    private HighScoreService service;

    @BeforeEach
    void setUp() {
        scoresFile = tempDir.resolve("JavaTetrisScore.json");
        service = newService();
    }

    @Test
    void firstLoadStartsEmpty() {
        // No file yet, and nothing invented to fill it.
        assertTrue(service.getTable().getEntries().isEmpty());
        assertFalse(Files.exists(scoresFile));
    }

    @Test
    void recordWritesTheFileAndSurvivesReload() {
        service.record(entry("Tasman", 1500, PlayerType.HUMAN));
        assertTrue(Files.exists(scoresFile));

        List<ScoreEntry> entries = newService().getTable().getEntries();
        assertEquals(1, entries.size());
        assertEquals("Tasman", entries.getFirst().playerName());
        assertEquals(1500, entries.getFirst().score());
    }

    @Test
    void recordKeepsEntriesSortedAcrossReload() {
        service.record(entry("Low", 100, PlayerType.HUMAN));
        service.record(entry("High", 900, PlayerType.AI));
        service.record(entry("Mid", 500, PlayerType.EXTERNAL));

        List<ScoreEntry> entries = newService().getTable().getEntries();
        assertEquals(List.of("High", "Mid", "Low"),
                entries.stream().map(ScoreEntry::playerName).toList());
    }

    @Test
    void configIsPersistedWithTheScore() {
        service.record(new ScoreEntry("Bivek", 800,
                new ScoreConfig(8, 20, 3, PlayerType.AI, true)));

        ScoreEntry reloaded = newService().getTable().getEntries().getFirst();
        assertEquals(8, reloaded.config().fieldWidth());
        assertEquals(3, reloaded.config().level());
        assertEquals(PlayerType.AI, reloaded.config().playerType());
        assertTrue(reloaded.config().extendMode());
    }

    @Test
    void fileUsesTheScoresWrapperAndNameKey() throws Exception {
        service.record(entry("Alex", 1200, PlayerType.HUMAN));
        String json = Files.readString(scoresFile);

        // The reference build's format.
        assertTrue(json.contains("\"scores\""), json);
        assertTrue(json.contains("\"name\""), json);
        assertTrue(json.contains("\"config\""), json);
        assertFalse(json.contains("\"playerName\""), json);
    }

    @Test
    void qualifiesDelegatesToTheTable() {
        for (int i = 1; i <= 10; i++) {
            service.record(entry("P" + i, i * 100, PlayerType.HUMAN));
        }
        assertTrue(service.qualifies(101));
        assertFalse(service.qualifies(100));
        assertFalse(service.qualifies(50));
    }

    @Test
    void clearAllPersistsAnEmptyTable() {
        service.record(entry("Alex", 500, PlayerType.HUMAN));
        service.clearAll();

        assertTrue(service.getTable().getEntries().isEmpty());
        assertTrue(newService().getTable().getEntries().isEmpty());
    }

    private HighScoreService newService() {
        return new HighScoreService(
                new JsonRepository<ScoreBoard>(scoresFile, new TypeReference<>() {
                }));
    }

    private static ScoreEntry entry(String name, int score, PlayerType type) {
        return new ScoreEntry(name, score, new ScoreConfig(10, 20, 1, type, false));
    }
}