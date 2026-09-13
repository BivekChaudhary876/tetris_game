package au.edu.Griffith.service;

/**
 * Singleton front for background music and sound effects.
 *
 * <p>Singleton because the audio device is a single real resource: two music
 * players would play over each other, and the {@code M} / {@code S} toggles have
 * to reach whatever is currently playing from whichever screen is open.</p>
 *
 * <p>Also the Facade pattern in miniature — callers say {@code playEffect(LINE_CLEAR)}
 * and never touch a {@code MediaPlayer}, so the JavaFX media API appears in one
 * class instead of being scattered through the game logic.</p>
 */
public final class AudioManager {

    /** The sound effects the game can trigger. */
    public enum Effect {
        MOVE,
        ROTATE,
        LINE_CLEAR,
        PIECE_LOCK,
        GAME_OVER,
        LEVEL_UP
    }

    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager();
    }

    private boolean musicOn = true;
    private boolean effectsOn = true;

    private AudioManager() {
        // Singleton.
    }

    public static AudioManager getInstance() {
        return Holder.INSTANCE;
    }

    public boolean isMusicOn() {
        return musicOn;
    }

    public boolean isEffectsOn() {
        return effectsOn;
    }

    /** Starts looping the background track, if music is enabled. */
    public void startMusic() {
        throw new UnsupportedOperationException("TODO: build a looping MediaPlayer from the music resource");
    }

    public void stopMusic() {
        throw new UnsupportedOperationException("TODO: stop and dispose the MediaPlayer");
    }

    public void pauseMusic() {
        throw new UnsupportedOperationException("TODO: pause the MediaPlayer");
    }

    public void resumeMusic() {
        throw new UnsupportedOperationException("TODO: resume the MediaPlayer if music is on");
    }

    /** Plays a one-shot effect, if effects are enabled. */
    public void playEffect(Effect effect) {
        throw new UnsupportedOperationException("TODO: look up the AudioClip for the effect and play it");
    }

    /** Behind the {@code M} key and the configuration checkbox. */
    public void setMusicOn(boolean musicOn) {
        throw new UnsupportedOperationException("TODO: store the flag and start or stop playback accordingly");
    }

    /** Behind the {@code S} key and the configuration checkbox. */
    public void setEffectsOn(boolean effectsOn) {
        throw new UnsupportedOperationException("TODO: store the flag");
    }
}
