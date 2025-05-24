package org.leycm.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Sql {
    private static Sql instance;
    private Connection connection;
    private final Logger logger;

    // Database configuration (could be moved to config file)
    private final String DB_URL;
    private final String DB_USER;
    private final String DB_PASSWORD;

    private Sql(String dbUrl, String dbUser, String dbPassword, Logger logger) {
        this.DB_URL = dbUrl;
        this.DB_USER = dbUser;
        this.DB_PASSWORD = dbPassword;
        this.logger = logger;
        initializeConnection();
    }

    public static synchronized Sql getInstance(String dbUrl, String dbUser, String dbPassword, Logger logger) {
        if (instance == null) {
            instance = new Sql(dbUrl, dbUser, dbPassword, logger);
        }
        return instance;
    }

    public static synchronized Sql getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SQLHandler not initialized. Call getInstance(dbUrl, dbUser, dbPassword, logger) first.");
        }
        return instance;
    }

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

    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Connection validation failed: " + e.getMessage(), e);
            return false;
        }
    }

    public void createTable(String tableName, String tableDefinition) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableDefinition + ");";
        executeUpdate(sql);
    }

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

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (!isConnectionValid()) {
            initializeConnection();
        }
        return connection.prepareStatement(sql);
    }

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

    public boolean dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName + ";";
        return executeUpdate(sql);
    }

    public boolean insertData(String tableName, String columns, String values) {
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ");";
        return executeUpdate(sql);
    }

    public boolean updateData(String tableName, String setClause, String whereClause) {
        String sql = "UPDATE " + tableName + " SET " + setClause;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        sql += ";";
        return executeUpdate(sql);
    }

    public boolean deleteData(String tableName, String whereClause) {
        String sql = "DELETE FROM " + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        sql += ";";
        return executeUpdate(sql);
    }

}
