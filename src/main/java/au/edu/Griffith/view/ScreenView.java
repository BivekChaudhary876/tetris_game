package au.edu.Griffith.view;

import javafx.scene.Parent;

/**
 * Every screen in the application: splash, menu, configuration, high scores and game.
 *
 * <p>The <strong>View</strong> half of MVC. A view builds JavaFX nodes and
 * displays what it is told; it holds no game rules and never mutates the model
 * directly. This interface is what lets
 * {@link au.edu.Griffith.controller.ScreenNavigator} swap screens without
 * knowing what any of them are.</p>
 */
public interface ScreenView {

    /** The root node of this screen, ready to be placed in a {@code Scene}. */
    Parent getRoot();

    /** Title for the window while this screen is showing. */
    String getTitle();

    /** Scene width this screen wants. */
    double getPrefWidth();

    /** Scene height this screen wants. */
    double getPrefHeight();

    /** Called when the screen becomes visible; a good place to refresh from the model. */
    void onShow();

    /** Called when the screen is replaced; unsubscribe observers and release resources here. */
    void onHide();
}
