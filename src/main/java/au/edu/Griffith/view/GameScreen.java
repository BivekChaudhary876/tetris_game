package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.service.AudioManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * The playing screen: one field, or two side-by-side in extend mode.
 *
 * <p>Each field is a {@link PlayFieldView} that observes its own model.
 * This class only lays them out and forwards show/hide/render.</p>
 */
public class GameScreen extends AbstractScreen {

    /** Space the "Play" heading and audio status line take above the fields. */
    private static final double HEADER_HEIGHT = 70;

    /** Space the Back button row takes below the fields. */
    private static final double FOOTER_HEIGHT = 60;

    /** Slack reserved for the OS window title bar and taskbar. */
    private static final double WINDOW_CHROME = 80;

    /** Never shrink a cell smaller than this, however large the board is. */
    private static final double MIN_TILE = 10;

    private final List<PlayFieldView> fields = new ArrayList<>();
    private final Runnable onBack;

    /** Shows the M/S toggles' effect, since neither has any other on-screen sign it worked. */
    private final Label audioStatusLabel = new Label();

    /**
     * @param models      one model per field, left to right
     * @param playerTypes which kind of player drives each field, for its Game Info panel
     * @param onBack      return-to-menu action, shown as one Back button below every field
     * @param onReplay    per-field replay callbacks
     */
    public GameScreen(
            List<GameModel> models,
            List<PlayerType> playerTypes,
            Runnable onBack,
            List<Runnable> onReplay) {

        this.onBack = onBack;

        double tileSize = fitTileSize(models);

        for (int i = 0; i < models.size(); i++) {
            fields.add(
                    new PlayFieldView(
                            models.get(i),
                            "Player " + (i + 1),
                            playerTypes.get(i),
                            onReplay.get(i),
                            tileSize));
        }
    }

    /**
     * Shrinks the board cell size just enough that the field(s) fit the
     * screen, so a maximum-sized field (or two, in extend mode) cannot spawn
     * a window taller or wider than the display. Boards small enough to fit
     * at full size are unaffected.
     */
    private static double fitTileSize(List<GameModel> models) {
        int cols = models.stream().mapToInt(m -> m.getBoard().getWidth()).max().orElse(0);
        int rows = models.stream().mapToInt(m -> m.getBoard().getHeight()).max().orElse(0);
        int fieldCount = models.size();

        if (cols == 0 || rows == 0 || fieldCount == 0) {
            return ScreenSizes.TILE;
        }

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        double availableHeight = bounds.getHeight() - HEADER_HEIGHT - FOOTER_HEIGHT - WINDOW_CHROME;
        double availableWidth = bounds.getWidth() - WINDOW_CHROME;

        double widthPerField = availableWidth / fieldCount - ScreenSizes.SIDEBAR_WIDTH;

        double tileFromHeight = availableHeight / rows;
        double tileFromWidth = widthPerField / cols;

        double tile = Math.min(ScreenSizes.TILE, Math.min(tileFromHeight, tileFromWidth));
        return Math.max(MIN_TILE, tile);
    }

    @Override
    public String getTitle() {
        return fields.size() > 1
                ? "Tetris - Extend Mode"
                : "Tetris";
    }

    @Override
    public double getPrefWidth() {
        return fields.stream()
                .mapToDouble(PlayFieldView::getPrefWidth)
                .sum();
    }

    @Override
    public double getPrefHeight() {
        return HEADER_HEIGHT + FOOTER_HEIGHT + fields.stream()
                .mapToDouble(PlayFieldView::getPrefHeight)
                .max()
                .orElse(ScreenSizes.MENU_HEIGHT);
    }

    @Override
    protected Parent buildLayout() {
        Label heading = new Label("Play");
        heading.getStyleClass().add("heading");

        audioStatusLabel.getStyleClass().add("label-muted");
        updateAudioStatus();

        VBox header = new VBox(4, heading, audioStatusLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(12, 0, 12, 0));

        HBox fieldsRow = new HBox();
        fieldsRow.setAlignment(Pos.TOP_LEFT);

        for (PlayFieldView field : fields) {
            fieldsRow.getChildren().add(field.getRoot());
        }

        Button backButton = new Button("Back");
        backButton.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        backButton.setFocusTraversable(false);
        backButton.setOnAction(event -> onBack.run());

        HBox backRow = new HBox(backButton);
        backRow.setAlignment(Pos.CENTER);
        backRow.setPadding(new Insets(12, 0, 12, 0));

        return new VBox(header, fieldsRow, backRow);
    }

    /** Reflects the M/S toggles' current effect: "Music: ON  Sound: OFF" and so on. */
    private void updateAudioStatus() {
        AudioManager audio = AudioManager.getInstance();

        audioStatusLabel.setText(
                "Music: " + (audio.isMusicOn() ? "ON" : "OFF")
                        + "   Sound: " + (audio.isEffectsOn() ? "ON" : "OFF"));
    }

    @Override
    protected String rootStyleClass() {
        return "game-screen";
    }

    @Override
    public void onShow() {
        getRoot();

        fields.forEach(
                PlayFieldView::attach);
    }

    @Override
    public void onHide() {
        fields.forEach(
                PlayFieldView::detach);
    }

    /** Repaints every field and the Music/Sound status line. */
    public void render() {
        updateAudioStatus();

        fields.forEach(
                PlayFieldView::render);
    }
}