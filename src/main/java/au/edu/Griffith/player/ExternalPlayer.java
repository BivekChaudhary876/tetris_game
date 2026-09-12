package au.edu.Griffith.player;

import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.network.ExternalPlayerClient;

/**
 * A player whose moves come from {@code TetrisServer.jar} over a socket.
 *
 * <p>The connection lives in {@link ExternalPlayerClient}; this class only turns
 * the move the server returns into commands. That split keeps all socket and
 * threading concerns in the {@code network} package, and lets this class be
 * tested against a mocked client — which is the Mockito example the marking
 * criteria asks for.</p>
 *
 * <p>The spec requires the game to survive the server being absent: when
 * {@link ExternalPlayerClient#isConnected()} is false the field simply receives
 * no commands and a warning is shown, and play resumes the moment the client
 * reconnects. That behaviour is why the client reconnects in the background
 * rather than throwing.</p>
 */
public class ExternalPlayer extends AbstractPlayer {

    private final ExternalPlayerClient client;

    public ExternalPlayer(ExternalPlayerClient client) {
        this.client = client;
    }

    @Override
    public PlayerType getType() {
        return PlayerType.EXTERNAL;
    }

    @Override
    public void update(double elapsedMs) {
        throw new UnsupportedOperationException(
                "TODO: if connected and a new piece needs a move, send the board state "
                        + "and turn the returned OpMove into rotate/move commands; do nothing when disconnected");
    }

    /** True while the server is reachable; drives the on-screen warning. */
    public boolean isConnected() {
        throw new UnsupportedOperationException("TODO: delegate to client.isConnected()");
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("TODO: client.disconnect()");
    }
}
