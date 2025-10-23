package sparql;

import integration.TestRegistry;
import tests.categories.TestCase;
import integration.TestResult;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * SPARQL Tester - Real Ontop CLI connection
 */
public class SPARQLTester {
    

    
    public static List<TestResult> runTests() {
        System.out.println("Running SPARQL Tests (via Ontop CLI)...");
        List<TestResult> results = new ArrayList<>();
        
        // Get all test cases and run SPARQL queries
        for (TestCase testCase : TestRegistry.getIntegrityTests()) {
            TestResult result = executeTest(testCase);
            results.add(result);
            result.display();
        }
        
        return results;
    }
    
    private static TestResult executeTest(TestCase testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.printf("    -> Executing SPARQL via Ontop: %s%n", testCase.name);
            
            int actual = executeOntopQuery(testCase.sparqlQuery);
            long executionTime = System.currentTimeMillis() - startTime;
            
            return new TestResult(testCase.testId, testCase.name, "SPARQL", testCase.expectedResult, actual, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            e.printStackTrace();
            return new TestResult(testCase.testId, testCase.name, "SPARQL", testCase.expectedResult, "ERROR: " + e.getMessage(), executionTime);
        }
    }
    
    private static int executeOntopQuery(String sparqlQuery) throws Exception {
        // Get paths (avoid duplicate declarations)
        String userDir = System.getProperty("user.dir");
        String projectRoot = new File(userDir).getParent();
        
        // Create temporary query file in project root
        Path queryFile = Paths.get(projectRoot).resolve("temp_query.sparql");
        Path outputFile = Paths.get(projectRoot).resolve("temp_results.csv");
        
        try {
            // Write SPARQL query to file
            Files.write(queryFile, sparqlQuery.getBytes());
            
            // Build cross-platform ontop command
            String ontopScript = getOntopScript(projectRoot);
            String dbPath = projectRoot.replace("\\", "/") + "/database/sports-db";
            String dbUrl = "jdbc:h2:" + dbPath + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
            
            ProcessBuilder pb = new ProcessBuilder(
                ontopScript, "query",
                "--ontology", "ontology/sport-ontology.owl",
                "--mapping", "mappings/sport-ontology-mapping.ttl", 
                "--db-driver", "org.h2.Driver",
                "--db-url", dbUrl,
                "--db-user", "sa",
                "--query", queryFile.getFileName().toString(),
                "--output", outputFile.getFileName().toString()
            );
            
            // Set working directory to project root
            pb.directory(new File(projectRoot));
            
            // Execute process
            Process process = pb.start();
            
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Read error stream
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // Wait for process completion
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                String errorMsg = errorOutput.toString().trim();
                if (errorMsg.isEmpty()) {
                    errorMsg = "No error details captured";
                }
                throw new Exception("Ontop CLI failed with exit code " + exitCode + ". Error: " + errorMsg);
            }
            
            // Read results from the clean CSV output file (separate from logging)
            if (!Files.exists(outputFile)) {
                throw new Exception("Ontop CLI completed but output file was not created: " + outputFile);
            }
            
            String csvContent = new String(Files.readAllBytes(outputFile));
            return parseCountFromCSV(csvContent);
            
        } finally {
            // Clean up temporary files
            Files.deleteIfExists(queryFile);
            Files.deleteIfExists(outputFile);
        }
    }
    
    private static int parseCountFromCSV(String csvContent) {
        // Parse clean CSV content (no logging mixed in)
        String[] lines = csvContent.trim().split("\n");
        
        if (lines.length == 0) {
            throw new RuntimeException("Empty CSV output");
        }
        
        // CSV format: header line "count" followed by the actual count value
        // Skip header and get the data line
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and headers
            if (line.isEmpty() || line.equals("count")) {
                continue;
            }
            
            // Remove quotes if present and parse as integer
            line = line.replaceAll("\"", "");
            
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                // Continue to next line
                continue;
            }
        }
        
        throw new RuntimeException("Could not parse count from CSV content: " + csvContent);
    }
    
    /**
     * Get the appropriate Ontop script for the current platform
     */
    private static String getOntopScript(String projectRoot) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return projectRoot + File.separator + "tools" + File.separator + "ontop" + File.separator + "ontop.bat";
        } else {
            return projectRoot + File.separator + "tools" + File.separator + "ontop" + File.separator + "ontop";
        }
    }
}