package config;

/**
 * Application Configuration
 * Centralizes all configuration constants for the OBDA system
 */
public class AppConfig {
    
    // Database Configuration
    public static final String DB_URL = "jdbc:h2:./sports-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    public static final String DB_USER = "sa";
    public static final String DB_PASSWORD = "";
    public static final String DB_DRIVER = "org.h2.Driver";
    
    // Ontology Configuration (for informational display)
    public static final String ONTOLOGY_PATH = "/ontology/sport-ontology.owl";
    public static final String MAPPING_PATH = "/ontology/sport-ontology.obda";
}