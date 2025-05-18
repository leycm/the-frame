package org.leycm.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A utility class that manages the registration, loading, reloading, and saving
 * of {@link StorageBase} instances. It supports JSON, YAML, and TOML file formats.
 * This class follows a singleton-like pattern for its static methods and maintains
 * a cache of loaded storage instances.
 */
public final class StorageRegistry {
    private static final String DEFAULT_STORAGE_DIR = ".storage";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Yaml yaml = new Yaml();
    private static final Toml toml = new Toml();

    @Getter private static Logger logger;
    @Getter private static String configDir = "";
    @Getter private static boolean isSetup;

    /**
     * A cache to store loaded {@link StorageBase} instances, keyed by their full file path.
     */
    public static final Map<String, StorageBase> storageCash = new HashMap<>();

    /**
     * Ensures that the {@link #setup(String, Logger)} method has been called before
     * allowing any operations that rely on the registry being initialized.
     *
     * @throws IllegalStateException if the registry has not been set up.
     */
    private static void requireSetup() {
        if (!isSetup()) {
            throw new IllegalStateException("Setup required before using this class.");
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException if an attempt is made to instantiate this class.
     */
    private StorageRegistry() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Initializes the storage registry with the specified configuration directory path and logger.
     * If the provided path is null, the default directory (".storage") will be used.
     * If the provided logger is null, a default logger named "the-frame" will be used.
     * This method must be called once at the application's startup.
     *
     * @param path   The path to the directory where storage files will be located.
     * @param logger The logger instance to be used by the registry for logging messages.
     * @throws IllegalStateException if the registry has already been set up.
     */
    public static void setup(String path,
                             Logger logger) {

        if (isSetup) throw new IllegalStateException("Already set up!");

        StorageRegistry.configDir = path != null ? path : DEFAULT_STORAGE_DIR;
        StorageRegistry.logger = logger != null ? logger : Logger.getLogger("the-frame");
        StorageRegistry.isSetup = true;
    }

    /**
     * Registers and loads a storage instance of the specified class. If an instance for the given
     * file already exists in the cache, it is retrieved from the cache; otherwise, a new instance
     * is loaded from the file.
     *
     * @param file           The name of the storage file (without the extension).
     * @param type           The {@link StorageBase.Type} of the storage file (e.g., JSON, YAML, TOML).
     * @param storageClass   The class of the {@link StorageBase} to be registered and loaded.
     * @param <T>            The generic type of the {@link StorageBase}.
     * @return The loaded or cached instance of the specified {@link StorageBase}.
     * @throws IllegalStateException if the registry has not been set up.
     * @throws RuntimeException      if an error occurs during the creation of the storage instance.
     */
    public static <T extends StorageBase> @NotNull T register(@NotNull String file,
                                                              @NotNull StorageBase.Type type,
                                                              @NotNull Class<T> storageClass) {
        requireSetup();
        String fullFile = configDir + "/" + file;
        return storageCash.containsKey(fullFile) ? fromCache(fullFile, storageClass) : load(fullFile, type, storageClass);
    }

    /**
     * Retrieves a {@link StorageBase} instance from the cache.
     *
     * @param file           The full file path (including the configuration directory and file name)
     * of the cached storage.
     * @param storageClass   The expected class of the cached {@link StorageBase}.
     * @param <T>            The generic type of the {@link StorageBase}.
     * @return The cached instance of the specified {@link StorageBase}.
     * @throws IllegalStateException if no cached storage is found for the given file or if the
     * cached storage is not of the expected type.
     */
    private static <T extends StorageBase> @NotNull T fromCache(@NotNull String file,
                                                                @NotNull Class<T> storageClass) {
        StorageBase cached = storageCash.get(file);
        if (cached == null) {
            throw new IllegalStateException("No cached storage found for file: " + file);
        }

        if (!storageClass.isInstance(cached)) {
            throw new IllegalStateException("Cached storage for '" + file + "' is not of expected type " + storageClass.getName());
        }

        return storageClass.cast(cached);
    }

    /**
     * Loads a new {@link StorageBase} instance from the specified file.
     *
     * @param file           The full file path (including the configuration directory and file name)
     * of the storage file (without the extension).
     * @param type           The {@link StorageBase.Type} of the storage file.
     * @param storageClass   The class of the {@link StorageBase} to be loaded.
     * @param <T>            The generic type of the {@link StorageBase}.
     * @return The newly loaded instance of the specified {@link StorageBase}.
     * @throws RuntimeException if an error occurs during the creation or loading of the storage instance.
     */
    private static <T extends StorageBase> @NotNull T load(@NotNull String file,
                                                           @NotNull StorageBase.Type type,
                                                           @NotNull Class<T> storageClass) {
        try {
            T storage = storageClass.getDeclaredConstructor().newInstance();

            storage.file = file;
            storage.type = type;
            reload(storage);

            cash(file, storage);
            storage.registerAdapter();

            return storage;
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create Storage instance", e);
        }
    }

    /**
     * Reloads the data from the storage file into the provided {@link StorageBase} instance.
     *
     * @param storage The {@link StorageBase} instance to be reloaded.
     * @throws IllegalStateException if the registry has not been set up.
     * @throws RuntimeException      if an I/O error occurs while reading the storage file.
     */
    @SuppressWarnings("unchecked")
    public static void reload(@NotNull StorageBase storage) {
        requireSetup();

        try {
            Path filePath = Path.of(storage.file + "." + storage.type.getId());
            String fileContent = Files.exists(filePath) ? Files.readString(filePath) : "{}";

            switch (storage.type) {
                case JSON -> storage.init.putAll(gson.fromJson(fileContent, Map.class));
                case YAML -> storage.init.putAll(yaml.load(fileContent));
                case TOML -> storage.init.putAll(toml.read(fileContent).toMap());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the data from the provided {@link StorageBase} instance to its corresponding file.
     *
     * @param storage The {@link StorageBase} instance to be saved.
     * @throws IllegalStateException if the registry has not been set up.
     */
    public static void save(@NotNull StorageBase storage) {
        requireSetup();
        File file = new File(storage.file + "." + storage.type.getId()); // e.g., "config.json"

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.severe("Failed to create directories: " + parentDir.getAbsolutePath());
                return;
            }
        }

        switch (storage.type) {
            case JSON -> {
                try (Writer writer = new FileWriter(file)) {
                    gson.toJson(storage.init, writer);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
            case YAML -> {
                try (Writer writer = new FileWriter(file)) {
                    yaml.dump(storage.init, writer);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
            case TOML -> {
                TomlWriter tomlWriter = new TomlWriter();
                try {
                    tomlWriter.write(storage.init, file);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
    }

    /**
     * Caches the given {@link StorageBase} instance with its full file path as the key.
     *
     * @param file    The full file path (including the configuration directory and file name)
     * of the storage.
     * @param storage The {@link StorageBase} instance to be cached.
     */
    private static void cash(@NotNull String file,
                             @NotNull StorageBase storage) {
        storageCash.put(file, storage);
    }
}