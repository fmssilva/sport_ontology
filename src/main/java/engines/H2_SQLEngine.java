package engines;

import config.AppConfig;
import database.CreateH2Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.nio.file.Files;

/**
 * Manages H2 database lifecycle and SQL operations
 * Uses centralized configuration from AppConfig for cross-platform compatibility
 */
public class H2_SQLEngine {
    private Connection connection;
    private String dbPath;
    private String dbUrl;
    private boolean isStarted = false;

    public H2_SQLEngine() {
        // Use centralized configuration from AppConfig
        this.dbPath = AppConfig.DATABASE_FILE.toString();
        this.dbUrl = AppConfig.getDatabaseUrlWithServer(9092);

        try {
            // Ensure database directory exists using cross-platform paths
            if (!Files.exists(AppConfig.DATABASE_DIR)) {
                Files.createDirectories(AppConfig.DATABASE_DIR);
                System.out.println("Created database directory: " + AppConfig.DATABASE_DIR);
            }
        } catch (Exception e) {
            System.err.println("Could not create database directory: " + e.getMessage());
        }
    }

    /**
     * Start the database engine
     */
    public void start() throws SQLException {
        if (isStarted) {
            System.out.println("Database engine already started");
            return;
        }

        try {
            System.out.println("Starting H2 Database Engine");
            CreateH2Database.main(new String[]{dbPath});
            connection = DriverManager.getConnection(dbUrl, "sa", "");

            if (testConnection()) {
                isStarted = true;
                System.out.println("Database Engine started successfully");
                System.out.println("Location: " + dbPath);
                System.out.println("URL: " + dbUrl);
            } else {
                throw new SQLException("Database connection test failed");
            }

        } catch (Exception e) {
            System.err.println("Failed to start Database Engine: " + e.getMessage());
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    System.err.println("Failed to close connection during cleanup");
                }
            }
            throw new SQLException("Database engine startup failed", e);
        }
    }

    /**
     * Stop the database engine
     */
    public void stop() throws SQLException {
        if (!isStarted) {
            System.out.println("Database engine not started or already stopped");
            return;
        }

        try {
            System.out.println("Stopping Database Engine");
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connection closed");
            }
            isStarted = false;
            System.out.println("Database Engine stopped successfully");

        } catch (SQLException e) {
            System.err.println("Error stopping Database Engine: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if database connection is healthy
     */
    public boolean isConnectionHealthy() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get the database connection
     */
    public Connection getConnection() {
        if (!isStarted || connection == null) {
            throw new IllegalStateException("Database engine not started or connection not available");
        }
        return connection;
    }

    /**
     * Execute a SQL query
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        if (!isStarted) {
            throw new IllegalStateException("Database engine not started");
        }

        System.out.println("Executing SQL: " + sql);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        return rs;
    }

    /**
     * Execute a SQL update/insert/delete statement
     */
    public int executeUpdate(String sql) throws SQLException {
        if (!isStarted) {
            throw new IllegalStateException("Database engine not started");
        }

        System.out.println("Executing SQL Update: " + sql);
        Statement stmt = connection.createStatement();
        int rowsAffected = stmt.executeUpdate(sql);
        stmt.close();
        return rowsAffected;
    }

    /**
     * Test database connectivity
     */
    private boolean testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                ResultSet rs = connection.createStatement().executeQuery("SELECT 1");
                boolean hasResult = rs.next();
                rs.close();
                return hasResult;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if the database engine is started
     */
    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Print database statistics
     */
    public void printDatabaseStats() throws SQLException {
        if (!isStarted) {
            System.out.println("Database not started - cannot print stats");
            return;
        }

        System.out.println("Database Statistics:");

        String[] tables = {"TEAM", "PERSON", "PLAYER_ROLE", "COACH_ROLE", "CONTRACT"};

        for (String table : tables) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM " + table);
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println(table + ": " + count + " records");
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println(table + ": Error reading table - " + e.getMessage());
            }
        }
    }

    /**
     * Get database URL
     */
    public String getDatabaseUrl() {
        return dbUrl;
    }

    /**
     * Get database path
     */
    public String getDatabasePath() {
        return dbPath;
    }
}
