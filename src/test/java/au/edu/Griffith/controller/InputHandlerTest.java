package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InputHandlerTest {

    @Test
    void playerOneUsesArrowKeys() {
        InputHandler handler = new InputHandler(InputHandler.DEFAULT_KEYS);

        assertEquals(CommandFactory.Action.MOVE_LEFT, handler.resolve(KeyCode.LEFT));
        assertEquals(CommandFactory.Action.MOVE_RIGHT, handler.resolve(KeyCode.RIGHT));
        assertEquals(CommandFactory.Action.ROTATE, handler.resolve(KeyCode.UP));
        assertEquals(CommandFactory.Action.SOFT_DROP, handler.resolve(KeyCode.DOWN));
        assertNull(handler.resolve(KeyCode.A));
    }

    @Test
    void playerTwoUsesWasd() {
        InputHandler handler = new InputHandler(InputHandler.PLAYER_TWO_KEYS);

        assertEquals(CommandFactory.Action.MOVE_LEFT, handler.resolve(KeyCode.A));
        assertEquals(CommandFactory.Action.MOVE_RIGHT, handler.resolve(KeyCode.D));
        assertEquals(CommandFactory.Action.ROTATE, handler.resolve(KeyCode.W));
        assertEquals(CommandFactory.Action.SOFT_DROP, handler.resolve(KeyCode.S));
        assertNull(handler.resolve(KeyCode.LEFT));
    }
}
