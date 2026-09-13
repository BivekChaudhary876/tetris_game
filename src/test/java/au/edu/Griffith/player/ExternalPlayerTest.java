package au.edu.Griffith.player;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link ExternalPlayer}, using a <strong>Mockito mock</strong> of
 * {@link au.edu.Griffith.network.ExternalPlayerClient}.
 *
 * <p>A mock is the right double here, not a stub. What matters is the
 * <em>interaction</em>: that no move is requested while the server is down, and
 * that a reply is turned into the right commands. Mockito can assert those calls
 * happened — and it means the tests run without {@code TetrisServer.jar} and
 * without opening a socket.</p>
 *
 * <p>This is also why {@code ExternalPlayer} takes its client through the
 * constructor instead of creating one: a class that builds its own collaborators
 * cannot be tested against a substitute.</p>
 */
@ExtendWith(MockitoExtension.class)
@Disabled("Skeleton - enable as ExternalPlayer is implemented")
class ExternalPlayerTest {

    @Test
    void issuesNoCommandsWhileTheServerIsDisconnected() {
        // TODO: mock client with isConnected() false; update(); verify requestMoveAsync was never called
    }

    @Test
    void requestsAMoveOnceConnected() {
        // TODO: mock client with isConnected() true; update(); verify requestMoveAsync was called once
    }

    @Test
    void translatesTheServerReplyIntoRotateAndMoveCommands() {
        // TODO: have the mock invoke its callback with OpMove(3, 1);
        //       assert the model received one rotate and the right number of horizontal moves
    }

    @Test
    void disposeClosesTheConnection() {
        // TODO: dispose(); verify client.disconnect()
    }
}
