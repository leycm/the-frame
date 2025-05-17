package org.leycm.tabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of the FlexibleTabel interface.
 */
public class BaseTable implements Table {

    private final BaseSchema schema;
    private final List<BaseEntry> entries = new ArrayList<>();
    private int nextIndex = 0;

    /**
     * Creates a new HashTable with the specified column types.
     * @param columnTypes the classes representing the column types
     */
    public BaseTable(Class<?>... columnTypes) {
        this.schema = new BaseSchema(columnTypes);
    }

    /**
     * Private constructor used for creating filtered tables.
     * @param schema the schema to use
     * @param entries the entries to include
     */
    private BaseTable(BaseSchema schema, List<BaseEntry> entries) {
        this.schema = schema;
        this.entries.addAll(entries);
        this.nextIndex = entries.isEmpty() ? 0 :
                entries.stream().mapToInt(Entry::getIndex).max().orElse(-1) + 1;
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
        return entries.stream()
                .filter(entry -> entry.getIndex() == index)
                .findFirst()
                .orElseThrow(() -> new IndexOutOfBoundsException("Entry with index " + index + " not found"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return entries.size();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry> getAllEntries() {
        return new ArrayList<>(entries);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int addEntry(Object... values) {
        validateValues(values);
        BaseEntry entry = new BaseEntry(nextIndex, values);
        entries.add(entry);
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

            if (!expectedType.isInstance(value)) {
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
        return entries.removeIf(entry -> entry.getIndex() == index);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Table findMatches(int column, Object searchValue) {
        if (column < 0 || column >= schema.getColumnCount()) {
            throw new IllegalArgumentException("Invalid column: " + column);
        }

        List<BaseEntry> matchingEntries = entries.stream()
                .filter(entry -> Objects.equals(entry.getValue(column), searchValue))
                .collect(Collectors.toList());

        return new BaseTable(schema, matchingEntries);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Table findMatches(Predicate<Entry> predicate) {
        List<BaseEntry> matchingEntries = entries.stream()
                .filter(predicate)
                .collect(Collectors.toList());

        return new BaseTable(schema, matchingEntries);
    }

    /**
     * Implementation of the Schema interface.
     */
    private static class BaseSchema implements Schema {
        private final Class<?>[] columnTypes;

        BaseSchema(Class<?>[] columnTypes) {
            this.columnTypes = Arrays.copyOf(columnTypes, columnTypes.length);
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
        @Contract(value = " -> new", pure = true)
        @Override
        public Class<?> @NotNull [] getAllColumnTypes() {
            return Arrays.copyOf(columnTypes, columnTypes.length);
        }
    }

    /**
     * Implementation of the Entry interface.
     */
    private static class BaseEntry implements Entry {
        private final int index;
        private final Object[] values;

        BaseEntry(int index, Object[] values) {
            this.index = index;
            this.values = Arrays.copyOf(values, values.length);
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
        @Contract(value = " -> new", pure = true)
        @Override
        public Object @NotNull [] getAllValues() {
            return Arrays.copyOf(values, values.length);
        }

    }
}
