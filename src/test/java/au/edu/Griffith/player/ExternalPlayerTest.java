package au.edu.Griffith.player;

import au.edu.Griffith.model.Board;
import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.tetromino.SharedSequenceGenerator;
import au.edu.Griffith.network.ExternalPlayerClient;
import au.edu.Griffith.network.OpMove;
import au.edu.Griffith.network.PureGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ExternalPlayer}, using a Mockito mock of
 * {@link ExternalPlayerClient}.
 *
 * <p>A mock is the right double here, not a stub. What matters is the
 * interaction: that no move is requested while the server is down, and that a
 * reply turns into commands. Mockito can assert those calls happened, and the
 * tests run without TetrisServer.jar and without opening a socket.</p>
 *
 * <p>This is also why ExternalPlayer takes its client through the constructor
 * instead of creating one: a class that builds its own collaborators cannot be
 * tested against a substitute.</p>
 */
@ExtendWith(MockitoExtension.class)
class ExternalPlayerTest {

    /** Long enough to clear ExternalPlayer's move interval in one update. */
    private static final double A_FULL_TICK = 100;

    @Mock
    private ExternalPlayerClient client;

    private GameModel model;
    private ExternalPlayer player;

    @BeforeEach
    void setUp() {
        model = new GameModel(new Board(10, 20), new SharedSequenceGenerator(42));
        player = new ExternalPlayer(client);
        player.attach(model);
        model.start();
    }

    @Test
    void attachStartsConnecting() {
        verify(client).connectAsync();
    }

    @Test
    void issuesNoCommandsWhileTheServerIsDisconnected() {
        when(client.isConnected()).thenReturn(false);

        player.update(A_FULL_TICK);

        verify(client, never()).requestMoveAsync(any(), any());
        assertFalse(player.isConnected());
    }

    @Test
    void requestsAMoveOnceConnected() {
        when(client.isConnected()).thenReturn(true);

        player.update(A_FULL_TICK);

        verify(client, times(1)).requestMoveAsync(any(PureGame.class), any());
    }

    @Test
    void requestsOnlyOneMovePerPiece() {
        when(client.isConnected()).thenReturn(true);

        player.update(A_FULL_TICK);
        player.update(A_FULL_TICK);
        player.update(A_FULL_TICK);

        // Three frames inside the retry window: the server is asked once, not
        // once per frame, or a slow server would be flooded.
        verify(client, times(1)).requestMoveAsync(any(PureGame.class), any());
    }

    @Test
    void retriesWhenNoReplyArrives() {
        when(client.isConnected()).thenReturn(true);

        // The client only calls back on success, so a failed request would
        // otherwise leave the piece waiting forever.
        player.update(A_FULL_TICK);
        player.update(600);

        verify(client, times(2)).requestMoveAsync(any(PureGame.class), any());
    }

    @Test
    void translatesTheServerReplyIntoMovement() {
        when(client.isConnected()).thenReturn(true);

        doAnswer(invocation -> {
            Consumer<OpMove> onMove = invocation.getArgument(1);
            onMove.accept(new OpMove(0, 0));
            return null;
        }).when(client).requestMoveAsync(any(), any());

        int startCol = leftmostColumn();

        player.update(A_FULL_TICK);
        player.update(A_FULL_TICK);

        assertTrue(leftmostColumn() < startCol,
                "piece should have moved toward the target column");
    }

    @Test
    void resumesWhenTheServerAppearsMidGame() {
        when(client.isConnected()).thenReturn(false, false, true);

        player.update(A_FULL_TICK);
        player.update(A_FULL_TICK);
        verify(client, never()).requestMoveAsync(any(), any());

        player.update(A_FULL_TICK);

        // No restart, no new piece: the next frame after the server appears is
        // enough for control to resume.
        verify(client, times(1)).requestMoveAsync(any(PureGame.class), any());
    }

    @Test
    void disposeClosesTheConnection() {
        player.dispose();

        verify(client).disconnect();
    }

    @Test
    void reportsTheExternalPlayerType() {
        assertEquals(PlayerType.EXTERNAL, player.getType());
    }

    private int leftmostColumn() {
        return model.getActivePiece().getCells().stream()
                .mapToInt(cell -> cell.col())
                .min()
                .orElseThrow();
    }
}
