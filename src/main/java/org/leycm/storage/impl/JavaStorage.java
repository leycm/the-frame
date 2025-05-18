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

public class JavaStorage extends Storage {

    /**
     * Registers all type adapters for this storage instance.
     * Implementations should override this method to register their specific adapters.
     */
    @Override
    public void registerAdapter() {
        addAdapter(UUID.class, this::setUuid, this::getUuid);

        addAdapter(LocalDate.class, this::setLocalDate, this::getLocalDate);
        addAdapter(LocalTime.class, this::setLocalTime, this::getLocalTime);
        addAdapter(LocalDateTime.class, this::setLocalDateTime, this::getLocalDateTime);
        addAdapter(ZonedDateTime.class, this::setZonedDateTime, this::getZonedDateTime);
        addAdapter(Instant.class, this::setInstant, this::getInstant);
        addAdapter(Period.class, this::setPeriod, this::getPeriod);
        addAdapter(Duration.class, this::setDuration, this::getDuration);

        addAdapter(File.class, this::setFile, this::getFile);
        addAdapter(URL.class, this::setUrl, this::getUrl);
        addAdapter(URI.class, this::setUri, this::getUri);
        addAdapter(BigInteger.class, this::setBigInteger, this::getBigInteger);
        addAdapter(BigDecimal.class, this::setBigDecimal, this::getBigDecimal);
    }

    private void setUuid(String key, @NotNull UUID uuid) {
        set(key, uuid.toString());
    }

    private UUID getUuid(String key) {
        return get(key, UUID.class);
    }

    private void setLocalDate(String key, @NotNull LocalDate date) {
        set(key, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    private @Nullable LocalDate getLocalDate(String key) {
        String value = get(key, String.class);
        return value != null ? LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    private void setLocalTime(String key, @NotNull LocalTime time) {
        set(key, time.format(DateTimeFormatter.ISO_LOCAL_TIME));
    }

    private @Nullable LocalTime getLocalTime(String key) {
        String value = get(key, String.class);
        return value != null ? LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME) : null;
    }

    private void setLocalDateTime(String key, @NotNull LocalDateTime dateTime) {
        set(key, dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private @Nullable LocalDateTime getLocalDateTime(String key) {
        String value = get(key, String.class);
        return value != null ? LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    private void setZonedDateTime(String key, @NotNull ZonedDateTime zonedDateTime) {
        set(key, zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    private @Nullable ZonedDateTime getZonedDateTime(String key) {
        String value = get(key, String.class);
        return value != null ? ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME) : null;
    }

    private void setInstant(String key, @NotNull Instant instant) {
        set(key, instant.toString());
    }

    private @Nullable Instant getInstant(String key) {
        String value = get(key, String.class);
        return value != null ? Instant.parse(value) : null;
    }

    private void setPeriod(String key, @NotNull Period period) {
        set(key, period.toString());
    }

    private @Nullable Period getPeriod(String key) {
        String value = get(key, String.class);
        return value != null ? Period.parse(value) : null;
    }

    private void setDuration(String key, @NotNull Duration duration) {
        set(key, duration.toString());
    }

    private @Nullable Duration getDuration(String key) {
        String value = get(key, String.class);
        return value != null ? Duration.parse(value) : null;
    }

    private void setFile(String key, @NotNull File file) {
        set(key, file.getAbsolutePath());
    }

    private @Nullable File getFile(String key) {
        String value = get(key, String.class);
        return value != null ? new File(value) : null;
    }

    private void setUrl(String key, @NotNull URL url) {
        set(key, url.toString());
    }

    private @Nullable URL getUrl(String key) throws MalformedURLException {
        String value = get(key, String.class);
        return value != null ? new URL(value) : null;
    }

    private void setUri(String key, @NotNull URI uri) {
        set(key, uri.toString());
    }

    private @Nullable URI getUri(String key) {
        String value = get(key, String.class);
        return value != null ? URI.create(value) : null;
    }

    private void setBigInteger(String key, @NotNull BigInteger bigInteger) {
        set(key, bigInteger.toString());
    }

    private @Nullable BigInteger getBigInteger(String key) {
        String value = get(key, String.class);
        return value != null ? new BigInteger(value) : null;
    }

    private void setBigDecimal(String key, @NotNull BigDecimal bigDecimal) {
        set(key, bigDecimal.toString());
    }

    private @Nullable BigDecimal getBigDecimal(String key) {
        String value = get(key, String.class);
        return value != null ? new BigDecimal(value) : null;
    }

}
