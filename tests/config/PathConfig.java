package config;

import java.nio.file.*;
import java.io.File;

/**
 * Cross-platform path configuration utility
 * Handles all path-related operations for the OBDA testing framework
 */
public class PathConfig {
    
    // Base paths
    private static final Path CURRENT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final Path PROJECT_ROOT = CURRENT_DIR.getParent();
    
    // Configuration directories
    public static final Path CONFIG_DIR = CURRENT_DIR.resolve("config");
    public static final Path BUILD_DIR = CURRENT_DIR.resolve("build");
    
    // Database paths
    public static final Path DATABASE_DIR = PROJECT_ROOT.resolve("database");
    public static final Path H2_JAR = PROJECT_ROOT.resolve("tools").resolve("h2").resolve("h2-2.4.240.jar");
    public static final Path SPORTS_DB = DATABASE_DIR.resolve("sports-db");
    
    // Ontop paths
    public static final Path ONTOP_LIB_DIR = PROJECT_ROOT.resolve("tools").resolve("ontop").resolve("lib");
    
    // Configuration files
    public static final Path ONTOLOGY_FILE = CONFIG_DIR.resolve("sport-ontology.owl");
    public static final Path MAPPING_FILE = CONFIG_DIR.resolve("sport-ontology-mapping.ttl");
    public static final Path H2_PROPERTIES = CONFIG_DIR.resolve("h2.properties");
    
    /**
     * Get H2 JDBC URL with cross-platform path
     * Uses system-specific path separators automatically
     */
    public static String getH2DatabaseUrl() {
        String dbPath = SPORTS_DB.toString();
        // H2 accepts forward slashes on all platforms
        dbPath = dbPath.replace("\\", "/");
        return "jdbc:h2:" + dbPath + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    }
    
    /**
     * Get Ontop classpath with all required JARs
     */
    public static String getOntopClasspath() {
        return ONTOP_LIB_DIR.toString() + File.separator + "*";
    }
    
    /**
     * Convert path to Ontop-compatible format (forward slashes and relative)
     * Ontop CLI requires local/relative paths and has issues with Windows backslashes
     */
    public static String toOntopPath(Path path) {
        // For Ontop CLI, use relative paths from the working directory
        Path relativePath = CURRENT_DIR.relativize(path);
        String pathStr = relativePath.toString();
        
        // Always use forward slashes for Ontop CLI compatibility
        // This works across all operating systems
        return pathStr.replace("\\", "/");
    }
    
    /**
     * Get cross-platform file separator (use system default)
     */
    public static String getFileSeparator() {
        return File.separator;
    }
    
    /**
     * Check if running on Windows
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    /**
     * Get platform-specific command prefix for running Java processes
     */
    public static String[] getJavaCommand() {
        if (isWindows()) {
            return new String[]{"java"};
        } else {
            return new String[]{"java"};
        }
    }
    
    /**
     * Get the working directory for process execution
     */
    public static File getWorkingDirectory() {
        return CURRENT_DIR.toFile();
    }
    
    /**
     * Create build directory if it doesn't exist
     */
    public static void ensureBuildDirectory() {
        try {
            Files.createDirectories(BUILD_DIR);
        } catch (Exception e) {
            System.err.println("Warning: Could not create build directory: " + e.getMessage());
        }
    }
    
    /**
     * Get OS-specific information for debugging
     */
    public static void printPathInfo() {
        System.out.println("=== Path Configuration ===");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Working Directory: " + CURRENT_DIR);
        System.out.println("Project Root: " + PROJECT_ROOT);
        System.out.println("Database URL: " + getH2DatabaseUrl());
        System.out.println("Ontop Classpath: " + getOntopClasspath());
        System.out.println("==========================");
    }
}