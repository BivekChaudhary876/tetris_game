package au.edu.Griffith.service;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

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
 *
 * <p>Effects are {@link AudioClip}s and music is a {@link MediaPlayer}: clips are
 * held decoded in memory so a rotation sound fires with no latency and several
 * can overlap, which a {@code MediaPlayer} per effect could not do.</p>
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

    private static final String MUSIC = "/audios/background.mp3";

    private static final Map<Effect, String> EFFECT_FILES =
            new EnumMap<>(Effect.class);

    static {
        EFFECT_FILES.put(Effect.MOVE, "/audios/move-turn.wav");
        EFFECT_FILES.put(Effect.ROTATE, "/audios/move-turn.wav");
        EFFECT_FILES.put(Effect.PIECE_LOCK, "/audios/move-turn.wav");
        EFFECT_FILES.put(Effect.LINE_CLEAR, "/audios/erase-line.wav");
        EFFECT_FILES.put(Effect.GAME_OVER, "/audios/game-finish.wav");
        EFFECT_FILES.put(Effect.LEVEL_UP, "/audios/level-up.wav");
    }

    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager();
    }

    private final Map<Effect, AudioClip> clips =
            new EnumMap<>(Effect.class);

    private MediaPlayer musicPlayer;
    private boolean musicOn = true;
    private boolean effectsOn = true;

    /** 0.0 to 1.0, applied to both the music player and every effect. */
    private double volume = 0.5;

    /** Loads every clip once, so no disk access happens mid-game. */
    private AudioManager() {
        for (Map.Entry<Effect, String> entry : EFFECT_FILES.entrySet()) {
            URL url = getClass().getResource(entry.getValue());

            if (url != null) {
                clips.put(
                        entry.getKey(),
                        new AudioClip(url.toExternalForm()));
            } else {
                System.err.println(
                        "Missing audio resource: " + entry.getValue());
            }
        }
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
        if (!musicOn || musicPlayer != null) {
            return;
        }

        URL url = getClass().getResource(MUSIC);

        if (url == null) {
            System.err.println("Missing audio resource: " + MUSIC);
            return;
        }

        musicPlayer = new MediaPlayer(
                new Media(url.toExternalForm()));

        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(volume);
        musicPlayer.play();
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (musicOn && musicPlayer != null) {
            musicPlayer.play();
        }
    }

    /** Plays a one-shot effect, if effects are enabled. */
    public void playEffect(Effect effect) {
        if (!effectsOn) {
            return;
        }

        AudioClip clip = clips.get(effect);

        if (clip != null) {
            clip.play(volume);
        }
    }

    /** Behind the {@code M} key and the configuration checkbox. */
    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;

        if (musicOn) {
            startMusic();
        } else {
            stopMusic();
        }
    }

    /** Behind the {@code S} key and the configuration checkbox. */
    public void setEffectsOn(boolean effectsOn) {
        this.effectsOn = effectsOn;
    }

    /** Volume as the configuration slider reports it, 0 to 100. */
    public int getVolume() {
        return (int) Math.round(volume * 100);
    }

    /**
     * Sets the volume for music and effects together.
     *
     * <p>Takes a percentage because the configuration slider works in whole
     * numbers; JavaFX wants a fraction from 0.0 to 1.0, so the conversion lives
     * here rather than at every call site.</p>
     *
     * <p>A change reaches the music player immediately. Effects pick it up on
     * their next play, since the level is passed per call rather than held on
     * the clip.</p>
     *
     * @param percent 0 to 100, clamped
     */
    public void setVolume(int percent) {
        this.volume =
                Math.max(0, Math.min(100, percent)) / 100.0;

        if (musicPlayer != null) {
            musicPlayer.setVolume(this.volume);
        }
    }
}