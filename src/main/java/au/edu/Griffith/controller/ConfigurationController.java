package au.edu.Griffith.controller;

import au.edu.Griffith.view.MainMenuScreen;

/**
 * Handles the configuration screen's navigation.
 *
 * <p>Thin on purpose. In Milestone 1 the configuration screen was presentational
 * only — the sliders and checkboxes moved and updated their own labels, but
 * nothing was stored or applied to the game. That behaviour is reproduced here
 * unchanged.</p>
 *
 * <p>Persisting these settings and applying them to gameplay is a later
 * milestone requirement; {@link au.edu.Griffith.service.ConfigService} is the
 * seam it will be built on.</p>
 */
public class ConfigurationController {

    private final ScreenNavigator navigator;

    public ConfigurationController(ScreenNavigator navigator) {
        this.navigator = navigator;
    }

    public void onBack() {
        navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
    }
}
