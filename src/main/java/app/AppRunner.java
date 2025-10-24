package app;

import database.CreateH2Database;
import config.AppConfig;
import config.DatabaseConfig;

/**
 * Main Application Entry Point
 * In a normal app this would launch the REST endpoints, etc...
 * In our case just launches the H2 DB to accept requests
 */
public class AppRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Sport Ontology OBDA System ===");
        System.out.println("Initializing Maven-based system...");
        
        try {
            // Step 1: Setup Database
            System.out.println("\n1. Setting up H2 Database...");
            CreateH2Database.main(new String[]{});
            
            // Step 2: Test Database Connection
            System.out.println("\n2. Testing database connection...");
            if (DatabaseConfig.testConnection()) {
                System.out.println("Database connection successful!");
            } else {
                System.err.println("❌ Database connection failed!");
                return;
            }
            
            // Step 3: System Ready
            System.out.println("\n3. System initialized successfully!");
            System.out.println("Database is ready at: " + AppConfig.DB_URL);
            System.out.println("Ontology available at: " + AppConfig.ONTOLOGY_PATH);
            System.out.println("Mappings available at: " + AppConfig.MAPPING_PATH);
            System.out.println("\nTo run tests:");
            System.out.println("  mvn test                    # Run all tests");
            System.out.println("  mvn test -Dtest=SQLTester   # Run SQL tests only");
            
            System.out.println("\nApplication completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Application failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                DatabaseConfig.closeConnection();
            } catch (Exception e) {
                System.err.println("Warning: Could not close database connection: " + e.getMessage());
            }
        }
    }
}