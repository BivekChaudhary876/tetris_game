package au.edu.Griffith.controller;

import au.edu.Griffith.controller.command.CommandFactory;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates key presses into {@link CommandFactory.Action} values.
 *
 * <p>A lookup table rather than Milestone 1's {@code switch} inside the scene's
 * key handler. Remapping keys becomes data rather than code, and the mapping can
 * be unit tested on its own.</p>
 *
 * <p>Three schemes, from the specification:</p>
 * <ul>
 *   <li>{@link #SINGLE_PLAYER_KEYS} — a single human field accepts <em>either</em>
 *   the arrow keys or {@code , . Space L}.</li>
 *   <li>{@link #PLAYER_ONE_KEYS} — Extend Mode, player one: {@code , . Space L}.</li>
 *   <li>{@link #PLAYER_TWO_KEYS} — Extend Mode, player two: the arrow keys.</li>
 * </ul>
 *
 * <p>Game-level keys — {@code P} pause, {@code S} sound, {@code M} music — are
 * handled in {@link GameController}, because they act on the session rather than
 * on a piece. That is also why no scheme here binds {@code S}: an earlier WASD
 * layout did, and it would have swallowed the sound toggle.</p>
 */
public class InputHandler {

    /** Arrow-key layout. */
    private static final Map<KeyCode, CommandFactory.Action> ARROW_KEYS = Map.of(
            KeyCode.LEFT, CommandFactory.Action.MOVE_LEFT,
            KeyCode.RIGHT, CommandFactory.Action.MOVE_RIGHT,
            KeyCode.UP, CommandFactory.Action.ROTATE,
            KeyCode.DOWN, CommandFactory.Action.SOFT_DROP);

    /** The {@code , . Space L} layout. */
    private static final Map<KeyCode, CommandFactory.Action> LETTER_KEYS = Map.of(
            KeyCode.COMMA, CommandFactory.Action.MOVE_LEFT,
            KeyCode.PERIOD, CommandFactory.Action.MOVE_RIGHT,
            KeyCode.L, CommandFactory.Action.ROTATE,
            KeyCode.SPACE, CommandFactory.Action.SOFT_DROP);

    /** Single-player: both layouts drive the one field. */
    public static final Map<KeyCode, CommandFactory.Action> SINGLE_PLAYER_KEYS =
            merge(ARROW_KEYS, LETTER_KEYS);

    /** Extend Mode, player one. */
    public static final Map<KeyCode, CommandFactory.Action> PLAYER_ONE_KEYS = LETTER_KEYS;

    /** Extend Mode, player two. */
    public static final Map<KeyCode, CommandFactory.Action> PLAYER_TWO_KEYS = ARROW_KEYS;

    /** Kept for callers written against the previous single-scheme API. */
    public static final Map<KeyCode, CommandFactory.Action> DEFAULT_KEYS = SINGLE_PLAYER_KEYS;

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

    private static Map<KeyCode, CommandFactory.Action> merge(
            Map<KeyCode, CommandFactory.Action> first,
            Map<KeyCode, CommandFactory.Action> second) {

        Map<KeyCode, CommandFactory.Action> combined = new HashMap<>(first);
        combined.putAll(second);
        return Map.copyOf(combined);
    }
}