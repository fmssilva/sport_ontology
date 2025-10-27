package protege_files;

import config.AppConfig;
import java.io.*;
import java.nio.file.*;

/**
 * Clean and modular Protege files builder for Sport Ontology project.
 * Delegates specialized tasks to focused builder classes.
 * 
 * Usage: java BuildProtegeFiles [output-directory]
 * Default output: ./protege_files/
 */
public class BuildProtegeFiles {
    
    private static final String PROJECT_BASE = System.getProperty("user.dir");
    
    public static void main(String[] args) {
        String outputDir = args.length > 0 ? args[0] : "protege_files";
        // Always include complete OBDA mappings
        buildProtegeFiles(outputDir, true);
    }
    
    public static void buildProtegeFiles(String outputDir) {
        buildProtegeFiles(outputDir, true);
    }
    
    public static void buildProtegeFiles(String outputDir, boolean includeMappings) {
        try {
            System.out.println(">> Building complete Protege files for Sport Ontology...");
            System.out.println(">> Output directory: " + Paths.get(outputDir).toAbsolutePath());
            
            // Create main folder structure
            Path outputPath = Paths.get(outputDir);
            Path databasePath = outputPath.resolve("database");
            Path ontologyPath = outputPath.resolve("ontology");
            Path queriesPath = outputPath.resolve("queries");
            Path sparqlPath = queriesPath.resolve("sparql");
            Path sqlPath = queriesPath.resolve("sql");
            
            // Create all directories
            Files.createDirectories(databasePath);
            Files.createDirectories(ontologyPath);
            Files.createDirectories(sparqlPath);
            Files.createDirectories(sqlPath);
            
            // Step 1: Build H2 Database setup (delegated to specialized class)
            System.out.println("\n>> Step 1: Building H2 Database setup...");
            Build_H2_DB_config_file dbConfig = Build_H2_DB_files.buildH2Setup(outputPath.toAbsolutePath().toString());
            
            // Step 2: Copy ontology files and generate OBDA
            System.out.println("\n>> Step 2: Copying ontology files and generating OBDA...");
            copyOntologyFiles(ontologyPath);
            
            // Generate complete OBDA file with R2RML mappings
            ObdaFileGenerator.generateObdaFile(outputPath.toAbsolutePath().toString(), dbConfig);
            
            // Attempt automatic Protégé plugin installation for easier H2 driver setup
            String h2JarPath = Paths.get(outputPath.toAbsolutePath().toString(), "database", "h2-2.4.240.jar").toString();
            ObdaFileGenerator.attemptProtegePluginInstallation(h2JarPath);
            
            // Step 3: Copy query files directly  
            System.out.println("\n>> Step 3: Organizing query files...");
            copyQueriesFolder(sparqlPath, sqlPath);
            
            // Step 4: Generate main PROTEGE_SET_UP file
            System.out.println("\n>> Step 4: Generating PROTEGE_SET_UP guide...");
            generateProtegeSetup(outputPath, dbConfig);
            
            System.out.println("\n>> Protege files build completed successfully!");
            System.out.println(">> Location: " + outputPath.toAbsolutePath());
            System.out.println(">> Files ready for Protege and report submission");
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to build protege files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void copyOntologyFiles(Path ontologyPath) throws IOException {
        Path sourceOntology = Paths.get(PROJECT_BASE, "src", "main", "resources", "ontology");
        
        // Copy main ontology files using centralized AppConfig naming
        Files.copy(
            sourceOntology.resolve(AppConfig.ONTOLOGY_FILE_NAME),
            ontologyPath.resolve(AppConfig.ONTOLOGY_FILE_NAME),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        Files.copy(
            sourceOntology.resolve(AppConfig.MAPPING_FILE_NAME),
            ontologyPath.resolve(AppConfig.MAPPING_FILE_NAME),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        // Copy OBDA file if it exists
        Path obdaSource = sourceOntology.resolve(AppConfig.OBDA_FILE_NAME);
        if (Files.exists(obdaSource)) {
            Files.copy(
                obdaSource,
                ontologyPath.resolve(AppConfig.OBDA_FILE_NAME),
                StandardCopyOption.REPLACE_EXISTING
            );
            System.out.println("   OBDA file copied successfully");
        }
        
        System.out.println("   Ontology files copied successfully");
    }
    
    private static void copyQueriesFolder(Path sparqlPath, Path sqlPath) throws IOException {
        Path queriesDir = Paths.get(PROJECT_BASE, "src", "test", "resources", "queries");
        
        if (!Files.exists(queriesDir)) {
            System.out.println("   WARNING: Queries directory not found, creating empty folders");
            return;
        }
        
        // Copy all SPARQL query files to sparql folder
        try (var paths = Files.walk(queriesDir)) {
            paths.filter(path -> path.toString().endsWith(".sparql"))
                 .sorted()
                 .forEach(queryFile -> {
                     try {
                         String fileName = queryFile.getFileName().toString();
                         Path targetFile = sparqlPath.resolve(fileName);
                         Files.copy(queryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                         System.out.println("   Copied SPARQL file: " + fileName);
                     } catch (IOException e) {
                         System.err.println("   Warning: Could not copy " + queryFile + ": " + e.getMessage());
                     }
                 });
        }
        
        // Copy all SQL files to sql folder
        try (var paths = Files.walk(queriesDir)) {
            paths.filter(path -> path.toString().endsWith(".sql"))
                 .sorted()
                 .forEach(queryFile -> {
                     try {
                         String fileName = queryFile.getFileName().toString();
                         Path targetFile = sqlPath.resolve(fileName);
                         Files.copy(queryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                         System.out.println("   Copied SQL file: " + fileName);
                     } catch (IOException e) {
                         System.err.println("   Warning: Could not copy " + queryFile + ": " + e.getMessage());
                     }
                 });
        }
        
        System.out.println("   All query files organized into sparql/ and sql/ folders");
    }
    
    private static void generateProtegeSetup(Path outputPath, Build_H2_DB_config_file dbConfig) throws IOException {
        // Copy PROTEGE_SET_UP.md from project root to protege_files folder
        Path sourceSetupFile = Paths.get(PROJECT_BASE, "PROTEGE_SET_UP.md");
        Path targetSetupFile = outputPath.resolve("PROTEGE_SET_UP.md");
        
        if (Files.exists(sourceSetupFile)) {
            Files.copy(sourceSetupFile, targetSetupFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("   PROTEGE_SET_UP.md copied successfully from project root");
        } else {
            System.err.println("   WARNING: PROTEGE_SET_UP.md not found in project root");
        }
    }
}