package engines;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * SPARQL Engine - Manages Ontop CLI execution for SPARQL queries
 * Handles the full OBDA stack: SPARQL → Ontop → R2RML → SQL → H2
 */
public class SPARQLEngine {
    private H2_SQLEngine dbEngine;
    private String ontologyPath;
    private String mappingPath; 
    private String propertiesPath;
    private boolean isSetup = false;
    
    public SPARQLEngine(H2_SQLEngine dbEngine) {
        this.dbEngine = dbEngine;
        this.ontologyPath = "src/main/resources/ontology/sport-ontology.owl";
        this.mappingPath = "src/main/resources/ontology/sport-ontology-mapping.ttl";
        this.propertiesPath = "src/main/resources/ontology/sport-ontology-simple.properties";
    }
    
    /**
     * Setup SPARQL engine - validates dependencies and creates properties file
     */
    public void setup() throws Exception {
        if (isSetup) {
            System.out.println("WARNING: SPARQL engine already setup");
            return;
        }
        
        System.out.println("🔄 Setting up SPARQL Engine...");
        
        // Validate database engine is started
        if (!dbEngine.isStarted()) {
            throw new IllegalStateException("SQLEngine must be started before SPARQLEngine setup");
        }
        
        // Validate required files exist
        validateFiles();
        
        // Create/update properties file with current database URL
        createPropertiesFile();
        
        isSetup = true;
        System.out.println("SPARQL Engine setup completed");
        System.out.println("   Ontology: " + ontologyPath);
        System.out.println("   Mappings: " + mappingPath);
        System.out.println("   Properties: " + propertiesPath);
    }
    
    /**
     * Execute SPARQL query using Ontop CLI
     * This tests the full OBDA pipeline exactly like Protégé + Ontop
     */
    public List<String> executeSPARQL(String sparqlQuery) throws Exception {
        if (!isSetup) {
            setup();
        }
        
        List<String> results = new ArrayList<>();
        Path tempQueryFile = null;
        Path tempResultFile = null;
        
        try {
            System.out.println("🔍 Executing SPARQL Query via Ontop CLI:");
            System.out.println("   Query: " + sparqlQuery.replaceAll("\\s+", " ").trim());
            
            // Create temporary files
            tempQueryFile = Files.createTempFile("sparql_query_", ".sparql");
            tempResultFile = Files.createTempFile("sparql_result_", ".csv");
            
            // Write query to file
            Files.write(tempQueryFile, sparqlQuery.getBytes());
            
            // Build Ontop CLI command 
            String ontopCommand = buildOntopCommand(tempQueryFile, tempResultFile);
            
            // Execute command using proper command parsing
            List<String> command = new ArrayList<>();
            command.add("cmd");
            command.add("/c");
            command.add(ontopCommand);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // Read process output for debugging
            StringBuilder processOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processOutput.append(line).append("\\n");
                }
            }
            
            // Wait for completion
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (finished && process.exitValue() == 0) {
                System.out.println("   🔍 Process output: " + processOutput.toString().trim());
                
                // Read results from output file
                if (Files.exists(tempResultFile)) {
                    results = Files.readAllLines(tempResultFile);
                    System.out.println("   Query executed successfully (" + results.size() + " lines)");
                    System.out.println("   Result file: " + tempResultFile.toString());
                    if (results.size() > 0) {
                        System.out.println("   First result line: " + results.get(0));
                        if (results.size() > 1) {
                            System.out.println("   Second result line: " + results.get(1));
                        }
                    }
                } else {
                    System.out.println("   WARNING: Query completed but no result file generated at: " + tempResultFile.toString());
                }
            } else {
                String errorMsg = "ERROR: ONTOP CLI EXECUTION FAILED!\n" +
                    "   Process output: " + processOutput.toString() + "\n" +
                    "   Exit code: " + process.exitValue() + "\n" +
                    "   This means the full OBDA stack is NOT working.";
                System.err.println(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (Exception e) {
            String errorMsg = "ERROR: FULL OBDA STACK FAILURE: " + e.getMessage() + "\n" +
                "   The SPARQL → Ontop → R2RML → SQL → H2 pipeline is broken.";
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg);
            
        } finally {
            // Cleanup temporary files
            if (tempQueryFile != null) {
                try { Files.deleteIfExists(tempQueryFile); } catch (Exception e) { /* ignore */ }
            }
            if (tempResultFile != null) {
                try { Files.deleteIfExists(tempResultFile); } catch (Exception e) { /* ignore */ }
            }
        }
        
        return results;
    }
    
    /**
     * Run comprehensive SPARQL tests (your existing test queries)
     */
    public void runTestQueries() throws Exception {
        System.out.println("\\n🔍 Running SPARQL Test Queries (Full OBDA Stack)...");
        
        String[] testQueries = {
            // Basic entity count
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?team) AS ?count) WHERE { ?team a :Team }",
            
