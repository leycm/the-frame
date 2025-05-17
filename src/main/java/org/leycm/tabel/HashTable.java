package org.leycm.tabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of the Table interface using a HashMap for faster lookups.
 * This extends the functionality of BaseTable with optimized retrieval operations.
 */
public class HashTable implements Table {

    private final Schema schema;
    private final Map<Integer, Entry> entriesMap = new HashMap<>();
    private int nextIndex = 0;

    /**
     * Creates a new HashTable with the specified column types.
     * @param columnTypes the classes representing the column types
     */
    public HashTable(Class<?>... columnTypes) {
        this.schema = new HashSchema(columnTypes);
    }

    /**
     * Private constructor used for creating filtered tables.
     * @param schema the schema to use
     * @param entries the entries to include
     */
    private HashTable(Schema schema, Map<Integer, Entry> entries) {
        this.schema = schema;
        this.entriesMap.putAll(entries);
        this.nextIndex = entries.isEmpty() ? 0 :
                entries.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return schema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry getEntry(int index) {
        Entry entry = entriesMap.get(index);
        if (entry == null) {
            throw new IndexOutOfBoundsException("Entry with index " + index + " not found");
        }
        return entry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return entriesMap.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> getAllEntries() {
        return new ArrayList<>(entriesMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addEntry(Object... values) {
        validateValues(values);
        HashEntry entry = new HashEntry(nextIndex, values);
        entriesMap.put(nextIndex, entry);
        return nextIndex++;
    }

    private void validateValues(@NotNull Object @NotNull [] values) {
        if (values.length != schema.getColumnCount()) {
            throw new IllegalArgumentException("Expected " + schema.getColumnCount()
                    + " values, but got " + values.length);
        }

        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            Class<?> expectedType = schema.getColumnType(i);

            if (value != null && !expectedType.isInstance(value)) {
                throw new IllegalArgumentException("Value at column " + i + " is not of expected type "
                        + expectedType.getName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeEntry(int index) {
        return entriesMap.remove(index) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table findMatches(int column, Object searchValue) {
        if (column < 0 || column >= schema.getColumnCount()) {
            throw new IllegalArgumentException("Invalid column: " + column);
        }

        Map<Integer, Entry> matchingEntries = entriesMap.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue().getValue(column), searchValue))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new HashTable(schema, matchingEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Table findMatches(Predicate<Entry> predicate) {
        Map<Integer, Entry> matchingEntries = entriesMap.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new HashTable(schema, matchingEntries);
    }

    /**
     * Implementation of the Schema interface optimized for HashTable.
     */
    private static class HashSchema implements Schema {
        private final Class<?>[] columnTypes;

        HashSchema(Class<?> @NotNull [] columnTypes) {
            this.columnTypes = columnTypes.clone();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getColumnCount() {
            return columnTypes.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?> getColumnType(int column) {
            if (column < 0 || column >= columnTypes.length) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
            return columnTypes[column];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?>[] getAllColumnTypes() {
            return columnTypes.clone();
        }
    }

    /**
     * Implementation of the Entry interface optimized for HashTable.
     */
    private static class HashEntry implements Entry {
        private final int index;
        private final Object[] values;

        HashEntry(int index, Object @NotNull [] values) {
            this.index = index;
            this.values = values.clone();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getIndex() {
            return index;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getColumnCount() {
            return values.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getValue(int column) {
            if (column < 0 || column >= values.length) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }
            return (T) values[column];
        }

        /**
         * {@inheritDoc}
         */
        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getValue(int column, @NotNull Class<T> type) {
            if (column < 0 || column >= values.length) {
                throw new IllegalArgumentException("Invalid column: " + column);
            }

            Object value = values[column];

            if (value == null) {
                return null;
            }

            if (!type.isInstance(value)) {
                throw new ClassCastException("Cannot cast " + value.getClass().getName()
                        + " to " + type.getName());
            }

            return (T) value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getAllValues() {
            return values.clone();
        }
    }
}