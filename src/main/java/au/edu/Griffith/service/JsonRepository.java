package au.edu.Griffith.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * A {@link Repository} backed by a single JSON file, read and written with Jackson.
 *
 * <p>The {@link TypeReference} is supplied by the caller because generic type
 * arguments are erased at runtime — Jackson cannot otherwise know what
 * {@code T} is when deserialising.</p>
 *
 * @param <T> the type being stored
 */
public class JsonRepository<T> implements Repository<T> {

    private final Path path;
    private final TypeReference<T> type;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonRepository(Path path, TypeReference<T> type) {
        this.path = path.toAbsolutePath();
        this.type = type;
    }

    /**
     * Reads the file.
     *
     * <p>A missing file is a normal first run. A corrupt or hand-edited file also
     * returns empty rather than throwing, so the caller falls back to defaults and
     * the game still starts — but the cause is reported, because a silently
     * ignored failure here is exactly how a persistence bug hides.</p>
     */
    @Override
    public Optional<T> load() {
        if (!exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(path.toFile(), type));
        } catch (IOException e) {
            System.err.println("Could not read " + path + ", falling back to defaults: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Writes the value, creating the parent directory if it is not there yet.
     *
     * <p>Written to a temporary file and then moved into place, so a crash
     * part-way through a write cannot leave a truncated file that fails to parse
     * on the next launch.</p>
     */
    @Override
    public void save(T value) {
        try {
            Files.createDirectories(path.getParent());

            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), value);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + path, e);
        }
    }

    @Override
    public boolean exists() {
        return Files.isRegularFile(path);
    }
}