            // Player count  
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?player) AS ?count) WHERE { ?player a :Player }",
            
            // Coach count
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?coach) AS ?count) WHERE { ?coach a :Coach }",
            
            // Forward count
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?forward) AS ?count) WHERE { ?forward a :Forward }"
        };
        
        String[] testNames = {
            "Count Teams", "Count Players", "Count Coaches", "Count Forwards"
        };
        
        for (int i = 0; i < testQueries.length; i++) {
            System.out.println("\\n🔸 Test " + (i + 1) + ": " + testNames[i]);
            try {
                List<String> results = executeSPARQL(testQueries[i]);
                
                if (!results.isEmpty()) {
                    System.out.println("   📋 Results:");
                    results.stream().limit(5).forEach(line -> 
                        System.out.println("      " + line));
                } else {
                    System.out.println("   📋 No results returned");
                }
                
            } catch (Exception e) {
                System.err.println("   ❌ Test failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Validate required ontology files exist
     */
    private void validateFiles() throws Exception {
        String[] requiredFiles = {ontologyPath, mappingPath};
        
        for (String filePath : requiredFiles) {
            if (!Files.exists(Paths.get(filePath))) {
                throw new FileNotFoundException("Required file not found: " + filePath);
            }
        }
        
        System.out.println("   ✅ Required ontology files validated");
    }
    
    /**
     * Create properties file with current database connection
     */
    private void createPropertiesFile() throws IOException {
        Properties props = new Properties();
        props.setProperty("jdbc.url", dbEngine.getDatabaseUrl());
        props.setProperty("jdbc.driver", "org.h2.Driver");
        props.setProperty("jdbc.user", "sa");
        props.setProperty("jdbc.password", "");
        
        // Ensure directory exists
        Path propsPath = Paths.get(propertiesPath);
        Files.createDirectories(propsPath.getParent());
        
        // Write properties file
        try (FileWriter writer = new FileWriter(propsPath.toFile())) {
            props.store(writer, "Generated by SPARQLEngine for current session");
        }
        
        System.out.println("   ✅ Properties file created/updated");
    }
    
    /**
     * Build Ontop CLI command for real OBDA execution
     */
    private String buildOntopCommand(Path queryFile, Path resultFile) {
        // Check if Ontop CLI is available
        String ontopPath = findOntopCLI();
        if (ontopPath == null) {
            throw new RuntimeException("❌ ONTOP CLI NOT FOUND! Cannot execute full OBDA stack.\n" +
                "   Please install Ontop CLI and ensure it's in PATH or in project tools/ directory.\n" +
                "   Download from: https://github.com/ontop/ontop/releases");
        }
        
        // Build actual Ontop CLI command
        StringBuilder cmd = new StringBuilder();
        cmd.append(ontopPath)
           .append(" query")
           .append(" --ontology \"").append(ontologyPath).append("\"")
           .append(" --mapping \"").append(mappingPath).append("\"")
           .append(" --properties \"").append(propertiesPath).append("\"")
           .append(" --query \"").append(queryFile.toString()).append("\"")
           .append(" --output \"").append(resultFile.toString()).append("\"");
        
        System.out.println("🔍 Full OBDA Command: " + cmd.toString());
        return cmd.toString();
    }
    
    /**
     * Find Ontop CLI executable in common locations
     */
    private String findOntopCLI() {
        // Common locations to check for Ontop CLI (Project-managed first)
        String[] possiblePaths = {
            // Project-managed Ontop CLI (highest priority)
            System.getProperty("user.dir") + "\\tools\\ontop.bat", // Windows absolute
            System.getProperty("user.dir") + "/tools/ontop",       // Unix absolute
            "tools\\ontop.bat",                                     // Windows relative
            "tools/ontop",                                          // Unix relative
            
            // System PATH (user installed)
            "ontop",                           // In PATH
            "ontop.bat",                       // Windows batch in PATH
            
            // Manual project installations
            "./tools/ontop/ontop",             // Project local Unix
            "./tools/ontop/ontop.bat",         // Project local Windows
            "../tools/ontop/ontop",            // Parent directory Unix
            "../tools/ontop/ontop.bat",        // Parent directory Windows
            
            // Common system installations
            "C:\\Program Files\\ontop\\ontop.bat",  // Common Windows install
            "/usr/local/bin/ontop",                 // Common Unix install
            "/opt/ontop/ontop"                      // Alternative Unix install
        };
        
        for (String path : possiblePaths) {
            try {
                // Test if the command is available
                ProcessBuilder testPb = new ProcessBuilder(path, "--version");
                testPb.redirectErrorStream(true);
                Process testProcess = testPb.start();
                
                boolean finished = testProcess.waitFor(5, TimeUnit.SECONDS);
                if (finished && testProcess.exitValue() == 0) {
                    System.out.println("✅ Found Ontop CLI at: " + path);
                    return path;
                }
            } catch (Exception e) {
                // Continue to next path
                continue;
            }
        }
        
        return null; // Not found
    }
    
    /**
     * Check if engine is setup
     */
    public boolean isSetup() {
        return isSetup;
    }
    
    /**
     * Cleanup resources (currently no persistent resources to clean)
     */
    public void cleanup() {
        System.out.println("🔄 SPARQL Engine cleanup completed");
    }
}