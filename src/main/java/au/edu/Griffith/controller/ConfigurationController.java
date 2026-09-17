package au.edu.Griffith.controller;

import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.service.AudioManager;
import au.edu.Griffith.service.ConfigService;
import au.edu.Griffith.view.MainMenuScreen;

/**
 * Handles the configuration screen: a draft copy of the live settings, saved
 * when the player leaves with Back.
 */
public class ConfigurationController {

    private final ScreenNavigator navigator;
    private final GameConfig draft;

    public ConfigurationController(ScreenNavigator navigator) {
        this.navigator = navigator;
        this.draft = ConfigService.getInstance().getConfig().copy();
    }

    /** The working copy the sliders and checkboxes edit. */
    public GameConfig getDraft() {
        return draft;
    }

    /** Validates, writes {@code config.json}, then returns to the menu. */
    public void onBack() {
        ConfigService.getInstance().update(draft);
        AudioManager audio = AudioManager.getInstance();
        audio.setMusicOn(draft.isMusicOn());
        audio.setEffectsOn(draft.isSoundEffectsOn());
        navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
    }
}
