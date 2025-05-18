package org.leycm.lang;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a translated text with placeholder support.
 * <p>
 * Placeholders must be in the format {@code %placeholder%} and will be replaced
 * by the provided values. Other formats like {@code <placeholder>} will not be processed.
 *
 * @param key The translation key used to identify this text
 * @param value The actual translated text with potential placeholders
 */
public record Translation(
        String path,
        String key,
        String value
) {

    /**
     * Returns the raw translated value without any placeholder processing.
     * @return The unmodified translation value
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Replaces placeholders in the format {@code %placeholder%} with values from the provided map.
     * <p>
     * Example:
     * <pre>{@code
     * Translation t;
     * String formatted = t.format(Map.of("command", "/msg <player>"));
     * // Result: "Use /msg <player>"
     * }</pre>
     *
     * @param replacements Map of placeholder names (without % symbols) to their replacement values
     * @return The formatted string with replaced placeholders
     */
    public String format(Map<String, String> replacements) {

        if (replacements == null || replacements.isEmpty()) {
            return value;
        }

        String result = value;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = Pattern.quote(entry.getKey());
            String replacement = Matcher.quoteReplacement(entry.getValue());
            result = result.replaceAll("%" + placeholder + "%", replacement);
        }
        return result;
    }

    /**
     * Replaces placeholders in the format {@code %placeholder%} with values from the provided map and wraps the replacement with markers.
     * <p>
     * Example:
     * <pre>{@code
     * Translation t; // Use %command%
     * String formatted = t.format(Map.of("command", "/msg <player>"), "&");
     * // Result: "Use &/msg <player>&"
     * }</pre>
     *
     * You can also use % it won't be removed.
     * @param replacements Map of placeholder names (without % symbols) to their replacement values
     * @param marker The delimiter to wrap around the replacement
     * @return The formatted string with replaced placeholders
     */
    public String format(@NotNull Map<String, String> replacements,
                         @NotNull String marker) {

        if (replacements.isEmpty()) {
            return value;
        }

        String result = value;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = Pattern.quote(entry.getKey());
            String replacement = Matcher.quoteReplacement(entry.getValue());
            result = result.replaceAll("%" + placeholder + "%", marker + replacement + marker);
        }
        return result;
    }

    /**
     * Replaces a single placeholder with a value.
     * <p>
     * Example:
     * <pre>{@code
     * Translation t; // Hello %name%
     * String formatted = t.format("name", "Hans");
     * // Result: "Hello Hans!"
     * }</pre>
     *
     * @param placeholder The placeholder name without % symbols
     * @param value The replacement value
     * @return The formatted string
     */
    public @NotNull String format(String placeholder,
                                  String value) {

        return this.value.replaceAll(
                "%" + Pattern.quote(placeholder) + "%",
                Matcher.quoteReplacement(value)
        );
    }

    /**
     * Replaces a placeholder and wraps the replacement with markers.
     * <p>
     * Example:
     * <pre>{@code
     * Translation t; // "Use %command%"
     * String formatted = t.format("command", "/gamemode survival", "&");
     * // Result: "Use &/gamemode survival&"
     * }</pre>
     *
     * You can also use % it won't be removed.
     * @param placeholder The placeholder name without % symbols
     * @param replacement The value to insert (will be wrapped with markers)
     * @param marker The delimiter to wrap around the replacement
     * @return The formatted string with marked replacement
     */
    public @NotNull String format(@NotNull String placeholder,
                                  @NotNull String replacement,
                                  @NotNull String marker) {

        return this.value.replaceAll(
                "%" + Pattern.quote(placeholder) + "%",
                Matcher.quoteReplacement(marker + replacement + marker)
        );
    }
}