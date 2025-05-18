package org.leycm.storage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for storage implementations that handle key-value data persistence.
 * Supports multiple file formats (JSON, YAML, TOML) and provides type adapters for custom serialization.
 */
public abstract class Storage {
    protected final Map<String, Object> init = new HashMap<>();

    protected final Map<Class<?>, Adapter.StorageSetter<?>> encrypting = new HashMap<>();
    protected final Map<Class<?>, Adapter.StorageGetter<?>> decrypting = new HashMap<>();

    protected String file = "storage";
    protected Type type = Type.JSON;


    /**
     * Creates or load a storage instance of the specified type.
     *
     * @param <T>           The storage type
     * @param file          The base filename (without extension)
     * @param type          The storage file format type
     * @param storageClass  The class of the storage implementation to create
     * @return A new storage instance of the requested type
     * @throws IllegalArgumentException if the storage cannot be created
     */
    public static <T extends Storage> @NotNull T of(@NotNull String file,
                                                    @NotNull Type type,
                                                    @NotNull Class<T> storageClass) {
        return StorageRegistry.register(file, type, storageClass);
    }

    /**
     * Adds a setter adapter for a specific type to handle custom serialization.
     *
     * @param <T>      The type to adapt
     * @param type     The class object representing the type
     * @param setter The setter adapter that handles serialization
     * @param getter The getter adapter that handles deserialization
     */
    public <T> void addAdapter(Class<T> type, Adapter.StorageSetter<T> setter, Adapter.StorageGetter<T> getter) {
        encrypting.put(type, setter);
        decrypting.put(type, getter);
    }

    /**
     * Registers all type adapters for this storage instance.
     * Implementations should override this method to register their specific adapters.
     */
    public abstract void registerAdapter();

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
         * @param id The file extension for this format
         */
        Type(String id) {
            this.id = id;
        }
    }

    /**
     * Stores a value at the specified key/path.
     * Uses adapters if available for the value type, otherwise uses default storage.
     *
     * @param <T>   The type of the value to store
     * @param key   The path/key where to store the value
     * @param value The value to store (null will remove the key)
     */
    public <T> void set(@NotNull String key, @Nullable T value) {
        if (value == null) {
            remove(key);
            return;
        }

        @SuppressWarnings("unchecked")
        Adapter.StorageSetter<T> setter = (Adapter.StorageSetter<T>) encrypting.get(value.getClass());

        if (setter != null) {
            setter.set(key, value); // Use adapter
        } else {
            setDefault(key, value); // Default storage
        }
    }

    /**
     * Retrieves a value with a fallback to default if not found.
     *
     * @param <T>          The expected return type
     * @param key          The path/key of the value to retrieve
     * @param type         The expected class of the return value
     * @param defaultValue The value to return if the key doesn't exist
     * @return The stored value if found and convertible, otherwise the defaultValue
     * @see #get(String, Class)
     */
    public <T> T get(@NotNull String key,
                     @NotNull Class<T> type,
                     @Nullable T defaultValue) {
        T value = get(key, type);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves a value of specified type from storage.
     *
     * @param <T>  The expected return type
     * @param key  The path/key of the value to retrieve
     * @param type The expected class of the return value
     * @return The stored value if found and convertible, otherwise null
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(@NotNull String key, @NotNull Class<T> type) {
        Object value = getPathValue(key);
        if (value == null) return null;

        if (type.isInstance(value)) {
            return (T) value;
        }

        Adapter.StorageGetter<T> getter = (Adapter.StorageGetter<T>) decrypting.get(type);
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
     * Replaces the current {@link #init} contents with the loaded data.
     *
     * @throws RuntimeException if the reload operation fails
     */
    public void reload() {StorageRegistry.reload(this); }

    /**
     * Saves the current storage data to a file using the selected {@link Type}.
     * Automatically serializes the {@link #init} map based on the chosen format.
     *
     * @throws RuntimeException if the save operation fails
     */
    public void save() {StorageRegistry.save(this); }

}