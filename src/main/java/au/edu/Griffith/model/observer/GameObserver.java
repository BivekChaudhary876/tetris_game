package au.edu.Griffith.model.observer;

/**
 * Observer half of the Observer pattern.
 *
 * <p>The single reason this interface exists is to invert the dependency between
 * model and view: the model publishes to {@code GameObserver} and never imports
 * a view class, while the views implement this interface and subscribe. That is
 * what lets the same {@code GameModel} drive one field, two fields side by side,
 * or a headless unit test.</p>
 *
 * <p>Kept to a single method (Interface Segregation) so no implementer is forced
 * to stub out callbacks it does not care about; it filters on
 * {@link GameEvent#type()} instead.</p>
 */
@FunctionalInterface
public interface GameObserver {

    /**
     * Called after the model has finished changing state.
     *
     * @param event what changed
     */
    void onGameEvent(GameEvent event);
}
