package app;

import database.CreateH2Database;
import database.DatabaseConnect;
import config.AppConfig;

/**
 * Main Application Entry Point
 * Launches the H2 Database to accept requests
 */
public class AppRunner {
    
    public static void main(String[] args) {
        System.out.println("Sport Ontology OBDA System");
        System.out.println("Initializing system...");
        
        try {
            // Step 1: Setup Database
            System.out.println("Setting up H2 Database...");
            CreateH2Database.main(new String[]{});
            
            // Step 2: Test Database Connection
            System.out.println("Testing database connection...");
            if (DatabaseConnect.testConnection()) {
                System.out.println("Database connection successful");
            } else {
                System.out.println("Database connection failed");
                return;
            }
            
            // Step 3: System Ready
            System.out.println("System initialized successfully");
            System.out.println("Database URL: " + AppConfig.getDatabaseUrl());
            System.out.println("Ontology Path: " + AppConfig.getOntologyPath());
            System.out.println("Mapping Path: " + AppConfig.getMappingPath());
            System.out.println("Run tests using: mvn test");
            
            System.out.println("Application completed successfully");
            
        } catch (Exception e) {
            System.out.println("Application failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                DatabaseConnect.closeConnection();
            } catch (Exception e) {
                System.out.println("Could not close database connection: " + e.getMessage());
            }
        }
    }
}
