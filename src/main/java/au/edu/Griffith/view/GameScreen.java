package au.edu.Griffith.view;

import au.edu.Griffith.model.GameModel;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * The playing screen: one field, or two side-by-side in extend mode.
 *
 * <p>Each field is a {@link PlayFieldView} that observes its own model. This
 * class only lays them out and forwards show/hide/render.</p>
 */
public class GameScreen extends AbstractScreen {

    private final List<PlayFieldView> fields = new ArrayList<>();

    public GameScreen(GameModel model, Runnable onBack, Runnable onReplay) {
        this(List.of(model), List.of("Player 1"), onBack, List.of(onReplay));
    }

    /**
     * @param models   one model per field, left to right
     * @param titles   sidebar heading per field
     * @param onBack   shared return-to-menu action, shown on the first field only
     * @param onReplay per-field replay callbacks
     */
    public GameScreen(List<GameModel> models, List<String> titles, Runnable onBack, List<Runnable> onReplay) {
        for (int i = 0; i < models.size(); i++) {
            Runnable back = (i == 0) ? onBack : null;
            fields.add(new PlayFieldView(models.get(i), titles.get(i), back, onReplay.get(i)));
        }
    }

    @Override
    public String getTitle() {
        return fields.size() > 1 ? "Tetris — Extend Mode" : "Tetris";
    }

    @Override
    public double getPrefWidth() {
        return fields.stream().mapToDouble(PlayFieldView::getPrefWidth).sum();
    }

    @Override
    public double getPrefHeight() {
        return fields.stream().mapToDouble(PlayFieldView::getPrefHeight).max().orElse(ScreenSizes.MENU_HEIGHT);
    }

    @Override
    protected Parent buildLayout() {
        HBox layout = new HBox();
        layout.setAlignment(Pos.TOP_LEFT);
        for (PlayFieldView field : fields) {
            layout.getChildren().add(field.getRoot());
        }
        return layout;
    }

    @Override
    protected String rootStyleClass() {
        return "game-screen";
    }

    @Override
    public void onShow() {
        getRoot();
        fields.forEach(PlayFieldView::attach);
    }

    @Override
    public void onHide() {
        fields.forEach(PlayFieldView::detach);
    }

    /** Repaints every field. */
    public void render() {
        fields.forEach(PlayFieldView::render);
    }
}
