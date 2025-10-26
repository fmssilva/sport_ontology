package protege_files;

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
        buildProtegeFiles(outputDir);
    }
    
    public static void buildProtegeFiles(String outputDir) {
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
            DatabaseConfig dbConfig = BuildH2Database.buildH2Setup(outputPath.toAbsolutePath().toString());
            
            // Step 2: Copy ontology files directly
            System.out.println("\n>> Step 2: Copying ontology files...");
            copyOntologyFiles(ontologyPath);
            
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
        
        // Copy main ontology files
        Files.copy(
            sourceOntology.resolve("sport-ontology.owl"),
            ontologyPath.resolve("sport-ontology.owl"),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        Files.copy(
            sourceOntology.resolve("sport-ontology-mapping.ttl"),
            ontologyPath.resolve("sport-ontology-mapping.ttl"),
            StandardCopyOption.REPLACE_EXISTING
        );
        
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
    
    private static void generateProtegeSetup(Path outputPath, DatabaseConfig dbConfig) throws IOException {
        StringBuilder content = new StringBuilder();
        
        content.append("# Protege Setup Guide - Sport Ontology\n\n");
        content.append("*Complete steps to load the sport ontology in Protege with H2 database*\n\n");
        
        content.append("## Prerequisites\n");
        content.append("- Protege 5.5+ installed\n");
        content.append("- H2 JDBC driver (included in database/ folder)\n\n");
               
        content.append("## Step 1: Load Ontology\n");
        content.append("1. Open Protege\n");
        content.append("2. **File -> Open**\n");
        content.append("3. Select: `ontology/sport-ontology.owl`\n");
        content.append("4. Verify 40+ classes are loaded\n\n");
        
        content.append("## Step 2: Start HermiT Reasoner\n");
        content.append("1. **Reasoner -> HermiT**\n");
        content.append("2. **Click \"Start reasoner\"**\n");
        content.append("3. Wait for \"Consistent\" status » check the log button on the right bottom corner\n\n");
        
        content.append("## Step 3: Connect to H2 DB\n");
        // Add database configuration sections from dbConfig with proper formatting
        content.append("a: Set the H2 JAR location to the absolute path of h2-2.4.240.jar file\n");
        content.append(dbConfig.getPreferencesSteps().replace("\\n", "\n") + "\n");
        content.append("####\n");
        content.append("b: Set the connection to the H2 database file using the following details:\n");
        content.append(dbConfig.getConnectionSteps().replace("\\n", "\n") + "\n");
        
        content.append("#### Step 3.1: Using H2 as a built-in Protege backend or shared setup\n");
        content.append("If you want Protege to use H2 as an internal or shared database backend (not just as an external connection):\n");
        content.append("a. Copy the file `database/h2-2.4.240.jar` into your Protege `plugins` folder.\n");
        content.append("b. Restart Protege completely to load the new driver.\n\n");

        content.append("## Step 4: Load OBDA Mappings\n");
        content.append("1. In **Ontop top tab** click **import R2RML mapping**\n");
        content.append("2. Select: `ontology/sport-ontology-mapping.ttl`\n");
        content.append("3. Verify R2RML mappings are loaded\n\n");
        content.append("4. Click the **Validate** button to check the mapping validity.\n");
        content.append("5. Open some mappings and execute the given sample queries to test their functionality.\n");

        content.append("## Step 5: Confirm Ontology + Mapping + H2 DB correctness\n");
        content.append("1. Run Ontop Reasoner by clicking **Reasoner -> Ontop Reasoner -> Start Reasoner**\n");
        content.append("2. Ensure no errors occur during reasoning - check the log for any issues\n");
        content.append("3. Click in **ontop top tab** in **check consistency** button to verify if the seeded data in H2 DB is consistent for disjoint and functional properties\n");

        content.append("## Step 6: Test SPARQL Queries\n");
        content.append("1. On **ontop SPARQL tab** write the queries to test the ontology\n");
        content.append("2. Copy queries from the **queries folder** into the SPARQL tab and execute them\n");
                
        Files.write(outputPath.resolve("PROTEGE_SET_UP.md"), content.toString().getBytes());
        System.out.println("   PROTEGE_SET_UP.md created with system-specific paths");
    }
}