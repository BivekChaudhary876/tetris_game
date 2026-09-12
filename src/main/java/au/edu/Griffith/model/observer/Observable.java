package au.edu.Griffith.model.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject half of the Observer pattern: holds the observer list and fans events
 * out to it.
 *
 * <p>Made an abstract base class rather than repeating the same three methods in
 * every model class. {@link au.edu.Griffith.model.GameModel} extends it, so the
 * model gains publish/subscribe for free without any view coupling.</p>
 */
public abstract class Observable {

    private final List<GameObserver> observers = new ArrayList<>();

    /** Registers an observer. Ignores a duplicate registration. */
    public void addObserver(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Unregisters an observer, for example when a screen is closed. */
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Pushes an event to every registered observer.
     *
     * <p>Iterates a snapshot so an observer that unsubscribes inside its own
     * callback cannot cause a {@link java.util.ConcurrentModificationException}.</p>
     */
    protected void notifyObservers(GameEvent event) {
        for (GameObserver observer : List.copyOf(observers)) {
            observer.onGameEvent(event);
        }
    }

    /** Read-only view of the current observers, used by tests. */
    protected List<GameObserver> getObservers() {
        return List.copyOf(observers);
    }
}
