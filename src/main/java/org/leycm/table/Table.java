package org.leycm.table;

import java.util.List;
import java.util.function.Predicate;

/**
 * A flexible table interface that supports a variable number of columns.
 * Each row has an integer index and a list of column values.
 * The number and types of columns are determined at runtime.
 */
public interface Table {

    /**
     * Represents a single row entry in the table.
     */
    interface Entry {
        /**
         * Returns the index of this entry.
         * @return the integer index
         */
        int getIndex();

        /**
         * Returns the number of columns in this entry.
         * @return the column count
         */
        int getColumnCount();

        /**
         * Returns the value of the specified column.
         * @param column the column index (0-based)
         * @return the value of the column
         * @throws IllegalArgumentException if the column is invalid
         */
        <T> T getValue(int column);

        /**
         * Returns the value of the specified column as the given type.
         * @param column the column index (0-based)
         * @param type the class representing the expected type
         * @param <T> the expected type
         * @return the value of the column cast to the expected type
         * @throws IllegalArgumentException if the column is invalid
         * @throws ClassCastException if the value cannot be cast to the expected type
         */
        <T> T getValue(int column, Class<T> type);

        /**
         * Returns all column values as an array.
         * @return array of all column values
         */
        Object[] getAllValues();
    }

    /**
     * Creates a schema for the table.
     * The schema defines the column types.
     */
    interface Schema {
        /**
         * Returns the number of columns in this schema.
         * @return the column count
         */
        int getColumnCount();

        /**
         * Returns the class of the specified column.
         * @param column the column index (0-based)
         * @return the class of the column
         * @throws IllegalArgumentException if the column is invalid
         */
        Class<?> getColumnType(int column);

        /**
         * Returns all column types as an array.
         * @return array of all column types
         */
        Class<?>[] getAllColumnTypes();
    }

    /**
     * Returns the schema of this table.
     * @return the schema
     */
    Schema getSchema();

    /**
     * Returns the entry at the specified index.
     * @param index the index of the entry to retrieve
     * @return the entry at the specified index if found
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    Entry getEntry(int index);

    /**
     * Returns the number of rows in the table.
     * @return the row count
     */
    int size();

    /**
     * Returns all entries in the table.
     * @return a list of all entries
     */
    List<Entry> getAllEntries();

    /**
     * Adds a new entry to the table.
     * @param values the values for each column of the new entry
     * @return the index of the newly added entry
     * @throws IllegalArgumentException if the number of values doesn't match the schema or if types don't match
     */
    int addEntry(Object... values);

    /**
     * Removes the entry at the specified index.
     * @param index the index of the entry to remove
     * @return true if an entry was removed, false otherwise
     */
    boolean removeEntry(int index);

    /**
     * Finds all entries where the specified column matches the search value.
     * @param column the column to search (0-based)
     * @param searchValue the value to match
     * @return a new table containing only the matching entries
     */
    Table findMatches(int column, Object searchValue);

    /**
     * Finds all entries that satisfy the given predicate.
     * @param predicate the predicate to test entries against
     * @return a new table containing only the matching entries
     */
    Table findMatches(Predicate<Entry> predicate);

}