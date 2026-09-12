package au.edu.Griffith.model.tetromino;

import java.util.Random;

/**
 * The production {@link TetrominoGenerator}: a uniformly random sequence of the
 * seven shapes, matching Milestone 1's {@code getRandomType()}.
 *
 * <p>Seeded rather than calling {@code Math.random()} directly. The behaviour is
 * identical, but holding the seed means a game can be replayed exactly when
 * diagnosing a bug, and a test can pin the sequence instead of fighting a global
 * random source.</p>
 */
public class SharedSequenceGenerator implements TetrominoGenerator {

    private static final TetrominoType[] SHAPES = TetrominoType.values();

    private final long seed;
    private Random random;
    private TetrominoType lookahead;

    /** Creates a generator with an arbitrary seed. */
    public SharedSequenceGenerator() {
        this(new Random().nextLong());
    }

    /** Creates a generator that will always produce the same sequence for {@code seed}. */
    public SharedSequenceGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /** The seed in use, so a session can be reproduced. */
    public long getSeed() {
        return seed;
    }

    @Override
    public TetrominoType next() {
        TetrominoType current = peek();
        lookahead = draw();
        return current;
    }

    @Override
    public TetrominoType peek() {
        if (lookahead == null) {
            lookahead = draw();
        }
        return lookahead;
    }

    @Override
    public void reset() {
        random = new Random(seed);
        lookahead = null;
    }

    private TetrominoType draw() {
        return SHAPES[random.nextInt(SHAPES.length)];
    }
}
