package org.leycm.storage;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Abstract base class for storage implementations that handle key-value data persistence.
 * Supports multiple file formats (JSON, YAML, TOML) and provides type adapters for custom serialization.
 * <p>
 * This class provides a hierarchical storage system with dot-notation paths (e.g., "user.profile.name")
 * and supports custom type adapters for complex object serialization/deserialization.
 */
public abstract class Storage {
    protected final Map<String, Object> init = new HashMap<>();

    protected final Map<Class<?>, StorageAdapter.Setter<?>> encrypting = new HashMap<>();
    protected final Map<Class<?>, StorageAdapter.Getter<?>> decrypting = new HashMap<>();

    protected String file = "storage";
    protected Type type = Type.JSON;
    protected boolean isDigital = false;

    /**
     * Creates or loads a storage instance of the specified type.
     *
     * @param <T>           The storage type that extends this abstract class
     * @param file          The base filename (without extension) where data will be stored
     * @param type          The storage file format type (JSON, YAML, or TOML)
     * @param digital       The storage file state digital or not
     * @param storageClass  The class of the storage implementation to create
     * @return A new storage instance of the requested type
     * @throws IllegalArgumentException if the storage cannot be created or loaded
     * @throws RuntimeException if there's an error during initialization
     */
    public static <T extends Storage> @NotNull T of(@NotNull String file,
                                                    @NotNull Type type,
                                                    boolean digital,
                                                    @NotNull Class<T> storageClass) {
        return StorageRegistry.register(file, type, digital, storageClass);
    }

    /**
     * Creates or loads a storage instance of the specified type.
     *
     * @param <T>           The storage type that extends this abstract class
     * @param file          The base filename (without extension) where data will be stored
     * @param type          The storage file format type (JSON, YAML, or TOML)
     * @param storageClass  The class of the storage implementation to create
     * @return A new storage instance of the requested type
     * @throws IllegalArgumentException if the storage cannot be created or loaded
     * @throws RuntimeException if there's an error during initialization
     */
    public static <T extends Storage> @NotNull T of(@NotNull String file,
                                                    @NotNull Type type,
                                                    @NotNull Class<T> storageClass) {
        return StorageRegistry.register(file, type, false, storageClass);
    }

    /**
     * Adds a type adapter for custom serialization/deserialization of specific classes.
     *
     * @param <T>     The type to be adapted
     * @param type    The class object representing the type to be adapted
     * @param setter  The setter adapter that handles serialization of the type
     * @param getter  The getter adapter that handles deserialization of the type
     */
    public <T> void addAdapter(Class<T> type, StorageAdapter.Setter<T> setter, StorageAdapter.Getter<T> getter) {
        encrypting.put(type, setter);
        decrypting.put(type, getter);
    }

    /**
     * Registers all custom type adapters for this storage instance.
     * <p>
     * Implementations should override this method to register their specific adapters
     * for custom types. This method is called during storage initialization.
     */
    public abstract void registerAdapter();

    /**
     * Registers base type adapters that are common to all storage implementations.
     * <p>
     * This method registers adapters for fundamental types like StorageSection.
     * It is automatically called during storage initialization.
     */
    public void callAdapter() {
        addAdapter(StorageSection.class, this::setStorageSection, this::getStorageSection);
        registerAdapter();
    }

    /**
     * Internal method to handle serialization of StorageSection objects.
     * @deprecated {@link StorageSection} ar linking the value right back to their parent {@link Storage}.
     *
     * @param key      The key/path where the section will be stored
     * @param section  The StorageSection object to serialize
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    private void setStorageSection(String key, StorageSection section) {
        set(key, section);
    }

    /**
     * Internal method to handle deserialization of StorageSection objects.
     *
     * @param key  The key/path where the section is stored
     * @return The deserialized StorageSection object
     */

    @Contract("_ -> new")
    private @NotNull StorageSection getStorageSection(String key) {
        StorageSection section = new StorageSection();
        section.parentKey = key;
        section.parentStorage = this;
        return section;
    }

    /**
     * Enum representing supported storage file formats.
     */
    @Getter
    public enum Type {
        YAML("yml"),
        JSON("json"),
        TOML("toml");

        private final String id;

        /**
         * Creates a new Type enum value.
         *
         * @param id The file extension associated with this format
         */
        Type(String id) {
            this.id = id;
        }
    }

    /**
     * Checks if this storage instance is marked as digital (should not persist to file).
     * @return true if the @Digital annotation is present on the class
     */
    public boolean isDigital() {
        return isDigital;
    }


