package org.leycm.storage;

import java.net.MalformedURLException;

/**
 * Defines the contract for adapters that handle the serialization and
 * deserialization of specific data types to and from the underlying
 * storage format (e.g., JSON, YAML, TOML).
 */
public interface StorageAdapter {

    /**
     * A functional interface representing a setter operation for a specific type.
     * Implementations of this interface define how to store a value of type {@code T}
     * under a given key in the storage.
     *
     * @param <T> The type of the value to be set.
     */
    @FunctionalInterface
    interface Setter<T> {
        /**
         * Sets the given {@code value} for the specified {@code key} in the storage.
         *
         * @param key   The key under which the value should be stored.
         * @param value The value to be stored.
         */
        void set(String key, T value);
    }

    /**
     * A functional interface representing a getter operation for a specific type.
     * Implementations of this interface define how to retrieve a value of type {@code T}
     * from the storage based on a given key.
     *
     * @param <T> The type of the value to be retrieved.
     */
    @FunctionalInterface
    interface Getter<T> {
        /**
         * Retrieves the value of type {@code T} associated with the specified {@code key}
         * from the storage.
         *
         * @param key The key of the value to retrieve.
         * @return The value associated with the key, cast to type {@code T}.
         * @throws MalformedURLException If the retrieved value cannot be properly converted
         * to the expected type, or if the underlying data
         * represents a malformed URL when a URL is expected.
         */
        T get(String key) throws MalformedURLException;
    }
}