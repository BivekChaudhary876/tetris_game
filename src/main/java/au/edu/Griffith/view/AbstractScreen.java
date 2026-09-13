package au.edu.Griffith.view;

import javafx.scene.Parent;

import java.util.Objects;

/**
 * Base class for screens: applies the shared stylesheet and fixes the order in
 * which a screen is assembled.
 *
 * <p>Template Method — {@link #getRoot()} runs the same sequence for every
 * screen (build the layout, attach {@code tetris.css}, tag the root with a style
 * class) and calls {@link #buildLayout()} for the part that differs. In
 * Milestone 1 each screen set its own inline {@code -fx-} strings, which is why
 * the same dark-panel colours were repeated in four files; moving styling to a
 * stylesheet applied here removes that duplication.</p>
 */
public abstract class AbstractScreen implements ScreenView {

    /** Shared stylesheet, applied to every screen. */
    protected static final String STYLESHEET = "/css/tetris.css";

    private Parent root;

    @Override
    public Parent getRoot() {
        if (root == null) {
            root = buildLayout();
            root.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(STYLESHEET),
                            "Missing stylesheet on the classpath: " + STYLESHEET).toExternalForm());
            root.getStyleClass().add(rootStyleClass());
        }
        return root;
    }

    @Override
    public double getPrefWidth() {
        return ScreenSizes.MENU_WIDTH;
    }

    @Override
    public double getPrefHeight() {
        return ScreenSizes.MENU_HEIGHT;
    }

    @Override
    public void onShow() {
        // Screens that need to refresh override this.
    }

    @Override
    public void onHide() {
        // Screens that subscribe to the model override this to unsubscribe.
    }

    /** Builds the screen-specific node tree. Called once, lazily. */
    protected abstract Parent buildLayout();

    /** CSS style class put on the root node, so each screen can be themed. */
    protected abstract String rootStyleClass();
}
