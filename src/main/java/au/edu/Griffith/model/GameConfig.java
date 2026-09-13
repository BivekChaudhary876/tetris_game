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

    private int fieldWidth = 10;
    private int fieldHeight = 20;
    private int startingLevel = 1;
    private boolean musicOn = true;
    private boolean soundEffectsOn = true;
    private boolean extendMode = false;
    private PlayerType playerOneType = PlayerType.HUMAN;
    private PlayerType playerTwoType = PlayerType.AI;

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
    public void validate() {
        throw new UnsupportedOperationException("TODO: range-check width, height and level");
    }

    /** Deep copy, so the configuration screen can edit a draft and discard it on Cancel. */
    public GameConfig copy() {
        throw new UnsupportedOperationException("TODO: return a field-by-field clone");
    }
}
