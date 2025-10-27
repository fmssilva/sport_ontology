package engines;

import config.AppConfig;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * SPARQL Engine - Manages Ontop CLI execution for SPARQL queries.
 * Handles the full OBDA stack: SPARQL → Ontop → R2RML → SQL → H2.
 */
public class SPARQLEngine {
    private H2_SQLEngine dbEngine;
    private String ontologyPath;
    private String mappingPath; 
    private String propertiesPath;
    private boolean isSetup = false;
    
    public SPARQLEngine(H2_SQLEngine dbEngine) {
        this.dbEngine = dbEngine;
        this.ontologyPath = AppConfig.getOntologyPath();
        this.mappingPath = AppConfig.getMappingPath();
        this.propertiesPath = AppConfig.getPropertiesPath();
    }
    
    /**
     * Setup SPARQL engine - validates dependencies and creates properties file.
     */
    public void setup() throws Exception {
        if (isSetup) {
            System.out.println("SPARQL engine already setup");
            return;
        }
        
        System.out.println("Setting up SPARQL Engine...");
        
        if (!dbEngine.isStarted()) {
            throw new IllegalStateException("SQLEngine must be started before SPARQLEngine setup");
        }
        
        validateFiles();
        createPropertiesFile();
        
        isSetup = true;
        System.out.println("SPARQL Engine setup completed");
        System.out.println("Ontology: " + ontologyPath);
        System.out.println("Mappings: " + mappingPath);
        System.out.println("Properties: " + propertiesPath);
    }
    
    /**
     * Execute SPARQL query using Ontop CLI.
     */
    public List<String> executeSPARQL(String sparqlQuery) throws Exception {
        if (!isSetup) {
            setup();
        }
        
        List<String> results = new ArrayList<>();
        Path tempQueryFile = null;
        Path tempResultFile = null;
        
        try {
            System.out.println("Executing SPARQL Query via Ontop CLI:");
            System.out.println("Query: " + sparqlQuery.replaceAll("\\s+", " ").trim());
            
            tempQueryFile = Files.createTempFile("sparql_query_", ".sparql");
            tempResultFile = Files.createTempFile("sparql_result_", ".csv");
            
            Files.write(tempQueryFile, sparqlQuery.getBytes());
            
            String ontopCommand = buildOntopCommand(tempQueryFile, tempResultFile);
            
            List<String> command = new ArrayList<>();
            command.add("cmd");
            command.add("/c");
            command.add(ontopCommand);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            StringBuilder processOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processOutput.append(line).append("\n");
                }
            }
            
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (finished && process.exitValue() == 0) {
                System.out.println("Process output: " + processOutput.toString().trim());
                
                if (Files.exists(tempResultFile)) {
                    results = Files.readAllLines(tempResultFile);
                    System.out.println("Query executed successfully (" + results.size() + " lines)");
                } else {
                    System.out.println("Query completed but no result file generated");
                }
            } else {
                throw new RuntimeException("Ontop CLI execution failed. Process output: " + processOutput.toString());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("OBDA stack failure: " + e.getMessage());
            
        } finally {
            if (tempQueryFile != null) {
                try { Files.deleteIfExists(tempQueryFile); } catch (Exception e) { }
            }
            if (tempResultFile != null) {
                try { Files.deleteIfExists(tempResultFile); } catch (Exception e) { }
            }
        }
        
        return results;
    }
    
    /**
     * Run SPARQL test queries.
     */
    public void runTestQueries() throws Exception {
        System.out.println("Running SPARQL Test Queries...");
        
        String[] testQueries = {
            AppConfig.getSPARQLPrefix() + "SELECT (COUNT(?team) AS ?count) WHERE { ?team a :Team }",
            AppConfig.getSPARQLPrefix() + "SELECT (COUNT(?player) AS ?count) WHERE { ?player a :Player }",
            AppConfig.getSPARQLPrefix() + "SELECT (COUNT(?coach) AS ?count) WHERE { ?coach a :Coach }",
            AppConfig.getSPARQLPrefix() + "SELECT (COUNT(?forward) AS ?count) WHERE { ?forward a :Forward }"
        };
        
        String[] testNames = {
            "Count Teams", "Count Players", "Count Coaches", "Count Forwards"
        };
        
        for (int i = 0; i < testQueries.length; i++) {
            System.out.println("Test " + (i + 1) + ": " + testNames[i]);
            try {
                List<String> results = executeSPARQL(testQueries[i]);
                
                if (!results.isEmpty()) {
                    System.out.println("Results:");
                    results.stream().limit(5).forEach(line -> System.out.println(line));
                } else {
                    System.out.println("No results returned");
                }
                
            } catch (Exception e) {
                System.err.println("Test failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Validate required ontology files exist.
     */
    private void validateFiles() throws Exception {
        String[] requiredFiles = {ontologyPath, mappingPath};
        
        for (String filePath : requiredFiles) {
            if (!Files.exists(Paths.get(filePath))) {
                throw new FileNotFoundException("Required file not found: " + filePath);
            }
        }
        
        System.out.println("Required ontology files validated");
    }
    
    /**
     * Create properties file with current database connection.
     */
    private void createPropertiesFile() throws IOException {
        Properties props = new Properties();
        props.setProperty("jdbc.url", dbEngine.getDatabaseUrl());
        props.setProperty("jdbc.driver", "org.h2.Driver");
        props.setProperty("jdbc.user", "sa");
        props.setProperty("jdbc.password", "");
        
        Path propsPath = Paths.get(propertiesPath);
        Files.createDirectories(propsPath.getParent());
        
        try (FileWriter writer = new FileWriter(propsPath.toFile())) {
            props.store(writer, "Generated by SPARQLEngine");
        }
        
        System.out.println("Properties file created/updated");
    }
    
    /**
     * Build Ontop CLI command.
     */
    private String buildOntopCommand(Path queryFile, Path resultFile) {
        String ontopPath = findOntopCLI();
        if (ontopPath == null) {
            throw new RuntimeException("Ontop CLI not found. Please install Ontop CLI.");
        }
        
        StringBuilder cmd = new StringBuilder();
        cmd.append(ontopPath)
           .append(" query")
           .append(" --ontology \"").append(ontologyPath).append("\"")
           .append(" --mapping \"").append(mappingPath).append("\"")
           .append(" --properties \"").append(propertiesPath).append("\"")
           .append(" --query \"").append(queryFile.toString()).append("\"")
           .append(" --output \"").append(resultFile.toString()).append("\"");
        
        System.out.println("Full OBDA Command: " + cmd.toString());
        return cmd.toString();
    }
    
    /**
     * Find Ontop CLI executable.
     */
    private String findOntopCLI() {
        String[] possiblePaths = {
            "tools\\ontop\\ontop.bat",
            "tools/ontop/ontop",
            "tools\\ontop.bat",
            "tools/ontop",
            "ontop",
            "ontop.bat"
        };
        
        for (String path : possiblePaths) {
            try {
                ProcessBuilder testPb = new ProcessBuilder(path, "--version");
                testPb.redirectErrorStream(true);
                Process testProcess = testPb.start();
                
                boolean finished = testProcess.waitFor(5, TimeUnit.SECONDS);
                if (finished && testProcess.exitValue() == 0) {
                    System.out.println("Found Ontop CLI at: " + path);
                    return path;
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        return null;
    }
    
    /**
     * Check if engine is setup.
     */
    public boolean isSetup() {
        return isSetup;
    }
    
    /**
     * Cleanup resources.
     */
    public void cleanup() {
        System.out.println("SPARQL Engine cleanup completed");
    }
}
