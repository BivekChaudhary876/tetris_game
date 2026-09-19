package au.edu.Griffith.network;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * TCP client for {@code TetrisServer.jar}, which listens on {@code localhost:3000}.
 *
 * <p>The protocol is one JSON {@link PureGame} out, one JSON {@link OpMove}
 * back.</p>
 *
 * <p><b>A new socket per request.</b> The server closes the connection after it
 * answers, so there is no long-lived connection to hold. Every call to
 * {@link #requestMoveAsync} opens its own socket, sends, reads one line and
 * closes — which is why {@link #isConnected()} reports "was the server reachable
 * last time we tried" rather than "is a socket currently open".</p>
 *
 * <p><b>Threading.</b> Every socket call happens off the JavaFX thread. A
 * blocking connect or read on the UI thread would freeze the window, and the
 * spec requires the game to keep running when the server is <em>not</em> there.
 * So this class probes quietly in the background, exposes a volatile flag for
 * the warning banner, and hands replies back through a callback.</p>
 *
 * <p><b>Why a missing server is not an exception.</b> The spec treats it as an
 * expected state: warn, withhold control, then resume when it appears. A thrown
 * exception would make that a special case for every caller; a flag plus a probe
 * loop makes it ordinary.</p>
 */
public class ExternalPlayerClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 3000;

    /** How often to re-probe while the server is unreachable. */
    private static final long PROBE_INTERVAL_MS = 500;

    /** Short, so a missing server is noticed at once instead of hanging a frame. */
    private static final int CONNECT_TIMEOUT_MS = 500;

    /** Caps a server that accepts a socket but never answers. */
    private static final int READ_TIMEOUT_MS = 2000;

    private final String host;
    private final int port;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Serialises requests so two pieces cannot be in flight at once. */
    private final ExecutorService io = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "external-player-io");
        thread.setDaemon(true);
        return thread;
    });

    /** Probes for the server while it is unreachable, so the banner clears by itself. */
    private Thread probeThread;

    private volatile boolean running;
    private volatile boolean reachable;

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
     * True if the server answered, or responded to a probe, most recently.
     *
     * <p>{@code volatile} because the background threads write it and the JavaFX
     * thread reads it every frame to drive the warning banner.</p>
     */
    public boolean isConnected() {
        return reachable;
    }

    /**
     * Starts probing for the server in the background.
     *
     * <p>Returns immediately, never blocks, and never throws when the server is
     * down. The probe is what lets the warning clear — and control resume —
     * within half a second of the server being started mid-game, rather than
     * waiting for the next piece to spawn.</p>
     */
    public void connectAsync() {
        if (running) {
            return;
        }
        running = true;

        probeThread = new Thread(this::probeLoop, "external-player-probe");
        probeThread.setDaemon(true);
        probeThread.start();
    }

    private void probeLoop() {
        while (running) {
            if (!reachable) {
                probeOnce();
            }
            sleep(PROBE_INTERVAL_MS);
        }
    }

    /** Opens and immediately closes a socket, purely to see whether anyone is listening. */
    private void probeOnce() {
        try (Socket probe = new Socket()) {
            probe.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            if (!reachable) {
                System.out.println("TetrisServer reachable at " + host + ":" + port);
            }
            reachable = true;
        } catch (IOException e) {
            // Expected while the server is not running. Stay quiet and retry.
            reachable = false;
        }
    }

    /**
     * Sends a board state on a fresh socket and delivers the reply to
     * {@code onMove}.
     *
     * @param game   state to send
     * @param onMove called on the IO thread with the server's move; not called if
     *               the server is unreachable or the reply cannot be parsed
     */
    public void requestMoveAsync(PureGame game, Consumer<OpMove> onMove) {
        if (!running) {
            return;
        }

        io.submit(() -> {
            // try-with-resources: the server closes its end after replying, and
            // this makes sure we close ours even when the read fails.
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
                socket.setSoTimeout(READ_TIMEOUT_MS);

                try (PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                     BufferedReader in = new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                    out.println(mapper.writeValueAsString(game));

                    String reply = in.readLine();
                    if (reply == null) {
                        throw new IOException("Server closed without replying");
                    }

                    OpMove move = mapper.readValue(reply, OpMove.class);
                    reachable = true;
                    onMove.accept(move);
                }
            } catch (IOException e) {
                // Server gone, or went away mid-request. The probe loop will
                // notice when it comes back; this piece simply gets no move.
                reachable = false;
            } catch (RuntimeException e) {
                // A malformed reply should be visible, not silently swallowed by
                // the executor.
                System.err.println("Bad reply from TetrisServer: " + e);
                reachable = false;
            }
        });
    }

    /** Stops the probe loop and shuts down the IO thread. */
    public void disconnect() {
        running = false;
        reachable = false;

        if (probeThread != null) {
            probeThread.interrupt();
        }
        io.shutdownNow();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}