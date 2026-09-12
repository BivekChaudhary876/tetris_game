package au.edu.Griffith.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Score}, including the parameterized case the marking criteria
 * asks for.
 *
 * <p>The scoring rule is several related cases that differ only in their numbers
 * — exactly the shape {@code @ParameterizedTest} exists for. Writing it as four
 * near-identical {@code @Test} methods would say the same thing four times.</p>
 *
 * <p>The expected values are Milestone 1's flat 100 points per line.</p>
 */
class ScoreTest {

    @ParameterizedTest(name = "{0} lines cleared scores {1} points")
    @CsvSource({
            "0, 0",
            "1, 100",
            "2, 200",
            "3, 300",
            "4, 400"
    })
    void awardsTheCorrectPointsPerClear(int linesCleared, int expectedPoints) {
        Score score = new Score();

        assertEquals(expectedPoints, score.addClearedLines(linesCleared));
        assertEquals(expectedPoints, score.getPoints());
    }

    @Test
    void pointsAccumulateAcrossClears() {
        Score score = new Score();

        score.addClearedLines(1);
        score.addClearedLines(1);

        assertEquals(200, score.getPoints());
    }

    @Test
    void resetReturnsTheScoreToZero() {
        Score score = new Score();
        score.addClearedLines(4);

        score.reset();

        assertEquals(0, score.getPoints());
    }
}
