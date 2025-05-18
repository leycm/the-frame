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

public final class StorageRegistry {
    private static final String DEFAULT_STORAGE_DIR = ".storage";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Yaml yaml = new Yaml();
    private static final Toml toml = new Toml();

    @Getter private static Logger logger;
    @Getter private static String configDir = "";
    @Getter private static boolean isSetup;

    public static final Map<String, Storage> storageCash = new HashMap<>();

    private static void requireSetup() {
        if (!isSetup()) {
            throw new IllegalStateException("Setup required before using this class.");
        }
    }

    private StorageRegistry() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Initializes the translation handler with the specified language directory path.
     * If the provided path is null, the default directory (.lang) will be used.
     *
     * @param path The path to the directory containing language files
     */
    public static void setup(String path,
                             Logger logger) {

        if (isSetup) throw new IllegalStateException("Already set up!");

        StorageRegistry.configDir = path != null ? path : DEFAULT_STORAGE_DIR;
        StorageRegistry.logger = logger != null ? logger : Logger.getLogger("the-frame");
        StorageRegistry.isSetup = true;
    }

    public static <T extends Storage> @NotNull T register(@NotNull String file,
                                                          @NotNull Storage.Type type,
                                                          @NotNull Class<T> storageClass) {
        requireSetup();
        String fullFile = configDir + "/" + file;
        return storageCash.containsKey(fullFile) ? fromCache(fullFile, storageClass) : load(fullFile, type, storageClass);
    }

    private static <T extends Storage> @NotNull T fromCache(@NotNull String file,
                                                            @NotNull Class<T> storageClass) {
        Storage cached = storageCash.get(file);
        if (cached == null) {
            throw new IllegalStateException("No cached storage found for file: " + file);
        }

        if (!storageClass.isInstance(cached)) {
            throw new IllegalStateException("Cached storage for '" + file + "' is not of expected type " + storageClass.getName());
        }

        return storageClass.cast(cached);
    }

    private static <T extends Storage> @NotNull T load(@NotNull String file,
                                                       @NotNull Storage.Type type,
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

    @SuppressWarnings("unchecked")
    public static void reload(@NotNull Storage storage) {
        requireSetup();

        try {
            Path filePath = Path.of(storage.file + "." + storage.type.getId());

            String file = Files.exists(filePath) ? Files.readString(filePath) : "{}";

            switch (storage.type) {
                case JSON -> storage.init.putAll(gson.fromJson(file, Map.class));
                case YAML -> storage.init.putAll(yaml.load(file));
                case TOML -> storage.init.putAll(toml.read(file).toMap());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void save(@NotNull Storage storage) {
        requireSetup();
        File file = new File(storage.file + "." + storage.type.getId()); // z.B. "config.json"

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




    private static void cash(@NotNull String file,
                             @NotNull Storage storage) {
        storageCash.put(file, storage);
    }

}
