package au.edu.Griffith.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A {@link Repository} backed by one JSON file on disk.
 *
 * <p>Covers three of the marking criteria at once — JSON, File I/O and Generics
 * — in a single reusable class. {@link ConfigService} and
 * {@link HighScoreService} each hold one of these rather than repeating
 * try-with-resources blocks of their own.</p>
 *
 * <p>The {@link TypeReference} is what lets the type survive erasure, so a
 * {@code JsonRepository<List<ScoreEntry>>} can be read back as a genuine list of
 * entries rather than a list of maps.</p>
 *
 * @param <T> the type stored in the file
 */
public class JsonRepository<T> implements Repository<T> {

    private final Path file;
    private final TypeReference<T> typeReference;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param file          where to read and write
     * @param typeReference captures {@code T} so Jackson can rebuild it
     */
    public JsonRepository(Path file, TypeReference<T> typeReference) {
        this.file = file;
        this.typeReference = typeReference;
    }

    public Path getFile() {
        return file;
    }

    @Override
    public Optional<T> load() {
        throw new UnsupportedOperationException(
                "TODO: if the file exists, mapper.readValue into typeReference; return empty on absence or parse failure");
    }

    @Override
    public void save(T value) {
        throw new UnsupportedOperationException(
                "TODO: create parent directories, then mapper.writerWithDefaultPrettyPrinter().writeValue(file, value)");
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("TODO: Files.exists(file)");
    }
}
