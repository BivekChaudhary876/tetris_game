package au.edu.Griffith.model;

/**
 * The kinds of player that can occupy a field.
 *
 * <p>Drives both the on-screen "Player type" readout and the
 * {@link au.edu.Griffith.player.PlayerFactory} that builds the matching
 * controller for a field.</p>
 */
public enum PlayerType {

    /** Keyboard-driven player. */
    HUMAN("Human"),

    /** Local AI that plans its own moves. */
    AI("AI"),

    /** Moves supplied by {@code TetrisServer.jar} over a socket. */
    EXTERNAL("External");

    private final String displayName;

    PlayerType(String displayName) {
        this.displayName = displayName;
    }

    /** Label shown in the side panel and high-score table. */
    public String displayName() {
        return displayName;
    }
}
