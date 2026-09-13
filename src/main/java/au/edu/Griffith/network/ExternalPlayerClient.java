package au.edu.Griffith.network;

/**
 * TCP client for {@code TetrisServer.jar}, which listens on {@code localhost:3000}.
 *
 * <p>The protocol is one JSON {@link PureGame} out, one JSON {@link OpMove}
 * back, per piece.</p>
 *
 * <p><strong>Threading.</strong> Every socket call happens on a background
 * thread. A blocking {@code connect} or {@code read} on the JavaFX application
 * thread would freeze the whole window, and the spec explicitly requires the
 * game to keep running when the server is <em>not</em> there. So the client
 * retries quietly in the background, exposes {@link #isConnected()} for the
 * warning banner, and hands results back through a callback that the caller
 * marshals onto the UI thread.</p>
 */
public class ExternalPlayerClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 3000;

    /** How long to wait before retrying a failed connection. */
    private static final long RETRY_INTERVAL_MS = 2000;

    private final String host;
    private final int port;
    private volatile boolean connected;

    public ExternalPlayerClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ExternalPlayerClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * True while the socket is open.
     *
     * <p>{@code volatile} because it is written by the background thread and read
     * by the JavaFX thread.</p>
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Starts connecting in the background and keeps retrying every
     * {@link #RETRY_INTERVAL_MS} until {@link #disconnect()} is called.
     *
     * <p>Returns immediately; it never blocks the caller and never throws when
     * the server is down.</p>
     */
    public void connectAsync() {
        throw new UnsupportedOperationException(
                "TODO: start a daemon thread that opens the socket, sets connected, and retries on failure");
    }

    /**
     * Sends a board state and delivers the server's move to {@code onMove}.
     *
     * @param game   state to send
     * @param onMove called on the background thread with the reply; not called
     *               if the server is unreachable
     */
    public void requestMoveAsync(PureGame game, java.util.function.Consumer<OpMove> onMove) {
        throw new UnsupportedOperationException(
                "TODO: serialise game to JSON, write a line, read the reply, parse into OpMove, invoke onMove");
    }

    /** Closes the socket and stops the retry loop. */
    public void disconnect() {
        throw new UnsupportedOperationException("TODO: clear the running flag, close the socket, join the thread");
    }
}
