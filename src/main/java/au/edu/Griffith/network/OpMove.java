package au.edu.Griffith.network;

/**
 * The move {@code TetrisServer.jar} sends back: where to put the piece.
 *
 * <p>The server answers with a destination, not with keystrokes, so
 * {@link au.edu.Griffith.player.ExternalPlayer} has to turn this into a sequence
 * of rotate and move commands.</p>
 *
 * @param opX         target column for the piece's left edge
 * @param opRotate    number of 90-degree clockwise rotations to apply
 */
public record OpMove(int opX, int opRotate) {
}
