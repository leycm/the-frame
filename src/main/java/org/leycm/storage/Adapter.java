package org.leycm.storage;

public class Adapter {
    @FunctionalInterface
    public interface StorageSetter<T> {
        void set(String key, T value);
    }

    @FunctionalInterface
    public interface StorageGetter<T> {
        T get(String key);
    }
}
