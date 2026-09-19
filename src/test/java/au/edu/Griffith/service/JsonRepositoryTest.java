package au.edu.Griffith.service;

import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreConfig;
import au.edu.Griffith.model.ScoreEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * File I/O for the generic JSON repository, using a temp directory so tests
 * never touch {@code data/scores.json}.
 */
class JsonRepositoryTest {

    @TempDir
    Path tempDir;

    private Path file;
    private JsonRepository<List<ScoreEntry>> repository;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("corrupt-on-purpose.json");
        repository = new JsonRepository<>(file, new TypeReference<>() {
        });
    }

    @Test
    void loadReturnsEmptyWhenFileIsMissing() {
        assertTrue(repository.load().isEmpty());
        assertFalse(repository.exists());
    }

    @Test
    void saveCreatesParentDirectoriesAndRoundTripsEntries() throws Exception {
        Path nested = tempDir.resolve("data").resolve("scores.json");
        JsonRepository<List<ScoreEntry>> nestedRepo = new JsonRepository<>(nested, new TypeReference<>() {
        });

        List<ScoreEntry> entries = List.of(
                new ScoreEntry("Alex", 500, new ScoreConfig(10, 20, 1, PlayerType.HUMAN, false)),
                new ScoreEntry("Bot", 200, new ScoreConfig(10, 20, 1, PlayerType.AI, false)));

        nestedRepo.save(entries);

        assertTrue(Files.exists(nested));
        Optional<List<ScoreEntry>> loaded = nestedRepo.load();
        assertTrue(loaded.isPresent());
        assertEquals(entries, loaded.get());
    }

    @Test
    void saveWritesPrettyPrintedJson() throws Exception {
        repository.save(List.of(new ScoreEntry("Alex", 1200, new ScoreConfig(10, 20, 1, PlayerType.HUMAN, false))));

        String json = Files.readString(file);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("Alex"));
        assertTrue(json.contains("1200"));
        assertTrue(json.contains("HUMAN"));
        assertTrue(json.contains("\n"), "pretty printer should emit newlines");
    }

    @Test
    void loadReturnsEmptyOnCorruptJson() throws Exception {
        Files.writeString(file, "{not-valid-json");

        assertTrue(repository.load().isEmpty());
        assertTrue(repository.exists());
    }

    @Test
    void existsTracksTheFile() {
        assertFalse(repository.exists());
        repository.save(List.of());
        assertTrue(repository.exists());
    }
}
