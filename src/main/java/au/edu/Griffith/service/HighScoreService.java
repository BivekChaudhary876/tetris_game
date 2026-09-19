package au.edu.Griffith.service;

import au.edu.Griffith.model.HighScoreTable;
import au.edu.Griffith.model.ScoreBoard;
import au.edu.Griffith.model.ScoreEntry;

import java.nio.file.Path;

/**
 * Singleton owning the {@link HighScoreTable} and its score file.
 *
 * <p>Same holder idiom as {@link ConfigService}: lazy, and thread-safe through
 * class initialisation rather than locking. Single ownership matters here
 * because in Extend Mode two games can finish at almost the same moment and both
 * want to record into the same table.</p>
 *
 * <p>The file is {@code JavaTetrisScore.json} in the working directory, holding
 * {@code {"scores":[...]}} — the same name and shape as the reference build, so
 * a file from either is readable by the other.</p>
 */
public final class HighScoreService {

    private static final Path SCORES_FILE = Path.of("JavaTetrisScore.json");

    private static final class Holder {
        private static final HighScoreService INSTANCE = new HighScoreService();
    }

    private final Repository<ScoreBoard> repository;
    private final HighScoreTable table = new HighScoreTable();
    private boolean loaded;

    private HighScoreService() {
        this(new JsonRepository<>(SCORES_FILE, new com.fasterxml.jackson.core.type.TypeReference<>() {
        }));
    }

    /** Visible for tests so they can point at a temp file instead of the real one. */
    HighScoreService(Repository<ScoreBoard> repository) {
        this.repository = repository;
    }

    public static HighScoreService getInstance() {
        return Holder.INSTANCE;
    }

    /** The table, loaded from disk on first access. */
    public HighScoreTable getTable() {
        if (!loaded) {
            // A missing file is a normal first run: start empty rather than
            // inventing scores nobody played for.
            repository.load().ifPresent(board -> table.replaceAll(board.scores()));
            loaded = true;
        }
        return table;
    }

    /** True if this score earns a place, so the name prompt should be shown. */
    public boolean qualifies(int score) {
        return getTable().qualifies(score);
    }

    /** Records a finished game and writes the file straight away. */
    public void record(ScoreEntry entry) {
        getTable().add(entry);
        save();
    }

    /** Empties the table and persists the empty result, behind the Clear button. */
    public void clearAll() {
        getTable().clear();
        save();
    }

    private void save() {
        repository.save(new ScoreBoard(table.getEntries()));
    }
}