package au.edu.Griffith.service;

import java.util.Optional;

/**
 * A generic load/save contract for anything the game persists.
 *
 * <p>Generic on {@code T} so one implementation serves both the configuration
 * and the high-score table with no casting and no duplicated file-handling code
 * — this is the "Generics" criterion, and also Dependency Inversion: the
 * services above depend on this interface, not on Jackson or on
 * {@code java.io}.</p>
 *
 * @param <T> the type being stored
 */
public interface Repository<T> {

    /**
     * Reads the stored value.
     *
     * @return the value, or empty if nothing has been saved yet or the file is
     *         unreadable — a first run is not an error
     */
    Optional<T> load();

    /** Writes the value, replacing anything already stored. */
    void save(T value);

    /** True if a stored value exists. */
    boolean exists();
}