    /**
     * Stores a value at the specified key/path.
     * <p>
     * Uses registered adapters if available for the value type, otherwise uses default storage.
     * If the value is null, the key will be removed from storage.
     *
     * @param <T>   The type of the value to store
     * @param key   The path/key where to store the value (dot-notation supported)
     * @param value The value to store (null will remove the key)
     */
    public <T> void set(@NotNull String key, @Nullable T value) {
        if (value == null) {
            remove(key);
            return;
        }

        @SuppressWarnings("unchecked")
        StorageAdapter.Setter<T> setter = (StorageAdapter.Setter<T>) encrypting.get(value.getClass());

        if (setter != null) {
            setter.set(key, value);
        } else {
            setDefault(key, value);
        }
    }

    /**
     * Retrieves a value with a fallback to default if not found.
     *
     * @param <T>          The expected return type
     * @param key          The path/key of the value to retrieve
     * @param clazz         The expected class of the return value
     * @param defaultValue The value to return if the key doesn't exist
     * @return The stored value if found and convertible, otherwise the defaultValue
     * @see #get(String, Class)
     */
    public <T> T get(@NotNull String key,
                     @NotNull Class<T> clazz,
                     @Nullable T defaultValue) {
        T value = get(key, clazz);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves a value of specified type from storage.
     * <p>
     * If a type adapter is registered for the requested type, it will be used for deserialization.
     *
     * @param <T>  The expected return type
     * @param key  The path/key of the value to retrieve
     * @param clazz The expected class of the return value
     * @return The stored value if found and convertible, otherwise null
     * @throws RuntimeException if there's an error during deserialization
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(@NotNull String key, @NotNull Class<T> clazz) {
        Object value = getPathValue(key);
        if (value == null) return null;

        if (clazz.isInstance(value)) {
            return (T) value;
        }

        StorageAdapter.Getter<T> getter = (StorageAdapter.Getter<T>) decrypting.get(clazz);
        if (getter != null) {
            try {
                return getter.get(key);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * Internal method to retrieve a value using dot-path notation.
     *
     * @param path The dot-separated path to the value (e.g., "user.profile.name")
     * @return The value if found, otherwise null
     */
    public @Nullable Object getPathValue(@NotNull String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = init;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) return null;
            //noinspection unchecked
            current = (Map<String, Object>) next;
        }

        return current.get(parts[parts.length - 1]);
    }

    /**
     * Internal method to store a value using dot-path notation with automatic map creation.
     *
     * @param path  The dot-separated path where to store the value
     * @param value The value to store
     */
    private void setDefault(@NotNull String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = init;

        for (int i = 0; i < parts.length - 1; i++) {
            //noinspection unchecked
            current = (Map<String, Object>) current.computeIfAbsent(
                    parts[i], k -> new HashMap<String, Object>()
            );
        }

        current.put(parts[parts.length - 1], value);
    }

    /**
     * Removes a value at the specified path.
     *
     * @param path The dot-separated path of the value to remove
     */
    private void remove(@NotNull String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = init;

        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) return;
            //noinspection unchecked
            current = (Map<String, Object>) next;
        }

        current.remove(parts[parts.length - 1]);
    }

    /**
     * Reloads the storage data from the file using the selected {@link Type}.
     * <p>
     * Replaces the current {@link #init} contents with the loaded data.
     * This operation is thread-safe and will preserve any registered adapters.
     *
     * @throws RuntimeException if the reload operation fails (e.g., file not found or parse error)
     */
    public void reload() {StorageRegistry.reload(this); }

    /**
     * Saves the current storage data to a file using the selected {@link Type}.
     * <p>
     * Automatically serializes the {@link #init} map based on the chosen format.
     * The file will be created if it doesn't exist, or overwritten if it does.
     *
     * @throws RuntimeException if the save operation fails (e.g., IO error or serialization error)
     * @throws IllegalStateException if this is a digital storage (marked with @Digital annotation)
     */
    public void save() {
        if (isDigital()) throw new IllegalStateException("Digital storage cannot be saved to file");
        StorageRegistry.save(this);
    }

    /**
     * Returns a string representation of the storage data in the default format.
     * @return serialized data as string in default format
     */
    @Override
    public String toString() {
        return toStringAs(this.type);
    }

    /**
     * Returns a string representation of the storage data in the specified format.
     * @param format the desired output format
     * @return serialized data as string in requested format
     */
    public String toStringAs(Type format) {
        return StorageRegistry.serialize(this.init, format);
    }

    /**
     * Deserializes data from a string representation in the default format
     * and populates the storage with the parsed data.
     *
     * @param content the string containing serialized data in default format
     * @throws IllegalArgumentException if the content cannot be parsed
     * @throws NullPointerException if the content is null
     */
    public void fromString(String content) {
        fromSpecificString(content, this.type);
    }

