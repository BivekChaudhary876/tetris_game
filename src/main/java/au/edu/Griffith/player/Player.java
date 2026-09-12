package au.edu.Griffith.player;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.PlayerType;

/**
 * Strategy pattern: an interchangeable source of moves for one field.
 *
 * <p>This is what makes the two-player requirement tractable. A field does not
 * know whether it is being played by a person, by the AI or by
 * {@code TetrisServer.jar} — it holds a {@code Player}. "Human vs AI",
 * "AI vs AI" and "External vs External" are then the same code with two
 * different objects plugged in, rather than three separate game loops.</p>
 *
 * <p>Liskov Substitution in action: every implementation must be usable wherever
 * a {@code Player} is expected, which is why none of them is allowed to require
 * extra setup calls beyond {@link #attach(GameModel)}.</p>
 */
public interface Player {

    /** Which kind of player this is, for the side-panel readout. */
    PlayerType getType();

    /** Binds this player to the field it will control. */
    void attach(GameModel model);

    /**
     * Called once per frame; the player issues whatever commands it wants for
     * this frame, or none.
     *
     * @param elapsedMs milliseconds since the previous frame
     */
    void update(double elapsedMs);

    /** Releases any thread, socket or listener this player holds. */
    void dispose();
}
