package au.edu.Griffith.player;

import au.edu.Griffith.model.PlayerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PlayerFactoryTest {

    @Test
    void humanTypeBuildsAHumanPlayer() {
        Player player = PlayerFactory.create(PlayerType.HUMAN);
        assertInstanceOf(HumanPlayer.class, player);
        assertEquals(PlayerType.HUMAN, player.getType());
    }

    @Test
    void aiTypeBuildsAnAiPlayer() {
        Player player = PlayerFactory.create(PlayerType.AI);
        assertInstanceOf(AIPlayer.class, player);
        assertEquals(PlayerType.AI, player.getType());
    }

    @Test
    void externalTypeBuildsAnExternalPlayer() {
        Player player = PlayerFactory.create(PlayerType.EXTERNAL);
        assertInstanceOf(ExternalPlayer.class, player);
        assertEquals(PlayerType.EXTERNAL, player.getType());
    }
}
