package au.edu.Griffith.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * What {@code JavaTetrisScore.json} contains: {@code {"scores":[ ... ]}}.
 *
 * <p>A wrapper rather than a bare JSON array, matching the reference build's
 * file. It also leaves room to add fields later — a version number, say —
 * without the file changing shape.</p>
 */
public record ScoreBoard(@JsonProperty("scores") List<ScoreEntry> scores) {

    /** An empty board, for a first run or after Clear. */
    public static ScoreBoard empty() {
        return new ScoreBoard(List.of());
    }

    /** Never null, so callers need not guard against a hand-edited file. */
    @Override
    public List<ScoreEntry> scores() {
        return scores == null ? List.of() : scores;
    }
}