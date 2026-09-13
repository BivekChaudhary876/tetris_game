package au.edu.Griffith.view;

import au.edu.Griffith.controller.MainMenuController;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * The main menu: Play, Configuration, High Scores, Exit.
 *
 * <p>Note what the buttons do here. In Milestone 1 the menu handlers built a
 * {@code Tetris} object and set the scene themselves — the view knew how to
 * start a game. Now each button calls a method on
 * {@link MainMenuController}, and the controller decides what happens. The view
 * reports intent; it does not carry it out.</p>
 */
public class MainMenuScreen extends AbstractScreen {

    private final MainMenuController controller;

    public MainMenuScreen(MainMenuController controller) {
        this.controller = controller;
    }

    @Override
    public String getTitle() {
        return "Tetris - Main Menu";
    }

    @Override
    protected Parent buildLayout() {
        Label heading = new Label("TETRIS");
        heading.getStyleClass().add("heading");

        VBox layout = new VBox(ScreenSizes.MENU_SPACING,
                heading,
                menuButton("Play Tetris", controller::onPlay),
                menuButton("High Scores", controller::onHighScores),
                menuButton("Configuration", controller::onConfigure),
                menuButton("Exit", controller::onExit));

        layout.setAlignment(Pos.CENTER);
        return layout;
    }

    /**
     * Builds one menu button.
     *
     * <p>Takes the handler as a {@link Runnable} so the button is wired with a
     * method reference to the controller and holds no logic of its own.</p>
     */
    private Button menuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefWidth(ScreenSizes.BUTTON_WIDTH);
        button.setOnAction(event -> action.run());
        return button;
    }

    @Override
    protected String rootStyleClass() {
        return "menu-screen";
    }
}
