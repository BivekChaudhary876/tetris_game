package au.edu.Griffith.ai;

/**
 * A placement the AI is considering: put the piece's left edge on {@code column}
 * after turning it {@code rotations} times.
 *
 * <p>Same shape as {@link au.edu.Griffith.network.OpMove} (what TetrisServer
 * sends back) so AIPlayer and ExternalPlayer can steer a piece the same way.</p>
 *
 * @param score how good this placement is. Only used to compare candidates —
 *              it is not the player's game score.
 */
public record Move(int column, int rotations, double score) {
}