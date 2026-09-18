package au.edu.Griffith.controller;

import au.edu.Griffith.model.GameConfig;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.service.AudioManager;
import au.edu.Griffith.service.ConfigService;
import au.edu.Griffith.view.MainMenuScreen;

/**
 * Handles the configuration screen: holds a working copy of the settings while
 * the user edits them, and commits that copy through {@link ConfigService} when
 * they navigate back.
 *
 * <p>The screen edits a draft rather than the live {@link GameConfig} so a
 * partially-edited, invalid state is never visible to the running game.</p>
 *
 * <p>Audio settings are also applied immediately so the player hears the effect
 * of a toggle or volume change straight away.</p>
 */
public class ConfigurationController {

    private final ScreenNavigator navigator;
    private final GameConfig draft;

    public ConfigurationController(ScreenNavigator navigator) {
        this.navigator = navigator;

        GameConfig current = ConfigService.getInstance().getConfig();
        this.draft = current.copy();
    }

    /** The values the screen should display when it opens. */
    public GameConfig getDraft() {
        return draft;
    }

    public void setFieldWidth(int value) {
        draft.setFieldWidth(value);
    }

    public void setFieldHeight(int value) {
        draft.setFieldHeight(value);
    }

    public void setStartingLevel(int value) {
        draft.setStartingLevel(value);
    }

    public void setMusic(boolean on) {
        draft.setMusicOn(on);
        AudioManager.getInstance().setMusicOn(on);
    }

    public void setSoundEffects(boolean on) {
        draft.setSoundEffectsOn(on);
        AudioManager.getInstance().setEffectsOn(on);
    }

    /** Volume for music and effects, from 0 to 100. */
    public void setVolume(int value) {
        draft.setVolume(value);
        AudioManager.getInstance().setVolume(value);
    }

    /** AI Play controls whether player two is driven by the AI. */
    public void setAiPlay(boolean on) {
        draft.setPlayerTwoType(on ? PlayerType.AI : PlayerType.HUMAN);
    }

    public void setExtendMode(boolean on) {
        draft.setExtendMode(on);
    }

    /**
     * Validates and saves the current configuration, applies the final
     * audio settings, then returns to the main menu.
     */
    public void onBack() {
        ConfigService.getInstance().update(draft);

        AudioManager audio = AudioManager.getInstance();
        audio.setMusicOn(draft.isMusicOn());
        audio.setEffectsOn(draft.isSoundEffectsOn());
        audio.setVolume(draft.getVolume());

        navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
    }
}