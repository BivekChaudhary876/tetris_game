package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import javafx.scene.input.KeyCode;

import java.util.Map;

/**
 * Translates key presses into {@link CommandFactory.Action} values.
 *
 * <p>A lookup table rather than Milestone 1's {@code switch} inside the scene's
 * key handler. Remapping keys becomes data rather than code, and the mapping can
 * be unit tested on its own.</p>
 *
 * <p>Game-level keys — currently just {@code P} for pause — are handled
 * separately in {@link GameController}, because they act on the session rather
 * than on a piece.</p>
 */
public class InputHandler {

    /** The arrow-key layout from Milestone 1 — Player 1. */
    public static final Map<KeyCode, CommandFactory.Action> DEFAULT_KEYS = Map.of(
            KeyCode.LEFT, CommandFactory.Action.MOVE_LEFT,
            KeyCode.RIGHT, CommandFactory.Action.MOVE_RIGHT,
            KeyCode.UP, CommandFactory.Action.ROTATE,
            KeyCode.DOWN, CommandFactory.Action.SOFT_DROP);

    /** WASD layout for Player 2 in extend mode. */
    public static final Map<KeyCode, CommandFactory.Action> PLAYER_TWO_KEYS = Map.of(
            KeyCode.A, CommandFactory.Action.MOVE_LEFT,
            KeyCode.D, CommandFactory.Action.MOVE_RIGHT,
            KeyCode.W, CommandFactory.Action.ROTATE,
            KeyCode.S, CommandFactory.Action.SOFT_DROP);

    private final Map<KeyCode, CommandFactory.Action> keyMap;

    public InputHandler(Map<KeyCode, CommandFactory.Action> keyMap) {
        this.keyMap = Map.copyOf(keyMap);
    }

    /**
     * The action a key means, or {@code null} if the key is not bound.
     *
     * @param code key that was pressed
     */
    public CommandFactory.Action resolve(KeyCode code) {
        return keyMap.get(code);
    }
}
