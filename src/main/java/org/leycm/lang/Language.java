package org.leycm.lang;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a language with its metadata and file path.
 * Manages caching of language instances for efficient retrieval.
 */
public record Language(
        String id,
        String path,
        String name
) {
    private static final Map<String, Language> langCache = new HashMap<>();

    /**
     * Retrieves a language instance from cache or creates a new one.
     * @param filePath Path to the language file (relative or absolute)
     * @return Language instance or null if file doesn't exist
     */
    public static @Nullable Language of(String filePath) {
        if (langCache.containsKey(filePath)) {
            return langCache.get(filePath);
        }

        String fullPath;
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            fullPath = TranslationHandler.getLangDir() + File.separator + filePath;
        } else {
            fullPath = filePath;
        }

        File langFile = new File(fullPath);
        if (!langFile.exists() || !langFile.isFile()) {
            return null;
        }

        String langId = langFile.getName().replace(".json", "");
        String langName = langId.split("-")[0];

        Language language = new Language(langId, fullPath, langName);
        langCache.put(filePath, language);

        return language;
    }

    /**
     * Clears all cached language instances.
     */
    public static void clearCache() {
        langCache.clear();
    }
}
