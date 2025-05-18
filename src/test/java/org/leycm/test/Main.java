package org.leycm.test;

import org.leycm.storage.impl.JavaStorage;
import org.leycm.storage.StorageBase;
import org.leycm.storage.StorageRegistry;

import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {

        Logger logger = Logger.getLogger("main");

        StorageRegistry.setup(null, logger);

        StorageBase storage = StorageBase.of("test/path/file", StorageBase.Type.JSON, JavaStorage.class);

        storage.set("test.name.hans", "Hans");
        storage.set("test.name.paul", "Paul");
        storage.set("test.id.paul.", "paul-is-dumm");
        storage.set("noch.ein.test", "Hans 2.0 Update");
        storage.save();

        System.out.println("key=\"\" deep=false : " + storage.getKeys(false));
        System.out.println("key=\"\" deep=true : " + storage.getKeys(true));
        System.out.println("key=\"test\" deep=false : " + storage.getKeys("test", false));
        System.out.println("key=\"test\" deep=true : " + storage.getKeys("test", true));
    }
}