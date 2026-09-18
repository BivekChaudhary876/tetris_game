package au.edu.Griffith.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameConfigTest {

    @Test
    void defaultsAreValid() {
        assertDoesNotThrow(() -> new GameConfig().validate());
    }

    @ParameterizedTest(name = "width {0} height {1} level {2} is rejected")
    @CsvSource({
            "4, 20, 1",
            "16, 20, 1",
            "10, 14, 1",
            "10, 31, 1",
            "10, 20, 0",
            "10, 20, 11"
    })
    void outOfRangeSettingsAreRejected(int width, int height, int level) {
        GameConfig config = new GameConfig();
        config.setFieldWidth(width);
        config.setFieldHeight(height);
        config.setStartingLevel(level);

        assertThrows(IllegalStateException.class, config::validate);
    }

    @ParameterizedTest(name = "width {0} height {1} level {2} is accepted")
    @CsvSource({
            "5, 15, 1",
            "15, 30, 10",
            "10, 20, 6"
    })
    void boundarySettingsAreAccepted(int width, int height, int level) {
        GameConfig config = new GameConfig();
        config.setFieldWidth(width);
        config.setFieldHeight(height);
        config.setStartingLevel(level);

        assertDoesNotThrow(config::validate);
    }

    @Test
    void copyIsAFieldByFieldClone() {
        GameConfig original = new GameConfig();
        original.setFieldWidth(12);
        original.setFieldHeight(22);
        original.setStartingLevel(4);
        original.setMusicOn(false);
        original.setSoundEffectsOn(false);
        original.setExtendMode(true);
        original.setPlayerOneType(PlayerType.AI);
        original.setPlayerTwoType(PlayerType.EXTERNAL);

        GameConfig clone = original.copy();

        assertNotSame(original, clone);
        assertEquals(12, clone.getFieldWidth());
        assertEquals(22, clone.getFieldHeight());
        assertEquals(4, clone.getStartingLevel());
        assertEquals(false, clone.isMusicOn());
        assertEquals(false, clone.isSoundEffectsOn());
        assertEquals(true, clone.isExtendMode());
        assertEquals(PlayerType.AI, clone.getPlayerOneType());
        assertEquals(PlayerType.EXTERNAL, clone.getPlayerTwoType());
    }

    @Test
    void mutatingTheCopyDoesNotChangeTheOriginal() {
        GameConfig original = new GameConfig();
        GameConfig clone = original.copy();

        clone.setFieldWidth(15);
        clone.setMusicOn(false);

        assertEquals(10, original.getFieldWidth());
        assertEquals(true, original.isMusicOn());
    }
}
