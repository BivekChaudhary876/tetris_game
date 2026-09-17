package au.edu.Griffith.service;

import au.edu.Griffith.model.GameConfig;

import java.nio.file.Path;

/**
 * Singleton owning the one live {@link GameConfig} and its {@code config.json} file.
 *
 * <p>A Singleton is the right call here because the settings are genuinely a
 * single shared resource: the configuration screen, both game fields, the audio
 * manager and the window sizer must all see the same values, and two competing
 * copies would silently disagree.</p>
 *
 * <p><strong>Thread safety and lazy initialisation.</strong> This uses the
 * initialisation-on-demand holder idiom: {@code Holder} is not loaded until
 * {@link #getInstance()} first runs, so the instance is created lazily, and the
 * JVM's own class-initialisation lock makes that creation thread-safe without
 * any {@code synchronized} block on the hot path. That matters once the AI and
 * the network client are running on their own threads.</p>
 */
public final class ConfigService {

    private static final Path CONFIG_FILE = Path.of("data", "config.json");

    /** Not loaded until getInstance() is first called — this is what makes it lazy. */
    private static final class Holder {
        private static final ConfigService INSTANCE = new ConfigService();
    }

    private final Repository<GameConfig> repository;
    private GameConfig config;

    private ConfigService() {
        this(new JsonRepository<>(CONFIG_FILE, new com.fasterxml.jackson.core.type.TypeReference<>() {
        }));
    }

    /** Visible for tests so they can point at a temp file instead of {@code data/config.json}. */
    ConfigService(Repository<GameConfig> repository) {
        this.repository = repository;
    }

    public static ConfigService getInstance() {
        return Holder.INSTANCE;
    }

    /** The live settings, loaded from disk on first access, defaults if absent. */
    public GameConfig getConfig() {
        if (config == null) {
            config = repository.load().orElseGet(GameConfig::new);
            if (!repository.exists()) {
                repository.save(config);
            }
        }
        return config;
    }

    /** Validates and replaces the live settings, then writes them to disk. */
    public void update(GameConfig updated) {
        updated.validate();
        config = updated;
        repository.save(updated);
    }

    /** Forces a re-read from disk, used by tests. */
    public void reload() {
        config = null;
    }
}
