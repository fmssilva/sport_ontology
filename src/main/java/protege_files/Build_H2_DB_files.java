package protege_files;

import config.AppConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Specialized builder for H2 database configuration and setup files.
 * Handles all database-related tasks for Protege OBDA integration.
 */
public class Build_H2_DB_files {
    
    /**
     * Creates complete H2 database setup with configuration files and documentation.
     * 
     * @param baseDir The absolute path to the protege_files directory
     * @return DatabaseConfig object containing all configuration parameters
     */
    public static Build_H2_DB_config_file buildH2Setup(String baseDir) {
        System.out.println("\n>>> Building H2 Database Configuration...");
        
        try {
            // Generate absolute paths with cross-platform compatibility
            String projectRoot = System.getProperty("user.dir");
            String sourceH2JarPath = Paths.get(projectRoot, "target", "dependency", "h2-2.4.240.jar").toString();
            
            // H2 JAR will be copied to protege_files/database folder
            String h2JarPath = Paths.get(baseDir, "database", "h2-2.4.240.jar").toAbsolutePath().toString();
            
            // Use absolute path for database file - critical for Protégé compatibility
            String dbFilePath = Paths.get(baseDir, "database", AppConfig.DATABASE_NAME).toAbsolutePath().toString();
            
            // Cross-platform path normalization (convert backslashes to forward slashes)
            // This is especially important for Protégé which prefers Unix-style paths
            h2JarPath = h2JarPath.replace("\\", "/");
            dbFilePath = dbFilePath.replace("\\", "/");
            
            System.out.println("   Project root: " + projectRoot);
            System.out.println("   Source H2 JAR: " + sourceH2JarPath);
            System.out.println("   Target H2 JAR path: " + h2JarPath);
            System.out.println("   Database file path (absolute): " + dbFilePath);
            
            // Ensure H2 JAR is available by downloading dependencies automatically
            ensureH2JarAvailable(projectRoot, sourceH2JarPath);
            
            // Copy H2 JAR file to database folder first (needed for config paths)
            copyH2JarFile(baseDir, sourceH2JarPath);
            
            // Create DatabaseConfig with all parameters (using target JAR path)
            Build_H2_DB_config_file config = createDatabaseConfig(h2JarPath, dbFilePath);
            
            // Log configuration for verification
            config.logConfiguration();
            
            // === DETAILED PROTEGE CONNECTION LOG ===
            System.out.println("\n   === PROTEGE CONNECTION DETAILS ===");
            System.out.println("   For Protege OBDA Connection Dialog:");
            System.out.println("   URL: " + config.getDbConnectionURL());
            System.out.println("   Driver: " + config.getDbDriverClassName());
            System.out.println("   Username: " + config.getDbUserName());
            System.out.println("   Password: " + (config.getDbPassword().isEmpty() ? "(leave empty)" : config.getDbPassword()));
            System.out.println("   JAR Location: " + config.getDbDriverJarPath());
            System.out.println("   ===================================");
            
            // Create configuration files
            createH2ConfigFile(baseDir, config);
            createH2DocumentationFile(baseDir, config);
            createH2ServerScripts(baseDir, dbFilePath);
            
            // Copy seeded database files
            copySeededDatabase(baseDir);
            
            System.out.println("   ✓ H2 Database setup completed successfully");
            return config;
            
        } catch (Exception e) {
            System.err.println("   ✗ ERROR: Failed to build H2 setup - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("H2 database setup failed", e);
        }
    }
    
    /**
     * Creates the DatabaseConfig object with all H2 parameters
     */
    private static Build_H2_DB_config_file createDatabaseConfig(String h2JarPath, String dbFilePath) {
        String dbDriverClassName = "org.h2.Driver";
        String dbDescription = "H2 Database for Sport Ontology OBDA";
        String dbConnectionURL = "jdbc:h2:" + dbFilePath + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
        String dbUserName = "sa";
        String dbPassword = "";
        
        String preferencesSteps = generatePreferencesInstructions();
        String connectionSteps = generateConnectionInstructions();
        
        return new Build_H2_DB_config_file(dbDriverClassName, dbDescription, h2JarPath, 
                                 dbConnectionURL, dbUserName, dbPassword,
                                 preferencesSteps, connectionSteps);
    }
    
    /**
     * Creates H2_Config.properties file
     */
    private static void createH2ConfigFile(String baseDir, Build_H2_DB_config_file config) throws IOException {
        Path configPath = Paths.get(baseDir, "database", "H2_Config.properties");
        String content = config.generatePropertiesContent();
        Files.write(configPath, content.getBytes());
        System.out.println("   ✓ Created: " + configPath.getFileName());
    }
    
    /**
     * Creates comprehensive documentation file
     */
    private static void createH2DocumentationFile(String baseDir, Build_H2_DB_config_file config) throws IOException {
        Path docPath = Paths.get(baseDir, "PROTEGE_SET_UP.md");
        String content = generateProtegeSetupDocumentation(config);
        Files.write(docPath, content.getBytes());
        System.out.println("   ✓ Created: " + docPath.getFileName());
    }
    
    /**
     * Creates H2 server startup scripts for all platforms
     */
    private static void createH2ServerScripts(String baseDir, String dbFilePath) throws IOException {
        String databaseDir = Paths.get(baseDir, "database").toString();
        createWindowsScript(databaseDir, dbFilePath);
        createLinuxScript(databaseDir, dbFilePath);
        createMacScript(databaseDir, dbFilePath);
    }
    
    /**
     * Ensures H2 JAR is available by automatically downloading Maven dependencies if needed
     */
    private static void ensureH2JarAvailable(String projectRoot, String sourceH2JarPath) {
        Path sourceJar = Paths.get(sourceH2JarPath);
        
        if (!Files.exists(sourceJar)) {
            System.out.println("   ! H2 JAR not found at: " + sourceH2JarPath);
            System.out.println("   ! Automatically downloading Maven dependencies...");
            
            try {
                // Run Maven dependency:copy-dependencies command
                ProcessBuilder pb = new ProcessBuilder();
                
                // Set the command based on OS
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    pb.command("cmd", "/c", "mvn dependency:copy-dependencies -q");
                } else {
                    pb.command("bash", "-c", "mvn dependency:copy-dependencies -q");
                }
                
                pb.directory(new java.io.File(projectRoot));
                pb.redirectErrorStream(true);
                
                Process process = pb.start();
                
                // Read the output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty() && !line.contains("WARNING")) {
                            System.out.println("   " + line);
                        }
                    }
                }
                
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("   ✓ Maven dependencies downloaded successfully");
                    
