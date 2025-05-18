package org.leycm.storage.impl;

import org.jetbrains.annotations.NotNull;
import org.leycm.storage.Storage;

import java.util.UUID;

public class JavaStorage extends Storage {
    /**
     * Registers all type adapters for this storage instance.
     * Implementations should override this method to register their specific adapters.
     */
    @Override
    public void registerAdapter() {
        addAdapter(UUID.class, (this::setUuid));
        addAdapter(UUID.class, (this::getUuid));
    }

    private void setUuid(String key, @NotNull UUID uuid) {
        set(key, uuid.toString());
    }

    private void getUuid(String key, @NotNull UUID uuid) {
        set(key, uuid.toString());
    }

}
