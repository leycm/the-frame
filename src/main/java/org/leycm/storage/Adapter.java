package org.leycm.storage;

import java.net.MalformedURLException;

public class Adapter {
    @FunctionalInterface
    public interface StorageSetter<T> {
        void set(String key, T value);
    }

    @FunctionalInterface
    public interface StorageGetter<T> {
        T get(String key) throws MalformedURLException;
    }
}
