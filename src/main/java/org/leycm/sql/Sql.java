package org.leycm.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton class for managing SQL database connections and operations.
 * Provides methods for executing queries, updates, and managing database tables.
 */
public final class Sql {
    private static Sql instance;
    private Connection connection;
    private final Logger logger;

    // Database configuration (could be moved to config file)
    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;

    /**
     * Constructs a new Sql instance with the specified database credentials.
     *
     * @param dbUrl the database connection URL
     * @param dbUser the database username
     * @param dbPassword the database password
     * @param logger the logger instance for logging messages
     */
    private Sql(String dbUrl, String dbUser, String dbPassword, Logger logger) {
        this.DB_URL = dbUrl;
        this.DB_USER = dbUser;
        this.DB_PASSWORD = dbPassword;
        this.logger = logger;
        initializeConnection();
    }

    /**
     * Gets the singleton instance of Sql, initializing it if necessary.
     *
     * @param dbUrl the database connection URL
     * @param dbUser the database username
     * @param dbPassword the database password
     * @param logger the logger instance for logging messages
     * @return the Sql singleton instance
     */
    public static synchronized Sql getInstance(String dbUrl, String dbUser, String dbPassword, Logger logger) {
        if (instance == null) {
            instance = new Sql(dbUrl, dbUser, dbPassword, logger);
        }
        return instance;
    }

    /**
     * Gets the existing singleton instance of Sql.
     *
     * @return the Sql singleton instance
     * @throws IllegalStateException if the instance hasn't been initialized
     */
    public static synchronized Sql getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SQLHandler not initialized. Call getInstance(dbUrl, dbUser, dbPassword, logger) first.");
        }
        return instance;
    }

    /**
     * Initializes the database connection.
     */
    private void initializeConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                logger.info("Database connection established");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to close database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the current connection is valid.
     *
     * @return true if the connection is valid and open, false otherwise
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Connection validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Creates a new table if it doesn't exist.
     *
     * @param tableName the name of the table to create
     * @param tableDefinition the column definitions for the table
     */
    public void createTable(String tableName, String tableDefinition) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableDefinition + ");";
        executeUpdate(sql);
    }

    /**
     * Executes an SQL update statement.
     *
     * @param sql the SQL update statement to execute
     * @return true if the update was successful, false otherwise
     */
    public boolean executeUpdate(String sql) {
        if (!isConnectionValid()) {
            initializeConnection();
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute update: " + sql + " - " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Executes an SQL query statement.
     *
     * @param sql the SQL query to execute
     * @return the ResultSet containing the query results, or null if the query failed
     */
    public @Nullable ResultSet executeQuery(String sql) {
        if (!isConnectionValid()) {
            initializeConnection();
        }

        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to execute query: " + sql + " - " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a prepared statement for parameterized queries.
     *
     * @param sql the SQL statement with parameters
     * @return the PreparedStatement object
     * @throws SQLException if a database access error occurs
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (!isConnectionValid()) {
            initializeConnection();
        }
        return connection.prepareStatement(sql);
    }

    /**
     * Checks if a table exists in the database.
     *
     * @param tableName the name of the table to check
     * @return true if the table exists, false otherwise
     */
    public boolean tableExists(String tableName) {
        if (!isConnectionValid()) {
            initializeConnection();
        }

        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, tableName, new String[] {"TABLE"});
            return tables.next();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check if table exists: " + tableName + " - " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets the number of rows in a table.
     *
     * @param tableName the name of the table
     * @return the row count, or -1 if the operation failed
     */
    public int getRowCount(String tableName) {
        if (!isConnectionValid()) {
            initializeConnection();
        }

        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get row count for table: " + tableName + " - " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Drops a table from the database if it exists.
     *
     * @param tableName the name of the table to drop
     * @return true if the table was dropped successfully, false otherwise
     */
    public boolean dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName + ";";
        return executeUpdate(sql);
    }

    /**
     * Inserts data into a table.
     *
     * @param tableName the name of the table
     * @param columns the columns to insert data into
     * @param values the values to insert
     * @return true if the insert was successful, false otherwise
     */
    public boolean insertData(String tableName, String columns, String values) {
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ");";
        return executeUpdate(sql);
    }

    /**
     * Updates data in a table.
     *
     * @param tableName the name of the table
     * @param setClause the SET clause for the update
     * @param whereClause the WHERE clause for the update (optional)
     * @return true if the update was successful, false otherwise
     */
    public boolean updateData(String tableName, String setClause, String whereClause) {
        String sql = "UPDATE " + tableName + " SET " + setClause;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        sql += ";";
        return executeUpdate(sql);
    }

    /**
     * Deletes data from a table.
     *
     * @param tableName the name of the table
     * @param whereClause the WHERE clause for the delete (optional)
     * @return true if the delete was successful, false otherwise
     */
    public boolean deleteData(String tableName, String whereClause) {
        String sql = "DELETE FROM " + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        sql += ";";
        return executeUpdate(sql);
    }
}