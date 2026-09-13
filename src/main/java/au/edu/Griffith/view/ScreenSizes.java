package au.edu.Griffith.view;

/**
 * Layout constants shared by more than one screen, all taken from Milestone 1.
 *
 * <p>Collected here so the same numbers are not retyped across the view classes,
 * the way the Milestone 1 screens each carried their own copy of {@code 800} and
 * {@code 700}.</p>
 */
public final class ScreenSizes {

    /** Scene width for the menu, configuration and high-score screens. */
    public static final double MENU_WIDTH = 800;

    /** Scene height for the menu, configuration and high-score screens. */
    public static final double MENU_HEIGHT = 700;

    /** Side of one board cell, in pixels. */
    public static final double TILE = 30;

    /** Width of the panel beside the playfield. */
    public static final double SIDEBAR_WIDTH = 200;

    /** Side of one block in the next-piece preview. */
    public static final double PREVIEW_BLOCK = 28;

    /** Width the splash artwork is scaled to. */
    public static final double SPLASH_IMAGE_WIDTH = 800;

    /** Width of a menu or sidebar button. */
    public static final double BUTTON_WIDTH = 150;

    /** Vertical gap between stacked menu items. */
    public static final double MENU_SPACING = 20;

    private ScreenSizes() {
        // Constants only.
    }
}
