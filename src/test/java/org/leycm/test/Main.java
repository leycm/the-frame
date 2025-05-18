package org.leycm.test;

import org.leycm.storage.impl.JavaStorage;
import org.leycm.storage.Storage;
import org.leycm.storage.StorageRegistry;

import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {

        Logger logger = Logger.getLogger("main");

        StorageRegistry.setup(null, logger);

        Storage storage = Storage.of("test/path/file", Storage.Type.JSON, JavaStorage.class);

        String s = storage.get("test.name", String.class);
        System.out.println(s);

    }
}