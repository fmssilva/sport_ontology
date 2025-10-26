package protege_files;

/**
 * Configuration data holder for H2 database setup in Protege.
 * Contains all the paths, connection details, and instruction strings.
 */
public class DatabaseConfig {
    
    // Database connection parameters
    private final String dbDriverClassName;
    private final String dbDescription;
    private final String dbDriverJarPath;
    private final String dbConnectionURL;
    private final String dbUserName;
    private final String dbPassword;
    
    // Instruction strings for Protege setup
    private final String stepsToPreferencesOnProtege;
    private final String stepsToOntopConnectionOnProtege;
    
    public DatabaseConfig(String dbDriverClassName, String dbDescription, String dbDriverJarPath,
                         String dbConnectionURL, String dbUserName, String dbPassword,
                         String stepsToPreferencesOnProtege, String stepsToOntopConnectionOnProtege) {
        this.dbDriverClassName = dbDriverClassName;
        this.dbDescription = dbDescription;
        this.dbDriverJarPath = dbDriverJarPath;
        this.dbConnectionURL = dbConnectionURL;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.stepsToPreferencesOnProtege = stepsToPreferencesOnProtege;
        this.stepsToOntopConnectionOnProtege = stepsToOntopConnectionOnProtege;
    }
    
    // Getters
    public String getDbDriverClassName() { return dbDriverClassName; }
    public String getDbDescription() { return dbDescription; }
    public String getDbDriverJarPath() { return dbDriverJarPath; }
    public String getDbConnectionURL() { return dbConnectionURL; }
    public String getDbUserName() { return dbUserName; }
    public String getDbPassword() { return dbPassword; }
    public String getPreferencesSteps() { return stepsToPreferencesOnProtege; }
    public String getConnectionSteps() { return stepsToOntopConnectionOnProtege; }
    
    /**
     * Logs all configuration values for verification
     */
    public void logConfiguration() {
        System.out.println("   === H2 DATABASE CONFIGURATION ===");
        System.out.println("   Driver Class: " + dbDriverClassName);
        System.out.println("   Description: " + dbDescription);
        System.out.println("   JAR Path: " + dbDriverJarPath);
        System.out.println("   Connection URL: " + dbConnectionURL);
        System.out.println("   Username: " + dbUserName);
        System.out.println("   Password: " + (dbPassword.isEmpty() ? "(empty)" : "(set)"));
        System.out.println("   === PATH VERIFICATION ===");
        System.out.println("   Using forward slashes: " + (dbConnectionURL.contains("/") ? "YES" : "NO"));
        System.out.println("   Absolute path detected: " + (dbConnectionURL.contains(":") ? "YES" : "NO"));
        System.out.println("   =================================");
    }
    
    /**
     * Generates properties file content for H2_Config.properties
     */
    public String generatePropertiesContent() {
        StringBuilder config = new StringBuilder();
        
        // === PROTEGE JDBC DRIVER SETUP (File → Preferences → JDBC Drivers) ===
        config.append("# ========================================\n");
        config.append("# PROTEGE JDBC DRIVER CONFIGURATION\n");
        config.append("# For: File → Preferences → JDBC Drivers → Add Driver\n");
        config.append("# ========================================\n");
        config.append("driver.description=").append(dbDescription).append("\n");
        config.append("driver.class=").append(dbDriverClassName).append("\n");
        config.append("driver.jar=").append(dbDriverJarPath).append("\n\n");
        
        // === DATABASE CONNECTION PARAMETERS ===
        config.append("# ========================================\n");
        config.append("# DATABASE CONNECTION PARAMETERS\n");
        config.append("# For: Ontop Mappings → Connect to database\n");
        config.append("# ========================================\n");
        config.append("jdbc.driver=").append(dbDriverClassName).append("\n");
        config.append("jdbc.url=").append(dbConnectionURL).append("\n");
        config.append("jdbc.user=").append(dbUserName).append("\n");
        config.append("jdbc.password=").append(dbPassword).append("\n\n");
        
        // === SETUP INSTRUCTIONS ===
        config.append("# ========================================\n");
        config.append("# PROTEGE SETUP INSTRUCTIONS\n");
        config.append("# ========================================\n");
        config.append("# \n");
        config.append("# STEP 1: Install H2 Driver in Protege\n");
        config.append("# 1.1. Go to File → Preferences\n");
        config.append("# 1.2. Click 'JDBC Drivers' tab\n");
        config.append("# 1.3. Click 'Add Driver' button\n");
        config.append("# 1.4. Fill in the dialog:\n");
        config.append("#      Description: ").append(dbDescription).append("\n");
        config.append("#      Class name: ").append(dbDriverClassName).append("\n");
        config.append("#      Driver file (jar): ").append(dbDriverJarPath).append("\n");
        config.append("# 1.5. Click 'OK' to save\n");
        config.append("# \n");
        config.append("# STEP 2: Connect to Database in Ontop\n");
        config.append("# 2.1. Open your .owl file in Protege\n");
        config.append("# 2.2. Go to Window → Tabs → Ontop Mappings\n");
        config.append("# 2.3. Click 'Connect to database' button\n");
        config.append("# 2.4. Fill in the connection dialog:\n");
        config.append("#      Driver: Select '").append(dbDescription).append("' from dropdown\n");
        config.append("#      URL: ").append(dbConnectionURL).append("\n");
        config.append("#      Username: ").append(dbUserName).append("\n");
        config.append("#      Password: ").append(dbPassword.isEmpty() ? "(leave empty)" : dbPassword).append("\n");
        config.append("# 2.5. Click 'Test Connection' and then 'OK'\n");
        config.append("# \n");
        config.append("# DATABASE FILE LOCATION: ").append(dbConnectionURL.substring(dbConnectionURL.indexOf("jdbc:h2:") + 8, dbConnectionURL.indexOf(";"))).append(".mv.db\n");
        config.append("# H2 WEB CONSOLE: Start server with start_h2_server.bat and go to http://localhost:8082\n");
        
        return config.toString();
    }
}