    /**
     * Deserializes data from a string representation in the specified format
     * and populates the storage with the parsed data.
     *
     * @param content the string containing serialized data
     * @param format the format of the input data
     * @throws IllegalArgumentException if the content cannot be parsed in the specified format
     * @throws NullPointerException if either content or format is null
     * @throws UnsupportedOperationException if the specified format is not supported
     */
    public void fromSpecificString(String content, Type format) {
        init.putAll(StorageRegistry.deserialize(content, format));
    }

    /**
     * Returns a set of keys at the root level or all keys if deep is true.
     *
     * @param deep If true, returns all keys in the storage with their full path;
     *             If false, returns only the top-level keys
     * @return A set of keys matching the criteria
     */
    public Set<String> getKeys(boolean deep) {
        return getKeys("", deep);
    }

    /**
     * Returns a set of keys at the specified base path or all keys underneath if deep is true.
     *
     * @param baseKey The base path to start from (empty string for root)
     * @param deep    If true, returns all keys in the path with their full path;
     *                If false, returns only the immediate child keys
     * @return A set of keys matching the criteria
     */
    public Set<String> getKeys(@NotNull String baseKey, boolean deep) {
        Set<String> result = new HashSet<>();

        if (baseKey.isEmpty()) {
            if (deep) {
                collectAllPaths(init, "", result);
            } else {
                result.addAll(init.keySet());
            }
            return result;
        }

        // Get the value at the baseKey path
        Object value = getPathValue(baseKey);
        if (!(value instanceof Map)) {
            return result; // doesn't point to a map
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;

        if (deep) {
            collectKeysWithSubpaths(map, baseKey, result);
        } else {
            result.addAll(map.keySet());
        }

        return result;
    }

    /**
     * Collects all paths in the storage from the root.
     *
     * @param currentMap  The current map being processed
     * @param currentPath The current path within the map
     * @param collector   The set where paths will be collected
     */
    private void collectAllPaths(@NotNull Map<String, Object> currentMap, String currentPath, Set<String> collector) {
        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String path = currentPath.isEmpty() ? key : currentPath + "." + key;

            collector.add(path);

            // If the value is a map, recursively collect paths
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;

                collectAllPaths(nestedMap, path, collector);
            }
        }
    }

    /**
     * Collects keys with their subpaths for a specified base key.
     *
     * @param currentMap  The current map being processed
     * @param baseKey     The base key for this collection
     * @param collector   The set where keys will be collected
     */
    private void collectKeysWithSubpaths(@NotNull Map<String, Object> currentMap, String baseKey, Set<String> collector) {
        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            collector.add(key);

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;

                collector.addAll(getNestedPaths(key, nestedMap));
            }
        }
    }

    /**
     * Gets nested paths with their parent prefix.
     *
     * @param prefix The prefix for all nested paths
     * @param map    The map to extract paths from
     * @return A set of paths with their parent prefix
     */
    private @NotNull Set<String> getNestedPaths(String prefix, @NotNull Map<String, Object> map) {
        Set<String> result = new HashSet<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String path = prefix + "." + key;
            result.add(path);

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;

                result.addAll(getNestedPaths(path, nestedMap));
            }
        }

        return result;
    }

    /**
     * Adds all variations of a path to the collector.
     * For example, for "a.b.c", it adds "a", "a.b", and "a.b.c".
     *
     * @param path      The full path to add variations for
     * @param collector The set to collect the path variations
     */
    private void addAllPathVariations(@NotNull String path, Set<String> collector) {
        String[] parts = path.split("\\.");
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                current.append(".");
            }
            current.append(parts[i]);
            collector.add(current.toString());
        }
    }

    /**
     * Checks if the storage contains a value at the specified key/path.
     *
     * @param key The path/key to check
     * @return true if a value exists at the specified path, false otherwise
     */
    public boolean contains(@NotNull String key) {
        return getPathValue(key) != null;
    }

    /**
     * Checks if the storage contains a non-null value at the specified key/path.
     *
     * @param key The path/key to check
     * @return true if a non-null value exists at the specified path, false otherwise
     */
    public boolean containsNotNull(@NotNull String key) {
        Object value = getPathValue(key);
        return value != null;
    }

    /**
     * Returns the number of top-level keys in the storage.
     *
     * @return The count of top-level keys
     */
    public int size() {
        return init.size();
    }

    /**
     * Returns the number of keys at the specified path (if it points to a map).
     *
     * @param path The path to check (empty string for root)
     * @return The count of keys at the specified path, or 0 if path doesn't point to a map
     */
    public int size(@NotNull String path) {
        Object value = path.isEmpty() ? init : getPathValue(path);
        return (value instanceof Map) ? ((Map<?, ?>) value).size() : 0;
    }

    /**
     * Clears all data from the storage.
     */
    public void clear() {
        init.clear();
    }

    /**
     * Clears all data at the specified path (if it points to a map).
     *
     * @param path The path to clear (empty string for root)
     */
    public void clear(@NotNull String path) {
        Object value = path.isEmpty() ? init : getPathValue(path);
        if (value instanceof Map) {
            ((Map<?, ?>) value).clear();
        }
    }

    /**
     * Performs an action for each key-value pair in the storage.
     *
     * @param action The action to perform for each entry
     */
    public void forEach(@NotNull BiConsumer<String, Object> action) {
        forEach("", action, false);
    }

    /**
     * Performs an action for each key-value pair starting from the specified path.
     *
     * @param basePath The path to start from (empty string for root)
     * @param action The action to perform for each entry
     */
    public void forEach(@NotNull String basePath, @NotNull BiConsumer<String, Object> action) {
        forEach(basePath, action, false);
    }

    /**
     * Performs an action for each key-value pair in the storage, recursively if specified.
     *
     * @param basePath The path to start from (empty string for root)
     * @param action The action to perform for each entry
     * @param recursive Whether to recurse into nested maps
     */
    public void forEach(@NotNull String basePath,
                        @NotNull BiConsumer<String, Object> action,
                        boolean recursive) {
        Object value = basePath.isEmpty() ? init : getPathValue(basePath);
        if (!(value instanceof Map)) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fullPath = basePath.isEmpty() ? entry.getKey() : basePath + "." + entry.getKey();
            action.accept(fullPath, entry.getValue());

            if (recursive && entry.getValue() instanceof Map) {
                forEach(fullPath, action, true);
            }
        }
    }

    /**
     * Performs an action for each key in the storage.
     *
     * @param action The action to perform for each key
     */
    public void forEachKey(@NotNull Consumer<String> action) {
        getKeys(true).forEach(action);
    }

    /**
     * Performs an action for each value in the storage.
     *
     * @param action The action to perform for each value
     */
    public void forEachValue(@NotNull Consumer<Object> action) {
        forEach((k, v) -> action.accept(v));
    }

    /**
     * Puts all entries from the given map into storage at the specified path.
     *
     * @param path The base path where to put the entries
     * @param map The map containing entries to add
     */
    public void putAll(@NotNull String path, @NotNull Map<String, ?> map) {
        map.forEach((key, value) -> set(path.isEmpty() ? key : path + "." + key, value));
    }

    /**
     * Gets a map containing all key-value pairs at the specified path.
     *
     * @param path The path to get the map from (empty for root)
     * @return A new map containing all key-value pairs, or empty map if path doesn't point to a map
     */
    @SuppressWarnings("unchecked")
    public @NotNull Map<String, Object> toMap(@NotNull String path) {
        Object value = path.isEmpty() ? init : getPathValue(path);
        if (value instanceof Map) {
            return new HashMap<>((Map<String, Object>) value);
        }
        return new HashMap<>();
    }

    /**
     * Gets a read-only view of the map at the specified path.
     *
     * @param path The path to get the map from (empty for root)
     * @return An unmodifiable map view, or empty map if path doesn't point to a map
     */
    @SuppressWarnings("unchecked")
    public @NotNull Map<String, Object> getMapView(@NotNull String path) {
        Object value = path.isEmpty() ? init : getPathValue(path);
        if (value instanceof Map) {
            return Collections.unmodifiableMap((Map<String, Object>) value);
        }
        return Collections.emptyMap();
    }

    /**
     * Merges another storage's data into this one.
     *
     * @param other The storage to merge from
     * @param overwrite Whether to overwrite existing keys
     */
    public void merge(@NotNull Storage other, boolean overwrite) {
        merge("", other.init, overwrite);
    }

    private void merge(@NotNull String currentPath, @NotNull Map<String, Object> source, boolean overwrite) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String fullPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                // If target is also a map, recurse
                Object targetValue = getPathValue(fullPath);
                if (targetValue instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> targetMap = (Map<String, Object>) targetValue;
                    //noinspection unchecked
                    merge(fullPath, (Map<String, Object>) entry.getValue(), overwrite);
                } else if (overwrite || targetValue == null) {
                    set(fullPath, entry.getValue());
                }
            } else if (overwrite || !contains(fullPath)) {
                set(fullPath, entry.getValue());
            }
        }
    }

    /**
     * Creates a deep copy of the storage data.
     *
     * @return A new map containing all the data
     */
    public @NotNull Map<String, Object> deepCopy() {
        return deepCopyMap(init);
    }

    private @NotNull Map<String, Object> deepCopyMap(@NotNull Map<String, Object> original) {
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                //noinspection unchecked
                copy.put(entry.getKey(), deepCopyMap((Map<String, Object>) value));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

}