                    // Verify H2 JAR is now available
                    if (Files.exists(sourceJar)) {
                        System.out.println("   ✓ H2 JAR confirmed at: " + sourceH2JarPath);
                    } else {
                        throw new RuntimeException("H2 JAR still not found after dependency download");
                    }
                } else {
                    throw new RuntimeException("Maven dependency download failed with exit code: " + exitCode);
                }
                
            } catch (Exception e) {
                System.err.println("   ✗ Failed to download Maven dependencies: " + e.getMessage());
                System.err.println("   ! Please run 'mvn dependency:copy-dependencies' manually");
                throw new RuntimeException("Unable to ensure H2 JAR availability", e);
            }
        } else {
            System.out.println("   ✓ H2 JAR found at: " + sourceH2JarPath);
        }
    }
    
    /**
     * Copies H2 JAR file from target/dependency to database folder
     */
    private static void copyH2JarFile(String baseDir, String sourceH2JarPath) throws IOException {
        Path sourceJar = Paths.get(sourceH2JarPath);
        Path targetJar = Paths.get(baseDir, "database", "h2-2.4.240.jar");
        
        // Ensure target directory exists
        Files.createDirectories(targetJar.getParent());
        
        if (Files.exists(sourceJar)) {
            try {
                Files.copy(sourceJar, targetJar, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("   ✓ Copied H2 JAR to: " + targetJar.getFileName());
            } catch (IOException e) {
                if (Files.exists(targetJar)) {
                    System.out.println("   ✓ H2 JAR already exists: " + targetJar.getFileName());
                } else {
                    System.err.println("   ! Failed to copy H2 JAR: " + e.getMessage());
                    throw e;
                }
            }
        } else {
            System.out.println("   ! H2 JAR not found at: " + sourceH2JarPath);
            System.out.println("   ! Run 'mvn dependency:copy-dependencies' first");
        }
    }
    
    /**
     * Creates and copies seeded database files
     */
    private static void copySeededDatabase(String baseDir) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        Path sourceDbDir = Paths.get(projectRoot, "database");
        Path targetDbDir = Paths.get(baseDir, "database");
        
        // First, try to create the seeded database by calling CreateH2Database directly
        try {
            System.out.println("   Creating seeded database...");
            database.CreateH2Database.main(new String[0]);
            System.out.println("   ✓ Seeded database created successfully");
        } catch (Exception e) {
            System.out.println("   ! Failed to create seeded database: " + e.getMessage());
            System.out.println("   ! Continuing with existing database files if available...");
        }
        
        // Copy database files from project database folder to protege_files/database
        if (Files.exists(sourceDbDir)) {
            try (var paths = Files.walk(sourceDbDir)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.getFileName().toString().startsWith(AppConfig.DATABASE_NAME))
                     .forEach(sourceFile -> {
                         try {
                             String fileName = sourceFile.getFileName().toString();
                             Path targetFile = targetDbDir.resolve(fileName);
                             Files.copy(sourceFile, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                             System.out.println("   ✓ Copied seeded DB: " + fileName);
                         } catch (IOException e) {
                             System.err.println("   ! Failed to copy DB file: " + sourceFile.getFileName());
                         }
                     });
            }
        } else {
            System.out.println("   ! No seeded database found at: " + sourceDbDir);
            System.out.println("   ! Database files must be created first");
        }
    }
    
    /**
     * Creates Windows batch script for H2 server
     */
    private static void createWindowsScript(String baseDir, String dbFilePath) throws IOException {
        Path scriptPath = Paths.get(baseDir, "start_h2_server.bat");
        String content = generateWindowsServerScript(dbFilePath);
        Files.write(scriptPath, content.getBytes());
        System.out.println("   ✓ Created: " + scriptPath.getFileName());
    }
    
    /**
     * Creates Linux shell script for H2 server
     */
    private static void createLinuxScript(String baseDir, String dbFilePath) throws IOException {
        Path scriptPath = Paths.get(baseDir, "start_h2_server.sh");
        String content = generateLinuxServerScript(dbFilePath);
        Files.write(scriptPath, content.getBytes());
        
        // Make executable on Unix systems
        try {
            Runtime.getRuntime().exec("chmod +x " + scriptPath.toString());
        } catch (Exception e) {
            // Ignore on Windows
        }
        
        System.out.println("   ✓ Created: " + scriptPath.getFileName());
    }
    
    /**
     * Creates Mac shell script for H2 server
     */
    private static void createMacScript(String baseDir, String dbFilePath) throws IOException {
        Path scriptPath = Paths.get(baseDir, "start_h2_server_mac.sh");
        String content = generateMacServerScript(dbFilePath);
        Files.write(scriptPath, content.getBytes());
        
        // Make executable on Unix systems
        try {
            Runtime.getRuntime().exec("chmod +x " + scriptPath.toString());
        } catch (Exception e) {
            // Ignore on Windows
        }
        
        System.out.println("   ✓ Created: " + scriptPath.getFileName());
    }
    
    // === CONTENT GENERATION METHODS ===
    
    private static String generatePreferencesInstructions() {
        return "1. Open Protege\\n" +
               "2. Go to File → Preferences\\n" +
               "3. Navigate to JDBC Drivers tab\\n" +
               "4. Click 'Add Driver'\\n" +
               "5. Set Description: H2 Database for Sport Ontology OBDA\\n" +
               "6. Set Driver Class: org.h2.Driver\\n" +
               "7. Set H2 JAR location: Browse to h2-2.4.240.jar file or insert the absolute path\\n" +
               "8. Click OK to save driver and check if status is ready\\n";
    }
    
    private static String generateConnectionInstructions() {
        return "1. Go to Window → Tabs → Ontop Mappings (and in view go and add the ontop view to see the tabs in the main window)\\n" +
               "2. Click 'Connection parameters' tab\\n" +
               "3. Select the H2 driver class: org.h2.Driver\\n" +
               "4. Enter the connection URL from H2_Config.properties\\n" +
               "5. Username: sa\\n" +
               "6. Password: (leave empty)\\n" +
               "7. Test connection and save";
    }
    
    private static String generateProtegeSetupDocumentation(Build_H2_DB_config_file config) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# Protege H2 Database Setup Guide\n\n");
        sb.append("This guide provides step-by-step instructions for setting up H2 database integration with Protege for OBDA (Ontology-Based Data Access).\n\n");
        
        sb.append("## Prerequisites\n\n");
        sb.append("- Protege 5.x installed\n");
        sb.append("- Ontop plugin installed in Protege\n");
        sb.append("- H2 database JAR file: `").append(config.getDbDriverJarPath()).append("`\n\n");
        
        sb.append("## Step 1: Install H2 Driver in Protege\n\n");
        sb.append("1. Copy `h2-2.2.224.jar` to your Protege `plugins/` directory\n");
        sb.append("2. Restart Protege completely\n");
        sb.append("3. Verify the driver is loaded:\n");
        sb.append("   - Go to **File → Preferences**\n");
        sb.append("   - Click **JDBC Drivers** tab\n");
        sb.append("   - Click **Add Driver**\n");
        sb.append("   - Browse to: `").append(config.getDbDriverJarPath()).append("`\n");
        sb.append("   - Driver Class: `").append(config.getDbDriverClassName()).append("`\n");
        sb.append("   - Description: `").append(config.getDbDescription()).append("`\n");
        sb.append("   - Click **OK**\n\n");
        
        sb.append("## Step 2: Database Configuration\n\n");
        sb.append("**Connection Details (IMPORTANT: Use absolute paths for Protégé):**\n");
        sb.append("- **Driver Class:** `").append(config.getDbDriverClassName()).append("`\n");
        sb.append("- **Connection URL:** `").append(config.getDbConnectionURL()).append("`\n");
        sb.append("- **Username:** `").append(config.getDbUserName()).append("`\n");
        sb.append("- **Password:** `").append(config.getDbPassword().isEmpty() ? "(empty)" : config.getDbPassword()).append("`\n\n");
        sb.append("**Note:** The connection URL uses absolute paths with forward slashes for cross-platform compatibility.\n");
        sb.append("This is essential for Protégé to properly connect to the H2 database.\n\n");
        
        sb.append("## Step 3: Connect to Database in Protege\n\n");
        sb.append("1. Open your `.owl` ontology file in Protege\n");
        sb.append("2. Go to **Window → Tabs → Ontop Mappings**\n");
        sb.append("3. Click **Connect to database** button\n");
        sb.append("4. In the connection dialog:\n");
        sb.append("   - **Driver:** Select \"H2 Database\" from dropdown\n");
        sb.append("   - **URL:** `").append(config.getDbConnectionURL()).append("`\n");
        sb.append("   - **Username:** `").append(config.getDbUserName()).append("`\n");
        sb.append("   - **Password:** Leave empty\n");
        sb.append("5. Click **Test Connection** to verify\n");
        sb.append("6. Click **OK** to save connection\n\n");
        
        sb.append("## Step 4: Load Database Schema\n\n");
        sb.append("1. Once connected, the database tables should appear in the Ontop Mappings tab\n");
        sb.append("2. You can now create mappings between your ontology and database tables\n");
        sb.append("3. Use the mapping editor to define how RDF triples are generated from relational data\n\n");
        
        sb.append("## H2 Server Scripts\n\n");
        sb.append("For independent H2 server management:\n\n");
        sb.append("- **Windows:** Run `start_h2_server.bat`\n");
        sb.append("- **Linux:** Run `./start_h2_server.sh`\n");
        sb.append("- **Mac:** Run `./start_h2_server_mac.sh`\n\n");
        sb.append("These scripts start H2 in server mode with web console access at `http://localhost:8082`\n\n");
        
        sb.append("## Troubleshooting\n\n");
        sb.append("**Connection Issues:**\n");
        sb.append("- Ensure H2 JAR is in Protege plugins directory\n");
        sb.append("- Restart Protege after adding the driver\n");
        sb.append("- Check that the database file path exists\n");
        sb.append("- Verify forward slashes in connection URL\n\n");
        
        sb.append("**Database Access:**\n");
        sb.append("- H2 creates database files automatically\n");
        sb.append("- Database files are stored in: `database/" + AppConfig.DATABASE_NAME + ".*`\n");
        sb.append("- Web console available at: `http://localhost:8082` (when using server scripts)\n\n");
        
        sb.append("## Configuration Files\n\n");
        sb.append("- `H2_Config.properties` - JDBC connection properties\n");
        sb.append("- `" + AppConfig.MAPPING_FILE_NAME + "` - R2RML mapping definitions\n");
        sb.append("- `" + AppConfig.OBDA_FILE_NAME + "` - Ontop OBDA mapping definitions (optional)\n");
        sb.append("- `" + AppConfig.ONTOLOGY_FILE_NAME + "` - OWL ontology file\n\n");
        
        return sb.toString();
    }
    
    private static String generateWindowsServerScript(String dbFilePath) {
        return "@echo off\r\n" +
               "echo Starting H2 Database Server...\r\n" +
               "echo Database: " + dbFilePath + "\r\n" +
               "echo Web Console: http://localhost:8082\r\n" +
               "echo H2 JAR: h2-2.4.240.jar (local)\r\n" +
               "echo.\r\n" +
               "if not exist \"h2-2.4.240.jar\" (\r\n" +
               "    echo ERROR: h2-2.4.240.jar not found in current directory\r\n" +
               "    echo Please ensure the H2 JAR file is present\r\n" +
               "    pause\r\n" +
               "    exit /b 1\r\n" +
               ")\r\n" +
               "java -cp \"h2-2.4.240.jar\" org.h2.tools.Server -tcp -web -tcpAllowOthers -webAllowOthers\r\n" +
               "pause\r\n";
    }
    
    private static String generateLinuxServerScript(String dbFilePath) {
        return "#!/bin/bash\n" +
               "echo \"Starting H2 Database Server...\"\n" +
               "echo \"Database: " + dbFilePath + "\"\n" +
               "echo \"Web Console: http://localhost:8082\"\n" +
               "echo \"H2 JAR: h2-2.4.240.jar (local)\"\n" +
               "echo\n" +
               "if [ ! -f \"h2-2.4.240.jar\" ]; then\n" +
               "    echo \"ERROR: h2-2.4.240.jar not found in current directory\"\n" +
               "    echo \"Please ensure the H2 JAR file is present\"\n" +
               "    exit 1\n" +
               "fi\n" +
               "java -cp \"h2-2.4.240.jar\" org.h2.tools.Server -tcp -web -tcpAllowOthers -webAllowOthers\n";
    }
    
    private static String generateMacServerScript(String dbFilePath) {
        return "#!/bin/bash\n" +
               "echo \"Starting H2 Database Server on Mac...\"\n" +
               "echo \"Database: " + dbFilePath + "\"\n" +
               "echo \"Web Console: http://localhost:8082\"\n" +
               "echo \"H2 JAR: h2-2.4.240.jar (local)\"\n" +
               "echo\n" +
               "echo \"Note: You may need to allow Java network access in System Preferences > Security & Privacy\"\n" +
               "if [ ! -f \"h2-2.4.240.jar\" ]; then\n" +
               "    echo \"ERROR: h2-2.4.240.jar not found in current directory\"\n" +
               "    echo \"Please ensure the H2 JAR file is present\"\n" +
               "    exit 1\n" +
               "fi\n" +
               "echo\n" +
               "java -cp \"h2-2.4.240.jar\" org.h2.tools.Server -tcp -web -tcpAllowOthers -webAllowOthers\n";
    }
}