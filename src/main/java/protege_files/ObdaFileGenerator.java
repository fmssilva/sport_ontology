package protege_files;

import config.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates OBDA files for Protégé integration.
 * OBDA files contain database connection information and can include native OBDA mappings.
 */
public class ObdaFileGenerator {
    
    /**
     * Generates a complete OBDA file with H2 database connection for Protégé
     * Always includes converted R2RML mappings
     * 
     * @param outputDir The directory where the OBDA file will be created
     * @param dbConfig Database configuration containing connection details
     * @return Path to the generated OBDA file
     * @throws IOException if file creation fails
     */
    public static Path generateObdaFile(String outputDir, Build_H2_DB_config_file dbConfig) throws IOException {
        return generateObdaFile(outputDir, dbConfig, true);
    }
    
    /**
     * Generates a complete OBDA file with H2 database connection for Protégé
     * 
     * @param outputDir The directory where the OBDA file will be created
     * @param dbConfig Database configuration containing connection details
     * @param includeR2RMLMappings Whether to convert and include R2RML mappings in OBDA format
     * @return Path to the generated OBDA file
     * @throws IOException if file creation fails
     */
    public static Path generateObdaFile(String outputDir, Build_H2_DB_config_file dbConfig, boolean includeR2RMLMappings) throws IOException {
        Path obdaPath = Paths.get(outputDir, "ontology", AppConfig.OBDA_FILE_NAME);
        
        // Ensure parent directory exists
        Files.createDirectories(obdaPath.getParent());
        
        // Always generate complete OBDA with mappings
        String obdaContent = generateObdaContentWithMappings(dbConfig);
        System.out.println("   ✓ Created OBDA file: " + AppConfig.OBDA_FILE_NAME + " (with complete R2RML mappings)");
        
        Files.write(obdaPath, obdaContent.getBytes());
        System.out.println("   → Contains H2 database connection for Protégé");
        System.out.println("   → Uses relative path: ./plugins/h2-2.4.240.jar (portable across systems)");
        
        return obdaPath;
    }
    
    /**
     * Generates OBDA content with converted R2RML mappings
     */
    private static String generateObdaContentWithMappings(Build_H2_DB_config_file dbConfig) throws IOException {
        StringBuilder obda = new StringBuilder();
        
        // Add standard OBDA header (same as before)
        obda.append(generateObdaHeader(dbConfig));
        
        // Convert R2RML mappings to OBDA format
        String r2rmlMappings = convertR2RMLToOBDA();
        
        if (r2rmlMappings != null && !r2rmlMappings.trim().isEmpty()) {
            obda.append("[MappingDeclaration] @collection [[\n");
            obda.append(r2rmlMappings);
            obda.append("]]\n");
        } else {
            // Fallback to empty mapping section
            obda.append("[MappingDeclaration] @collection [[\n");
            obda.append("]]\n");
        }
        
        return obda.toString();
    }
    
    /**
     * Generates the OBDA header (prefixes and source declaration)
     * Uses Protégé plugins path as default for H2 JAR location
     */
    private static String generateObdaHeader(Build_H2_DB_config_file dbConfig) {
        StringBuilder obda = new StringBuilder();
        
        // OBDA file header with prefixes
        obda.append("[PrefixDeclaration]\n");
        obda.append(":").append("\t").append(AppConfig.ONTOLOGY_NAMESPACE).append("\n");
        obda.append("owl:").append("\t").append("http://www.w3.org/2002/07/owl#").append("\n");
        obda.append("rdf:").append("\t").append("http://www.w3.org/1999/02/22-rdf-syntax-ns#").append("\n");
        obda.append("rdfs:").append("\t").append("http://www.w3.org/2000/01/rdf-schema#").append("\n");
        obda.append("xsd:").append("\t").append("http://www.w3.org/2001/XMLSchema#").append("\n");
        obda.append("data:").append("\t").append("http://www.semanticweb.org/sports/data#").append("\n");
        obda.append("abox:").append("\t").append("http://www.semanticweb.org/sports/abox#").append("\n");
        obda.append("\n");
        
        // Source declaration - contains DB connection info, not JAR paths
        obda.append("[SourceDeclaration]\n");
        obda.append("connectionUrl\t").append(dbConfig.getDbConnectionURL()).append("\n");
        obda.append("username\t").append(dbConfig.getDbUserName()).append("\n");
        obda.append("password\t").append(dbConfig.getDbPassword()).append("\n");
        obda.append("driverClass\t").append(dbConfig.getDbDriverClassName()).append("\n");
        obda.append("\n");
        
        return obda.toString();
    }
    
