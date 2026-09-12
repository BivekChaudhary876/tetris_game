package au.edu.Griffith.model.tetromino;

import java.util.List;

/**
 * A test <strong>Stub</strong>: a {@link TetrominoGenerator} that hands back a
 * sequence fixed in advance.
 *
 * <p>This is the test double the marking criteria asks for, and the reason
 * {@code TetrominoGenerator} was made an interface rather than a method on the
 * model. A test that needs "spawn an I-piece, then an O-piece" can say exactly
 * that, instead of seeding a random source and hoping.</p>
 *
 * <p>A stub rather than a mock: it supplies canned answers and records nothing.
 * Verification of interactions is what the Mockito examples are for.</p>
 */
public class StubTetrominoGenerator implements TetrominoGenerator {

    private final List<TetrominoType> sequence;
    private int index;

    /**
     * @param sequence the shapes to hand out, in order; wraps around when exhausted
     */
    public StubTetrominoGenerator(TetrominoType... sequence) {
        this.sequence = List.of(sequence);
    }

    @Override
    public TetrominoType next() {
        TetrominoType type = sequence.get(index % sequence.size());
        index++;
        return type;
    }

    @Override
    public TetrominoType peek() {
        return sequence.get(index % sequence.size());
    }

    @Override
    public void reset() {
        index = 0;
    }
}
