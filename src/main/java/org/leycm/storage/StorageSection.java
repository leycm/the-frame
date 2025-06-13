package org.leycm.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a logical section within a parent storage, providing a namespaced view of the storage.
 * All operations on a StorageSection are automatically prefixed with the section's parent path,
 * allowing for hierarchical organization of storage data.
 *
 * <p>StorageSections delegate all operations to their parent storage, automatically
 * prepending the section's path to keys. This enables clean separation of concerns
 * while maintaining a single underlying storage file.</p>
 *
 * <p>Example usage:
 * <pre>
 * Storage storage = Storage.of("data", Type.JSON, JsonStorage.class);
 * StorageSection userSection = storage.getSection("user");
 *
 * // Stores at "user.profile" in the parent storage
 * userSection.set("profile", profileData);
 *
 * // Retrieves from "user.profile" in the parent storage
 * Profile profile = userSection.get("profile", Profile.class);
 * </pre>
 * </p>
 */
public class StorageSection extends Storage {

    /**
     * The parent storage instance to which this section delegates all operations.
     */
    protected Storage parentStorage;

    /**
     * The path prefix for this section in the parent storage.
     * All keys will be prefixed with this path when accessing the parent storage.
     */
    protected String parentKey;

    /**
     * Registers all custom type adapters for this storage instance.
     * <p>
     * Implementations should override this method to register their specific adapters
     * for custom types. This method is called during storage initialization.
     */
    @Override
    public void registerAdapter() {
        
    }

    /**
     * Stores a value in this storage section. The key will be automatically prefixed
     * with the section's path in the parent storage.
     * {@link Storage#set(String, Object)}
     *
     * @param <T>   The type of the value to store
     * @param key   The key within this section (without the section prefix)
     * @param value The value to store (null will remove the key)
     */
    @Override
    public <T> void set(@NotNull String key, @Nullable T value) {
        parentStorage.set(parentKey + "." + key, value);
    }

    /**
     * Retrieves a value from this storage section. The key will be automatically prefixed
     * with the section's path in the parent storage.
     * {@link Storage#get(String, Class)}
     *
     * @param <T>  The expected return type
     * @param key  The key within this section (without the section prefix)
     * @param type The expected class of the return value
     * @return The stored value if found and convertible, otherwise null
     */
    @Override
    public <T> @Nullable T get(@NotNull String key, @NotNull Class<T> type) {
        return parentStorage.get(parentKey + "." + key, type);
    }

    /**
     * Retrieves a value from this storage section with a fallback default value.
     * The key will be automatically prefixed with the section's path in the parent storage.
     * {@link Storage#get(String, Class, Object)}
     *
     * @param <T>          The expected return type
     * @param key          The key within this section (without the section prefix)
     * @param type         The expected class of the return value
     * @param defaultValue The value to return if the key doesn't exist
     * @return The stored value if found and convertible, otherwise the defaultValue
     */
    @Override
    public <T> T get(@NotNull String key, @NotNull Class<T> type, @Nullable T defaultValue) {
        return parentStorage.get(parentKey + "." + key, type, defaultValue);
    }
}