    /**
     * Converts R2RML TTL mappings to OBDA mapping format using general converter
     * Works with any R2RML TTL file without hardcoded mappings
     */
    private static String convertR2RMLToOBDA() {
        try {
            // Find the R2RML mapping file
            String projectRoot = System.getProperty("user.dir");
            Path r2rmlPath = Paths.get(projectRoot, "src", "main", "resources", "ontology", AppConfig.MAPPING_FILE_NAME);
            
            if (!Files.exists(r2rmlPath)) {
                System.out.println("   ! R2RML mapping file not found: " + AppConfig.MAPPING_FILE_NAME);
                return "";
            }
            
            // Use general TTL to OBDA converter
            GeneralTtlToObdaConverter converter = new GeneralTtlToObdaConverter(
                r2rmlPath,
                AppConfig.getOntologyNamespace(),
                AppConfig.getDataNamespace()
            );
            
            String obdaMappings = converter.convertToObda();
            
            System.out.println("   → [GENERAL] Converted R2RML mappings to OBDA format using general converter");
            System.out.println("   → [GENERAL] Includes " + countMappings(obdaMappings) + " mapping declarations");
            System.out.println("   → [GENERAL] Extracted from: " + AppConfig.MAPPING_FILE_NAME);
            
            return obdaMappings;
            
        } catch (Exception e) {
            System.out.println("   ! Warning: Could not convert R2RML mappings with general converter: " + e.getMessage());
            System.out.println("   ! Falling back to sport-specific converter...");
            
            // Fallback to sport-specific converter
            return SportSpecificObdaConverter.convertR2RMLToOBDA();
        }
    }
    
    /**
     * Helper method to count the number of mappings in the OBDA string
     */
    private static int countMappings(String obdaContent) {
        return obdaContent.split("mappingId\\s").length - 1;
    }
    

    
    /**
     * Validates if the OBDA file contains valid connection information
     */
    public static boolean validateObdaFile(Path obdaPath) {
        try {
            if (!Files.exists(obdaPath)) {
                return false;
            }
            
            String content = Files.readString(obdaPath);
            return content.contains("[SourceDeclaration]") && 
                   content.contains("connectionUrl") &&
                   content.contains("driverClass");
                   
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Extracts database connection URL from an existing OBDA file
     */
    public static String extractConnectionUrl(Path obdaPath) throws IOException {
        String content = Files.readString(obdaPath);
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (line.trim().startsWith("connectionUrl")) {
                return line.substring(line.indexOf('\t') + 1).trim();
            }
        }
        
        throw new IllegalArgumentException("No connectionUrl found in OBDA file");
    }
    
    /**
     * Attempts to copy H2 JAR to common Protégé plugin directories for automatic driver loading
     * This reduces the need for manual driver configuration in Preferences
     */
    public static void attemptProtegePluginInstallation(String h2JarPath) {
        System.out.println("   → Attempting automatic Protégé plugin installation...");
        
        // Common Protégé installation directories
        String[] commonProtegePaths = {
            "C:\\Program Files\\Protege",
            "C:\\Program Files (x86)\\Protege", 
            System.getProperty("user.home") + "\\AppData\\Local\\Protege",
            System.getProperty("user.home") + "\\Applications\\Protege",
            "/Applications/Protege.app",
            "/opt/protege",
            "/usr/local/protege"
        };
        
        boolean installed = false;
        for (String protegePath : commonProtegePaths) {
            Path protegeDir = Paths.get(protegePath);
            Path pluginsDir = protegeDir.resolve("plugins");
            
            if (Files.exists(pluginsDir)) {
                try {
                    Path targetJar = pluginsDir.resolve("h2-2.4.240.jar");
                    Files.copy(Paths.get(h2JarPath), targetJar, 
                              java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("   ✓ H2 JAR installed to: " + pluginsDir);
                    installed = true;
                    break;
                } catch (Exception e) {
                    // Continue trying other locations
                }
            }
        }
        
        if (!installed) {
            System.out.println("   ! Protégé installation not found - manual setup required");
            System.out.println("   ! RECOMMENDED: Copy database/h2-2.4.240.jar to your Protégé plugins/ folder");
            System.out.println("   ! This makes the OBDA file work automatically without configuration");
        }
    }
}