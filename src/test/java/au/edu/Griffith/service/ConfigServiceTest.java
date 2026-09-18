package au.edu.Griffith.service;

import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.PlayerType;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigServiceTest {

    @TempDir
    Path tempDir;

    private Path configFile;
    private ConfigService service;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("config.json");
        service = newService();
    }

    @Test
    void missingFileFallsBackToDefaultsAndCreatesJson() {
        GameConfig config = service.getConfig();

        assertEquals(10, config.getFieldWidth());
        assertEquals(20, config.getFieldHeight());
        assertEquals(1, config.getStartingLevel());
        assertTrue(config.isMusicOn());
        assertTrue(new JsonRepository<GameConfig>(configFile, new TypeReference<>() {
        }).exists());
    }

    @Test
    void getConfigReturnsTheSameLiveInstanceUntilReload() {
        GameConfig first = service.getConfig();
        GameConfig second = service.getConfig();
        assertSame(first, second);
    }

    @Test
    void updatePersistsAndSurvivesReload() {
        GameConfig updated = service.getConfig().copy();
        updated.setFieldWidth(12);
        updated.setFieldHeight(24);
        updated.setStartingLevel(7);
        updated.setMusicOn(false);
        updated.setSoundEffectsOn(false);
        updated.setExtendMode(true);
        updated.setPlayerOneType(PlayerType.AI);

        service.update(updated);

        ConfigService reloaded = newService();
        GameConfig loaded = reloaded.getConfig();
        assertEquals(12, loaded.getFieldWidth());
        assertEquals(24, loaded.getFieldHeight());
        assertEquals(7, loaded.getStartingLevel());
        assertEquals(false, loaded.isMusicOn());
        assertEquals(false, loaded.isSoundEffectsOn());
        assertEquals(true, loaded.isExtendMode());
        assertEquals(PlayerType.AI, loaded.getPlayerOneType());
    }

    @Test
    void updateRejectsInvalidSettingsAndLeavesLiveConfigUnchanged() {
        GameConfig live = service.getConfig();
        int originalWidth = live.getFieldWidth();

        GameConfig bad = live.copy();
        bad.setFieldWidth(99);

        assertThrows(IllegalStateException.class, () -> service.update(bad));
        assertEquals(originalWidth, service.getConfig().getFieldWidth());
    }

    @Test
    void reloadDropsTheCachedInstance() {
        GameConfig first = service.getConfig();
        service.reload();
        GameConfig second = service.getConfig();
        assertEquals(first.getFieldWidth(), second.getFieldWidth());
        // A fresh object is loaded from disk after reload.
        first.setFieldWidth(15);
        assertEquals(10, second.getFieldWidth());
    }

    private ConfigService newService() {
        return new ConfigService(new JsonRepository<>(configFile, new TypeReference<>() {
        }));
    }
}
