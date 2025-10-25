package engines;

import database.CreateH2Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

/**
 * SQL Engine - Manages H2 database lifecycle and SQL operations
 * Handles database startup, connection management, and cleanup
 */
public class H2_SQLEngine {
    private Connection connection;
    private String dbPath;
    private String dbUrl;
    private boolean isStarted = false;
    
    public H2_SQLEngine() {
        // Use Maven project structure with AUTO_SERVER for concurrent access
        // Store database files in database folder instead of root directory
        Path databaseDir = Paths.get(System.getProperty("user.dir")).resolve("database");
        this.dbPath = databaseDir.resolve("sports-db").toString();
        this.dbUrl = "jdbc:h2:" + dbPath + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true;AUTO_SERVER=true;AUTO_SERVER_PORT=9092";
        
        // Ensure database directory exists
        try {
            if (!Files.exists(databaseDir)) {
                Files.createDirectories(databaseDir);
                System.out.println("📁 Created database directory: " + databaseDir);
            }
        } catch (Exception e) {
            System.err.println("⚠️  Warning: Could not create database directory: " + e.getMessage());
        }
    }
    
    /**
     * Start the database engine - creates/populates DB and establishes connection
     */
    public void start() throws SQLException {
        if (isStarted) {
            System.out.println("WARNING: Database engine already started");
            return;
        }
        
        try {
            System.out.println("🔄 Starting H2 Database Engine...");
            
            // Step 1: Create and populate database using existing code with correct path
            System.out.println("   📊 Creating/populating database schema...");
            CreateH2Database.main(new String[]{dbPath});
            
            // Step 2: Establish connection
            System.out.println("   🔗 Establishing connection...");
            connection = DriverManager.getConnection(dbUrl, "sa", "");
            
            // Step 3: Verify connection
            if (testConnection()) {
                isStarted = true;
                System.out.println("✅ Database Engine started successfully");
                System.out.println("   📍 Location: " + dbPath);
                System.out.println("   🔗 URL: " + dbUrl);
            } else {
                throw new SQLException("Database connection test failed");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start Database Engine: " + e.getMessage());
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    // Log but don't throw
                    System.err.println("Warning: Failed to close connection during cleanup");
                }
            }
            throw new SQLException("Database engine startup failed", e);
        }
    }
    
    /**
     * Stop the database engine and clean up resources
     */
    public void stop() throws SQLException {
        if (!isStarted) {
            System.out.println("⚠️  Database engine not started or already stopped");
            return;
        }
        
        try {
            System.out.println("🔄 Stopping Database Engine...");
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("   🔗 Connection closed");
            }
            isStarted = false;
            System.out.println("Database Engine stopped successfully");
            
        } catch (SQLException e) {
            System.err.println("❌ Error stopping Database Engine: " + e.getMessage());
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
     * Get the database connection for direct use
     */
    public Connection getConnection() {
        if (!isStarted || connection == null) {
            throw new IllegalStateException("Database engine not started or connection not available");
        }
        return connection;
    }
    
    /**
     * Execute a SQL query and return results
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        if (!isStarted) {
            throw new IllegalStateException("Database engine not started");
        }
        
        System.out.println("🔍 Executing SQL: " + sql);
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
        
        System.out.println("🔄 Executing SQL Update: " + sql);
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
                // Simple test query
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
     * Get database statistics for debugging
     */
    public void printDatabaseStats() throws SQLException {
        if (!isStarted) {
            System.out.println("❌ Database not started - cannot print stats");
            return;
        }
        
        System.out.println("\n📊 DATABASE STATISTICS:");
        
        String[] tables = {"TEAM", "PERSON", "PLAYER_ROLE", "COACH_ROLE", "CONTRACT"};
        
        for (String table : tables) {
            try {
                ResultSet rs = executeQuery("SELECT COUNT(*) FROM " + table);
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("   📋 " + table + ": " + count + " records");
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("   ❌ " + table + ": Error reading table - " + e.getMessage());
            }
        }
        System.out.println();
    }
    
    /**
     * Get database URL for external tools (like Ontop)
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