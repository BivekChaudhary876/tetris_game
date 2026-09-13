package au.edu.Griffith.service;

import au.edu.Griffith.model.HighScoreTable;
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
        this.repository = new JsonRepository<>(SCORES_FILE, new com.fasterxml.jackson.core.type.TypeReference<>() {
        });
    }

    public static HighScoreService getInstance() {
        return Holder.INSTANCE;
    }

    /** The table, loaded from disk on first access. */
    public HighScoreTable getTable() {
        throw new UnsupportedOperationException("TODO: load once into table via repository, then return it");
    }

    /** True if this score earns a place, so the name prompt should be shown. */
    public boolean qualifies(int score) {
        throw new UnsupportedOperationException("TODO: delegate to getTable().qualifies(score)");
    }

    /** Records a finished game and writes the table straight back to disk. */
    public void record(ScoreEntry entry) {
        throw new UnsupportedOperationException("TODO: table.add(entry), then repository.save(table.getEntries())");
    }

    /** Empties the table and persists the empty result, behind the reset button. */
    public void clearAll() {
        throw new UnsupportedOperationException("TODO: table.clear(), repository.save(List.of())");
    }
}
