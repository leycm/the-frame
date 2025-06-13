package org.leycm.storage.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.leycm.storage.Storage;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * A concrete implementation of Storage that provides adapters for common Java types.
 * <p>
 * This class extends the abstract Storage class and implements type adapters for:
 * <ul>
 *   <li>UUID</li>
 *   <li>Java Time API classes (LocalDate, LocalTime, LocalDateTime, etc.)</li>
 *   <li>File system paths (File)</li>
 *   <li>Network resources (URL, URI)</li>
 *   <li>Big number types (BigInteger, BigDecimal)</li>
 * </ul>
 * All values are stored as strings using standard ISO formats where applicable.
 */
public class JavaStorage extends Storage {

    /**
     * Registers all type adapters for common Java types.
     * <p>
     * This implementation registers adapters for:
     * <ul>
     *   <li>UUID - stored as string representation</li>
     *   <li>Java Time API classes - stored in ISO formats</li>
     *   <li>File system paths - stored as absolute paths</li>
     *   <li>Network resources - stored as string representations</li>
     *   <li>Big number types - stored as string representations</li>
     * </ul>
     */
    @Override
    public void registerAdapter() {
        // Register UUID adapter
        addAdapter(UUID.class, this::setUuid, this::getUuid);

        // Register Java Time API adapters
        addAdapter(LocalDate.class, this::setLocalDate, this::getLocalDate);
        addAdapter(LocalTime.class, this::setLocalTime, this::getLocalTime);
        addAdapter(LocalDateTime.class, this::setLocalDateTime, this::getLocalDateTime);
        addAdapter(ZonedDateTime.class, this::setZonedDateTime, this::getZonedDateTime);
        addAdapter(Instant.class, this::setInstant, this::getInstant);
        addAdapter(Period.class, this::setPeriod, this::getPeriod);
        addAdapter(Duration.class, this::setDuration, this::getDuration);

        // Register I/O and network adapters
        addAdapter(File.class, this::setFile, this::getFile);
        addAdapter(URL.class, this::setUrl, this::getUrl);
        addAdapter(URI.class, this::setUri, this::getUri);

        // Register numeric adapters
        addAdapter(BigInteger.class, this::setBigInteger, this::getBigInteger);
        addAdapter(BigDecimal.class, this::setBigDecimal, this::getBigDecimal);
    }

    /**
     * Stores a UUID as its string representation.
     *
     * @param key  The storage key/path
     * @param uuid The UUID to store (must not be null)
     */
    private void setUuid(String key, @NotNull UUID uuid) {
        set(key, uuid.toString());
    }

    /**
     * Retrieves a UUID from its string representation.
     *
     * @param key The storage key/path
     * @return The UUID if found and valid, otherwise null
     */
    private UUID getUuid(String key) {
        return get(key, UUID.class);
    }

