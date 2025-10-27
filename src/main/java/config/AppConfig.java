package config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application Configuration
 * Centralizes configuration constants for the sport ontology OBDA system
 * All paths are cross-platform compatible using java.nio.file.Path
 */
public class AppConfig {
    
    // =============================================================================
    // DATABASE CONFIGURATION
    // =============================================================================
    
    /**
     * Database name - used for H2 database file naming
     */
    public static final String DATABASE_NAME = "sport_db";
    
    /**
     * H2 Database Configuration Options:
     * 
     * DATABASE_TO_UPPER=true
     * → Converts all unquoted identifiers (table names, column names) to UPPERCASE
     * → This is IMPORTANT because our R2RML mappings in sport-ontology-mapping.ttl
     *   reference tables like "TEAM", "PERSON" in uppercase
     * → Without this, H2 would store tables as lowercase but mappings expect uppercase
     * → Ensures compatibility between SQL schema and R2RML mappings
     * 
     * CASE_INSENSITIVE_IDENTIFIERS=true  
     * → Makes identifier comparison case-insensitive for queries
     * → This is IMPORTANT because it allows flexible querying:
     *   - SQL can use "select * from team" or "SELECT * FROM TEAM"
     *   - Both will work even though table is stored as "TEAM"
     * → Improves compatibility with different SQL writing styles
     * → Reduces errors from case mismatches in queries
     * 
     * Combined effect: Tables stored as UPPERCASE, queries work with any case
     */
    public static final String DB_URL = "jdbc:h2:./" + DATABASE_NAME + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    public static final String DB_USER = "sa";
    public static final String DB_PASSWORD = "";
    public static final String DB_DRIVER = "org.h2.Driver";
    
    // =============================================================================
    // ONTOLOGY NAMESPACE CONFIGURATION
    // =============================================================================
    
    /**
     * Ontology name - used as base for file naming and URIs
     */
    public static final String ONTOLOGY_NAME = "sport-ontology";
    
    /**
     * Base namespace URIs for the sport ontology system
     * These are used for creating IRIs in reasoning and SPARQL operations
     */
    public static final String ONTOLOGY_NAMESPACE = "http://www.semanticweb.org/sports/ontology#";
    public static final String ABOX_NAMESPACE = "http://www.semanticweb.org/sports/abox#";
    
    /**
     * File naming based on ontology name
     */
    public static final String ONTOLOGY_FILE_NAME = ONTOLOGY_NAME + ".owl";
    public static final String MAPPING_FILE_NAME = ONTOLOGY_NAME + "-mapping.ttl";
    public static final String OBDA_FILE_NAME = ONTOLOGY_NAME + ".obda";
    public static final String PROPERTIES_FILE_NAME = ONTOLOGY_NAME + "-simple.properties";
    
    // =============================================================================
    // CROSS-PLATFORM PATH CONFIGURATION  
    // =============================================================================
    
    /**
     * Base directories - all paths calculated relative to project root
     * Using Paths.get() ensures cross-platform compatibility (Windows/Unix/Mac)
     */
    public static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    public static final Path DATABASE_DIR = PROJECT_ROOT.resolve("database");
    public static final Path RESOURCES_DIR = PROJECT_ROOT.resolve("src").resolve("main").resolve("resources");
    public static final Path ONTOLOGY_DIR = RESOURCES_DIR.resolve("ontology");
    public static final Path TOOLS_DIR = PROJECT_ROOT.resolve("tools");
    
    /**
     * Ontology file paths - used by reasoning and SPARQL engines
     */
    public static final Path ONTOLOGY_FILE = ONTOLOGY_DIR.resolve(ONTOLOGY_FILE_NAME);
    public static final Path MAPPING_FILE = ONTOLOGY_DIR.resolve(MAPPING_FILE_NAME);
    public static final Path OBDA_FILE = ONTOLOGY_DIR.resolve(OBDA_FILE_NAME);
    public static final Path PROPERTIES_FILE = ONTOLOGY_DIR.resolve(PROPERTIES_FILE_NAME);
    
    /**
     * Database file paths
     */
    public static final Path DATABASE_FILE = DATABASE_DIR.resolve(DATABASE_NAME);
    
    /**
     * Ontop CLI paths - checked in order of preference
     */
    public static final Path[] ONTOP_CLI_PATHS = {
        TOOLS_DIR.resolve("ontop").resolve(isWindows() ? "ontop.bat" : "ontop"),
        TOOLS_DIR.resolve(isWindows() ? "ontop.bat" : "ontop"),
        Paths.get(isWindows() ? "ontop.bat" : "ontop") // System PATH
    };
    
    // =============================================================================
    // UTILITY METHODS
    // =============================================================================
    
    /**
     * Get database URL with cross-platform path
     */
    public static String getDatabaseUrl() {
        return "jdbc:h2:" + DATABASE_FILE.toString() + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    }
    
    /**
     * Get absolute database URL for Protégé with Linux format conversion
     * Protégé works better with forward slashes even on Windows
     */
    public static String getDatabaseUrlForProtege() {
        String absolutePath = DATABASE_FILE.toAbsolutePath().toString().replace("\\", "/");
        return "jdbc:h2:" + absolutePath + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    }
    
    /**
     * Get database URL with server mode for concurrent access
     */
    public static String getDatabaseUrlWithServer(int port) {
        return getDatabaseUrl() + ";AUTO_SERVER=true;AUTO_SERVER_PORT=" + port;
    }
    
    /**
     * Check if running on Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * Get string paths for backward compatibility
     * NOTE: For SPARQL engine, use relative paths since Ontop CLI works best with them
     */
    public static String getOntologyPath() {
        return "src/main/resources/ontology/" + ONTOLOGY_FILE_NAME;
    }
    
    public static String getMappingPath() {
        return "src/main/resources/ontology/" + MAPPING_FILE_NAME;
    }
    
    public static String getObdaPath() {
        return "src/main/resources/ontology/" + OBDA_FILE_NAME;
    }
    
    public static String getPropertiesPath() {
        return "src/main/resources/ontology/" + PROPERTIES_FILE_NAME;
    }
    
    /**
     * Get absolute paths for file operations (ReasoningEngine, etc.)
     */
    public static String getOntologyAbsolutePath() {
        return ONTOLOGY_FILE.toString();
    }
    
    public static String getMappingAbsolutePath() {
        return MAPPING_FILE.toString();
    }
    
    public static String getObdaAbsolutePath() {
        return OBDA_FILE.toString();
    }
    
    public static String getPropertiesAbsolutePath() {
        return PROPERTIES_FILE.toString();
    }
    
    /**
     * Create ontology class IRI from class name
     * @param className The class name (e.g., "Team", "Player")
     * @return Full IRI for the class
     */
    public static String createOntologyIRI(String className) {
        return ONTOLOGY_NAMESPACE + className;
    }
    
    /**
     * Create ABox individual IRI from individual name
     * @param individualName The individual name (e.g., "team1", "player1")
     * @return Full IRI for the individual
     */
    public static String createABoxIRI(String individualName) {
        return ABOX_NAMESPACE + individualName;
    }
    
    /**
     * Create SPARQL PREFIX declaration for ontology namespace
     * @return PREFIX declaration for use in SPARQL queries
     */
    public static String getSPARQLPrefix() {
        return "PREFIX : <" + ONTOLOGY_NAMESPACE + "> ";
    }
}
