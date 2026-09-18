package au.edu.Griffith.player;

import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.network.ExternalPlayerClient;

/**
 * Builds the {@link Player} implementation matching a {@link PlayerType}.
 *
 * <p>Keeps the "which class do I need?" decision in one place, so
 * {@link au.edu.Griffith.controller.GameController} can set up a field from a
 * configuration value without knowing that {@code ExternalPlayer} needs a socket
 * client and the others do not.</p>
 */
public final class PlayerFactory {

    private PlayerFactory() {
        // Static factory only.
    }

    /**
     * @param type which kind of player to build
     * @return a detached player, ready for {@link Player#attach}
     */
    public static Player create(PlayerType type) {
        return switch (type) {
            case HUMAN -> new HumanPlayer();
            case AI -> new AIPlayer();
            case EXTERNAL -> new ExternalPlayer(new ExternalPlayerClient());
        };
    }
}