    /**
     * Stores a LocalDate in ISO-8601 format (yyyy-MM-dd).
     *
     * @param key  The storage key/path
     * @param date The LocalDate to store (must not be null)
     */
    private void setLocalDate(String key, @NotNull LocalDate date) {
        set(key, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * Retrieves a LocalDate from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The LocalDate if found and valid, otherwise null
     */
    private @Nullable LocalDate getLocalDate(String key) {
        String value = get(key, String.class);
        return value != null ? LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    /**
     * Stores a LocalTime in ISO-8601 format (HH:mm:ss.SSS).
     *
     * @param key  The storage key/path
     * @param time The LocalTime to store (must not be null)
     */
    private void setLocalTime(String key, @NotNull LocalTime time) {
        set(key, time.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * Retrieves a LocalTime from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The LocalTime if found and valid, otherwise null
     */
    private @Nullable LocalTime getLocalTime(String key) {
        String value = get(key, String.class);
        return value != null ? LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME) : null;
    }

    /**
     * Stores a LocalDateTime in ISO-8601 format (yyyy-MM-dd'T'HH:mm:ss.SSS).
     *
     * @param key      The storage key/path
     * @param dateTime The LocalDateTime to store (must not be null)
     */
    private void setLocalDateTime(String key, @NotNull LocalDateTime dateTime) {
        set(key, dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Retrieves a LocalDateTime from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The LocalDateTime if found and valid, otherwise null
     */
    private @Nullable LocalDateTime getLocalDateTime(String key) {
        String value = get(key, String.class);
        return value != null ? LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    /**
     * Stores a ZonedDateTime in ISO-8601 format with time zone.
     *
     * @param key           The storage key/path
     * @param zonedDateTime The ZonedDateTime to store (must not be null)
     */
    private void setZonedDateTime(String key, @NotNull ZonedDateTime zonedDateTime) {
        set(key, zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    /**
     * Retrieves a ZonedDateTime from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The ZonedDateTime if found and valid, otherwise null
     */
    private @Nullable ZonedDateTime getZonedDateTime(String key) {
        String value = get(key, String.class);
        return value != null ? ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME) : null;
    }

    /**
     * Stores an Instant as an ISO-8601 UTC timestamp.
     *
     * @param key     The storage key/path
     * @param instant The Instant to store (must not be null)
     */
    private void setInstant(String key, @NotNull Instant instant) {
        set(key, instant.toString());
    }

    /**
     * Retrieves an Instant from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The Instant if found and valid, otherwise null
     */
    private @Nullable Instant getInstant(String key) {
        String value = get(key, String.class);
        return value != null ? Instant.parse(value) : null;
    }

    /**
     * Stores a Period in ISO-8601 format (PnYnMnD).
     *
     * @param key    The storage key/path
     * @param period The Period to store (must not be null)
     */
    private void setPeriod(String key, @NotNull Period period) {
        set(key, period.toString());
    }

    /**
     * Retrieves a Period from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The Period if found and valid, otherwise null
     */
    private @Nullable Period getPeriod(String key) {
        String value = get(key, String.class);
        return value != null ? Period.parse(value) : null;
    }

    /**
     * Stores a Duration in ISO-8601 format (PnDTnHnMn.nS).
     *
     * @param key      The storage key/path
     * @param duration The Duration to store (must not be null)
     */
    private void setDuration(String key, @NotNull Duration duration) {
        set(key, duration.toString());
    }

    /**
     * Retrieves a Duration from its ISO-8601 string representation.
     *
     * @param key The storage key/path
     * @return The Duration if found and valid, otherwise null
     */
    private @Nullable Duration getDuration(String key) {
        String value = get(key, String.class);
        return value != null ? Duration.parse(value) : null;
    }

    /**
     * Stores a File as its absolute path.
     *
     * @param key  The storage key/path
     * @param file The File to store (must not be null)
     */
    private void setFile(String key, @NotNull File file) {
        set(key, file.getAbsolutePath());
    }

    /**
     * Retrieves a File from its stored absolute path.
     *
     * @param key The storage key/path
     * @return The File if path exists, otherwise null
     */
    private @Nullable File getFile(String key) {
        String value = get(key, String.class);
        return value != null ? new File(value) : null;
    }

    /**
     * Stores a URL as its string representation.
     *
     * @param key The storage key/path
     * @param url The URL to store (must not be null)
     */
    private void setUrl(String key, @NotNull URL url) {
        set(key, url.toString());
    }

    /**
     * Retrieves a URL from its string representation.
     *
     * @param key The storage key/path
     * @return The URL if valid, otherwise null
     * @throws MalformedURLException if the stored string is not a valid URL
     */
    private @Nullable URL getUrl(String key) throws MalformedURLException {
        String value = get(key, String.class);
        return value != null ? new URL(value) : null;
    }

    /**
     * Stores a URI as its string representation.
     *
     * @param key The storage key/path
     * @param uri The URI to store (must not be null)
     */
    private void setUri(String key, @NotNull URI uri) {
        set(key, uri.toString());
    }

    /**
     * Retrieves a URI from its string representation.
     *
     * @param key The storage key/path
     * @return The URI if valid, otherwise null
     */
    private @Nullable URI getUri(String key) {
        String value = get(key, String.class);
        return value != null ? URI.create(value) : null;
    }

    /**
     * Stores a BigInteger as its string representation.
     *
     * @param key        The storage key/path
     * @param bigInteger The BigInteger to store (must not be null)
     */
    private void setBigInteger(String key, @NotNull BigInteger bigInteger) {
        set(key, bigInteger.toString());
    }

    /**
     * Retrieves a BigInteger from its string representation.
     *
     * @param key The storage key/path
     * @return The BigInteger if valid, otherwise null
     */
    private @Nullable BigInteger getBigInteger(String key) {
        String value = get(key, String.class);
        return value != null ? new BigInteger(value) : null;
    }

    /**
     * Stores a BigDecimal as its string representation.
     *
     * @param key        The storage key/path
     * @param bigDecimal The BigDecimal to store (must not be null)
     */
    private void setBigDecimal(String key, @NotNull BigDecimal bigDecimal) {
        set(key, bigDecimal.toString());
    }

    /**
     * Retrieves a BigDecimal from its string representation.
     *
     * @param key The storage key/path
     * @return The BigDecimal if valid, otherwise null
     */
    private @Nullable BigDecimal getBigDecimal(String key) {
        String value = get(key, String.class);
        return value != null ? new BigDecimal(value) : null;
    }
}