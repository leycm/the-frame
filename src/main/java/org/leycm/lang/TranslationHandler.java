package org.leycm.lang;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.*;
import org.leycm.storage.StorageRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A handler class for managing and retrieving translations from language files.
 * <p>
 * This class provides functionality to load language files in JSON format,
 * cache them for performance, and retrieve translations for specific keys.
 * It supports fallback to a default language (en-en.json) if the requested language is not available.
 */
public final class TranslationHandler {
    private static final String DEFAULT_LANG_DIR = ".lang";

    private static final Map<String, JsonObject> langCache = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Getter private static Logger logger;
    @Getter private static String langDir;
    @Getter private static boolean isSetup;

    private TranslationHandler() {}

    static {
        if (!isSetup) {
            throw new IllegalStateException("You must call setup() before using this class.");
        }
    }

    /**
     * Initializes the translation handler with the specified language directory path.
     * If the provided path is null, the default directory (.lang) will be used.
     *
     * @param path The path to the directory containing language files
     */
    public static void setup(String path,
                             Logger logger) {

        if (isSetup) throw new IllegalStateException("Already set up!");

        TranslationHandler.langDir = path != null ? path : DEFAULT_LANG_DIR;
        TranslationHandler.logger = logger != null ? logger : Logger.getLogger("the-frame");
        TranslationHandler.isSetup = true;
    }

    /**
     * Retrieves a translation for the given key using the default language (en-en).
     * The translation is searched in the "messages" path of the language file.
     *
     * @param key The key of the translation to retrieve
     * @return A Translation object containing the key and its translated value
     */
    public static @NotNull Translation getTranslationFor(String key) {
        return getTranslationFromJson("messages", key, Language.of("en-en.json"));
    }

    /**
     * Retrieves a translation for the given key using the specified language.
     * The translation is searched in the "messages" path of the language file.
     *
     * @param key The key of the translation to retrieve
     * @param lang The language to use for the translation
     * @return A Translation object containing the key and its translated value
     */
    public static @NotNull Translation getTranslationFor(String key, Language lang) {
        return getTranslationFromJson("messages", key, lang);
    }

    /**
     * Retrieves a translation for the given key using the default language (en-en).
     * The translation is searched in the specified path of the language file.
     *
     * @param path The path within the JSON structure where the translation is located
     * @param key The key of the translation to retrieve
     * @return A Translation object containing the key and its translated value
     */
    public static @NotNull Translation getTranslationFor(String path, String key) {
        return getTranslationFromJson(path, key, Language.of("en-en.json"));
    }

    /**
     * Retrieves a translation for the given key using the specified language.
     * The translation is searched in the specified path of the language file.
     *
     * @param path The path within the JSON structure where the translation is located
     * @param key The key of the translation to retrieve
     * @param lang The language to use for the translation (null will return the key as the translation)
     * @return A Translation object containing the key and its translated value
     */
    @Contract("_, _, null -> new")
    public static @NotNull Translation getTranslationFor(String path, String key, Language lang) {
        return getTranslationFromJson(path, key, lang);
    }

    /**
     * Internal method to retrieve a translation from a JSON language file.
     *
     * @param path The path within the JSON structure where the translation is located
     * @param key The key of the translation to retrieve
     * @param lang The language to use for the translation
     * @return A Translation object containing the key and its translated value
     *         (returns the key as the translation if the path/key is not found)
     */
    private static @NotNull Translation getTranslationFromJson(String path, String key, Language lang) {
        if (lang == null) return new Translation(path , key, key);

        try {
            JsonObject rootObject = loadLanguageFile(lang.id());
            if (rootObject == null) return new Translation(path , key, key);

            JsonElement currentElement = rootObject;
            if (path != null && !path.isEmpty()) {
                for (String part : path.split("\\.")) {
                    if (!currentElement.isJsonObject()) return new Translation(path , key, key);
                    currentElement = currentElement.getAsJsonObject().get(part);
                    if (currentElement == null || currentElement.isJsonNull()) return new Translation(path , key, key);
                }
            }

            if (!currentElement.isJsonObject()) return new Translation(path , key, key);
            JsonElement valueElement = currentElement.getAsJsonObject().get(key);
            if (valueElement == null || valueElement.isJsonNull()) return new Translation(path , key, key);

            if (valueElement.isJsonPrimitive() && valueElement.getAsJsonPrimitive().isString()) {
                return new Translation(path , key, valueElement.getAsString());
            }

        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

        return new Translation(path , key, key);
    }

    /**
     * Loads a language file from disk or returns a cached version if available.
     * If the requested language file doesn't exist, falls back to en-en.json.
     *
     * @param langCode The language code of the file to load (e.g., "en-en")
     * @return The parsed JsonObject containing all translations for the language
     * @throws IOException If there's an error reading the language file
     */
    public static @Nullable JsonObject loadLanguageFile(@NotNull String langCode) throws IOException {
        if (langCache.containsKey(langCode)) return langCache.get(langCode);

        String fullPath = Paths.get(langDir, langCode + ".json").toString();
        File langFile = new File(fullPath);

        if (!langFile.exists()) {
            String fallbackPath = Paths.get(langDir, "en-en.json").toString();
            File fallbackFile = new File(fallbackPath);
            if (!fallbackFile.exists()) return null;

            String content = new String(Files.readAllBytes(Paths.get(fallbackPath)));
            JsonObject rootObject = gson.fromJson(content, JsonObject.class);
            langCache.put(langCode, rootObject);
            return rootObject;
        }

        String content = new String(Files.readAllBytes(Paths.get(fullPath)));
        JsonObject rootObject = gson.fromJson(content, JsonObject.class);
        langCache.put(langCode, rootObject);
        return rootObject;
    }

    /**
     * Clears the cache of loaded language files.
     * This forces the handler to reload language files from disk on next access.
     */
    public static void clearCache() {
        langCache.clear();
    }
}