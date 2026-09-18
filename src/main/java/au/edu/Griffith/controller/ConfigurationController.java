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
 * <p>Audio settings are the exception to that: they also reach
 * {@link AudioManager} as they change, so the player hears the effect of a
 * toggle or a volume move straight away rather than on the next game.</p>
 */
public class ConfigurationController {

    private final ScreenNavigator navigator;
    private final GameConfig draft;

    public ConfigurationController(ScreenNavigator navigator) {
        this.navigator = navigator;
        this.draft = ConfigService.getInstance().getConfig().copy();
    }

    /** The values the screen should display when it opens. */
    public GameConfig getDraft() {
        return draft;
    }

    // field width and field height is being updated
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

    /** Volume for music and effects, 0 to 100. Applied live so the slider is audible. */
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


    //saves the new settings
    public void onBack() {
        ConfigService.getInstance().update(draft);
        navigator.show(new MainMenuScreen(new MainMenuController(navigator)));
    }
}