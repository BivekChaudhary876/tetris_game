package au.edu.Griffith.model;

/**
 * Lifecycle status of a single game session.
 *
 * <p>These three replace Milestone 1's {@code isPaused} and {@code isGameOver}
 * booleans, which between them could express four combinations for three legal
 * states. The {@link au.edu.Griffith.model.state} package models the
 * <em>behaviour</em> attached to each status; this enum is the plain data tag
 * for it.</p>
 */
public enum GameStatus {

    /** Pieces are falling and input is accepted. */
    RUNNING,

    /** Clock is stopped, input other than un-pause is ignored. */
    PAUSED,

    /** A piece could not be spawned; no further input is accepted. */
    GAME_OVER
}
