package au.edu.Griffith.model;

/**
 * User-editable game settings, exactly as they are persisted to {@code config.json}.
 *
 * <p>A plain mutable bean rather than a record because the JSON layer and the
 * configuration screen both need to read and write individual fields, and
 * because Jackson binds beans to JSON without any extra configuration.</p>
 *
 * <p>Note this class holds <em>only</em> data and validation — it does no file
 * access. Loading and saving is {@link au.edu.Griffith.service.ConfigService}'s
 * job (Single Responsibility).</p>
 */
public class GameConfig {

    public static final int MIN_WIDTH = 5;
    public static final int MAX_WIDTH = 15;
    public static final int MIN_HEIGHT = 15;
    public static final int MAX_HEIGHT = 30;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 10;
    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 100;

    private int fieldWidth = 10;
    private int fieldHeight = 20;
    private int startingLevel = 1;
    private boolean musicOn = true;
    private boolean soundEffectsOn = true;
    private boolean extendMode = false;
    private PlayerType playerOneType = PlayerType.HUMAN;
    private PlayerType playerTwoType = PlayerType.AI;

    /**
     * Playback level for music and effects, as a percentage.
     *
     * <p>Stored whole rather than as a 0.0–1.0 fraction because the slider works
     * in whole numbers and a percentage reads clearly in {@code config.json};
     * {@link au.edu.Griffith.service.AudioManager} converts it for JavaFX.</p>
     */
    private int volume = 50;

    public int getFieldWidth() {
        return fieldWidth;
    }

    public void setFieldWidth(int fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public void setFieldHeight(int fieldHeight) {
        this.fieldHeight = fieldHeight;
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public boolean isMusicOn() {
        return musicOn;
    }

    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;
    }

    public boolean isSoundEffectsOn() {
        return soundEffectsOn;
    }

    public void setSoundEffectsOn(boolean soundEffectsOn) {
        this.soundEffectsOn = soundEffectsOn;
    }

    /** Volume for music and effects, 0 to 100. */
    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    /** Extend mode is the two-player field layout. */
    public boolean isExtendMode() {
        return extendMode;
    }

    public void setExtendMode(boolean extendMode) {
        this.extendMode = extendMode;
    }

    public PlayerType getPlayerOneType() {
        return playerOneType;
    }

    public void setPlayerOneType(PlayerType playerOneType) {
        this.playerOneType = playerOneType;
    }

    public PlayerType getPlayerTwoType() {
        return playerTwoType;
    }

    public void setPlayerTwoType(PlayerType playerTwoType) {
        this.playerTwoType = playerTwoType;
    }

    /**
     * Checks every field is inside its allowed range.
     *
     * @throws IllegalStateException if any setting is out of range
     */
    // fieldWidth and height range updated validated
    public void validate() {
        requireInRange("Field width", fieldWidth, MIN_WIDTH, MAX_WIDTH);
        requireInRange("Field height", fieldHeight, MIN_HEIGHT, MAX_HEIGHT);
        requireInRange("Starting level", startingLevel, MIN_LEVEL, MAX_LEVEL);
        requireInRange("Volume", volume, MIN_VOLUME, MAX_VOLUME);

        if (playerOneType == null || playerTwoType == null) {
            throw new IllegalStateException("Player types must not be null");
        }
    }

    private static void requireInRange(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalStateException(
                    name + " must be between " + min + " and " + max + " but was " + value);
        }
    }

    /** Deep copy, so the configuration screen can edit a draft and discard it on Cancel. */
    // fieldWidth and height copy() so the config screen can edit a draft.
    public GameConfig copy() {
        GameConfig c = new GameConfig();
        c.fieldWidth = this.fieldWidth;
        c.fieldHeight = this.fieldHeight;
        c.startingLevel = this.startingLevel;
        c.musicOn = this.musicOn;
        c.soundEffectsOn = this.soundEffectsOn;
        c.volume = this.volume;
        c.extendMode = this.extendMode;
        c.playerOneType = this.playerOneType;
        c.playerTwoType = this.playerTwoType;
        return c;
    }
}
