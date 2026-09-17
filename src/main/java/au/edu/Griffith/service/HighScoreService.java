package au.edu.Griffith.service;

import au.edu.Griffith.model.HighScoreTable;
import au.edu.Griffith.model.PlayerType;
import au.edu.Griffith.model.ScoreEntry;

import java.nio.file.Path;
import java.util.List;

/**
 * Singleton owning the {@link HighScoreTable} and its {@code scores.json} file.
 *
 * <p>Same holder idiom as {@link ConfigService}: lazy, and thread-safe through
 * class initialisation rather than locking. Single ownership matters here
 * because in two-player mode two games can finish at almost the same moment and
 * both want to record a score into the same table.</p>
 */
public final class HighScoreService {

    private static final Path SCORES_FILE = Path.of("data", "scores.json");

    private static final class Holder {
        private static final HighScoreService INSTANCE = new HighScoreService();
    }

    private final Repository<List<ScoreEntry>> repository;
    private final HighScoreTable table = new HighScoreTable();
    private boolean loaded;

    private HighScoreService() {
        this(new JsonRepository<>(SCORES_FILE, new com.fasterxml.jackson.core.type.TypeReference<>() {
        }));
    }

    /** Visible for tests so they can point at a temp file instead of {@code data/scores.json}. */
    HighScoreService(Repository<List<ScoreEntry>> repository) {
        this.repository = repository;
    }

    public static HighScoreService getInstance() {
        return Holder.INSTANCE;
    }

    /** The table, loaded from disk on first access. */
    public HighScoreTable getTable() {
        if (!loaded) {
            repository.load().ifPresentOrElse(
                    table::replaceAll,
                    this::seedDefaults);
            loaded = true;
        }
        return table;
    }

    /** True if this score earns a place, so the name prompt should be shown. */
    public boolean qualifies(int score) {
        return getTable().qualifies(score);
    }

    /** Records a finished game and writes the table straight back to disk. */
    public void record(ScoreEntry entry) {
        getTable().add(entry);
        repository.save(table.getEntries());
    }

    /** Empties the table and persists the empty result, behind the reset button. */
    public void clearAll() {
        getTable().clear();
        repository.save(List.of());
    }

    /**
     * First run only: ten starter rows so the high-score screen is populated, then
     * write {@code scores.json}. Modest scores so a short game can still qualify.
     */
    private void seedDefaults() {
        table.replaceAll(List.of(
                new ScoreEntry("Alex", 1000, PlayerType.HUMAN),
                new ScoreEntry("Sam", 900, PlayerType.HUMAN),
                new ScoreEntry("Jordan", 800, PlayerType.HUMAN),
                new ScoreEntry("Riley", 700, PlayerType.HUMAN),
                new ScoreEntry("Casey", 600, PlayerType.HUMAN),
                new ScoreEntry("Morgan", 500, PlayerType.HUMAN),
                new ScoreEntry("Taylor", 400, PlayerType.HUMAN),
                new ScoreEntry("Quinn", 300, PlayerType.HUMAN),
                new ScoreEntry("Avery", 200, PlayerType.HUMAN),
                new ScoreEntry("Blake", 100, PlayerType.HUMAN)));
        repository.save(table.getEntries());
    }
}
