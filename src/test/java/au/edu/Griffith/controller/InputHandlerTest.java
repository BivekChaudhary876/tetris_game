package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InputHandlerTest {

    @Test
    void singlePlayerAcceptsArrowKeys() {
        InputHandler handler = new InputHandler(InputHandler.SINGLE_PLAYER_KEYS);

        assertEquals(CommandFactory.Action.MOVE_LEFT, handler.resolve(KeyCode.LEFT));
        assertEquals(CommandFactory.Action.MOVE_RIGHT, handler.resolve(KeyCode.RIGHT));
        assertEquals(CommandFactory.Action.ROTATE, handler.resolve(KeyCode.UP));
        assertEquals(CommandFactory.Action.SOFT_DROP, handler.resolve(KeyCode.DOWN));
    }

    @Test
    void singlePlayerAlsoAcceptsTheLetterKeys() {
        InputHandler handler = new InputHandler(InputHandler.SINGLE_PLAYER_KEYS);

        // The spec gives a lone human player both layouts.
        assertEquals(CommandFactory.Action.MOVE_LEFT, handler.resolve(KeyCode.COMMA));
        assertEquals(CommandFactory.Action.MOVE_RIGHT, handler.resolve(KeyCode.PERIOD));
        assertEquals(CommandFactory.Action.ROTATE, handler.resolve(KeyCode.L));
        assertEquals(CommandFactory.Action.SOFT_DROP, handler.resolve(KeyCode.SPACE));
    }

    @Test
    void playerOneUsesTheLetterKeysOnly() {
        InputHandler handler = new InputHandler(InputHandler.PLAYER_ONE_KEYS);

        assertEquals(CommandFactory.Action.MOVE_LEFT, handler.resolve(KeyCode.COMMA));
        assertEquals(CommandFactory.Action.MOVE_RIGHT, handler.resolve(KeyCode.PERIOD));
        assertEquals(CommandFactory.Action.ROTATE, handler.resolve(KeyCode.L));
        assertEquals(CommandFactory.Action.SOFT_DROP, handler.resolve(KeyCode.SPACE));

        // In Extend Mode the arrows belong to player two.
        assertNull(handler.resolve(KeyCode.LEFT));
        assertNull(handler.resolve(KeyCode.DOWN));
    }

    @Test
    void playerTwoUsesArrowKeysOnly() {
        InputHandler handler = new InputHandler(InputHandler.PLAYER_TWO_KEYS);

        assertEquals(CommandFactory.Action.MOVE_LEFT, handler.resolve(KeyCode.LEFT));
        assertEquals(CommandFactory.Action.MOVE_RIGHT, handler.resolve(KeyCode.RIGHT));
        assertEquals(CommandFactory.Action.ROTATE, handler.resolve(KeyCode.UP));
        assertEquals(CommandFactory.Action.SOFT_DROP, handler.resolve(KeyCode.DOWN));

        assertNull(handler.resolve(KeyCode.COMMA));
        assertNull(handler.resolve(KeyCode.L));
    }

    @Test
    void noSchemeBindsTheAudioOrPauseKeys() {
        // S, M and P are session-level and handled in GameController. A scheme
        // that bound them would swallow the toggles.
        List<Map<KeyCode, CommandFactory.Action>> schemes = List.of(
                InputHandler.SINGLE_PLAYER_KEYS,
                InputHandler.PLAYER_ONE_KEYS,
                InputHandler.PLAYER_TWO_KEYS);

        for (Map<KeyCode, CommandFactory.Action> scheme : schemes) {
            InputHandler handler = new InputHandler(scheme);

            assertNull(handler.resolve(KeyCode.S), "S toggles sound effects");
            assertNull(handler.resolve(KeyCode.M), "M toggles music");
            assertNull(handler.resolve(KeyCode.P), "P pauses");
        }
    }
}
