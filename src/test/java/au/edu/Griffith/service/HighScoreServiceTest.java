package au.edu.Griffith.service;

import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighScoreServiceTest {

    @TempDir
    Path tempDir;

    private Path scoresFile;
    private HighScoreService service;

    @BeforeEach
    void setUp() {
        scoresFile = tempDir.resolve("scores.json");
        service = newService();
    }

    @Test
    void firstLoadSeedsTenEntriesAndCreatesTheFile() {
        List<ScoreEntry> entries = service.getTable().getEntries();

        assertEquals(10, entries.size());
        assertTrue(new JsonRepository<List<ScoreEntry>>(scoresFile, new TypeReference<>() {
        }).exists());
        assertEquals("Alex", entries.getFirst().playerName());
        assertEquals(1000, entries.getFirst().score());
        assertEquals(100, entries.getLast().score());
    }

    @Test
    void recordInsertsSortedAndSurvivesReload() {
        service.getTable();
        service.record(new ScoreEntry("Tasman", 1500, PlayerType.HUMAN));

        HighScoreService reloaded = newService();
        List<ScoreEntry> entries = reloaded.getTable().getEntries();

        assertEquals(10, entries.size());
        assertEquals("Tasman", entries.getFirst().playerName());
        assertEquals(1500, entries.getFirst().score());
        assertEquals(200, entries.getLast().score());
    }

    @Test
    void qualifiesDelegatesToTheTable() {
        service.getTable();

        assertTrue(service.qualifies(101));
        assertFalse(service.qualifies(100));
        assertFalse(service.qualifies(50));
    }

    @Test
    void clearAllPersistsAnEmptyTable() {
        service.getTable();
        service.clearAll();

        assertTrue(service.getTable().getEntries().isEmpty());
        assertTrue(service.qualifies(0));

        HighScoreService reloaded = newService();
        assertTrue(reloaded.getTable().getEntries().isEmpty());
    }

    private HighScoreService newService() {
        return new HighScoreService(new JsonRepository<>(scoresFile, new TypeReference<>() {
        }));
    }
}
