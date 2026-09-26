package au.edu.Griffith;

/**
 * Entry point for the runnable jar ({@code java -jar TetrisJava.jar}).
 *
 * <p>When the class named in the jar manifest extends
 * {@link javafx.application.Application}, the JDK launcher insists on JavaFX
 * being loaded as named modules and aborts with "JavaFX runtime components are
 * missing". Starting from a plain class and delegating avoids that check, so the
 * JavaFX classes bundled in the jar are loaded from the classpath.</p>
 *
 * <p>IDE and {@code mvn javafx:run} still launch {@link TetrisApp} directly.</p>
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        TetrisApp.main(args);
    }
}
