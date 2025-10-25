package utils;

import engines.H2_SQLEngine;
import engines.SPARQLEngine;
import engines.ReasoningEngine;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Centralized test execution engine
 * Handles SQL, SPARQL, and reasoning test execution with proper result tracking
 */
public class TestExecutor {
    private H2_SQLEngine sqlEngine;
    private SPARQLEngine sparqlEngine;
    
    public TestExecutor(H2_SQLEngine sqlEngine, SPARQLEngine sparqlEngine) {
        this.sqlEngine = sqlEngine;
        this.sparqlEngine = sparqlEngine;
    }
    
    /**
     * Execute a complete test case (SQL + SPARQL + optional reasoning)
     */
    public List<TestResult> executeTestCase(TestCase testCase) {
        List<TestResult> results = new ArrayList<>();
        
        // Execute SQL test
        TestResult sqlResult = executeSQLTest(testCase);
        results.add(sqlResult);
        
        // Execute SPARQL test
        TestResult sparqlResult = executeSPARQLTest(testCase);
        results.add(sparqlResult);
        
        // Execute reasoning test if expected
        if (testCase.hasReasoningExpectation()) {
            TestResult reasoningResult = executeReasoningTest(testCase);
            results.add(reasoningResult);
        }
        
        return results;
    }
    
    /**
     * Execute SQL test
     */
    public TestResult executeSQLTest(TestCase testCase) {
        long startTime = System.currentTimeMillis();

        try {
            ResultSet rs = sqlEngine.executeQuery(testCase.sqlQuery);
            
            // For COUNT queries, extract the count value from the first column
            int actualResult = 0;
            if (rs.next()) {
                // Check if this is a COUNT query by looking for COUNT in the first column value or metadata
                Object firstValue = rs.getObject(1);
                if (firstValue instanceof Number) {
                    actualResult = ((Number) firstValue).intValue();
                    System.out.printf("  Row 1: %s=%s%n", rs.getMetaData().getColumnName(1), firstValue);
                } else {
                    // Fallback: count rows for non-COUNT queries
                    actualResult = 1;
                    StringBuilder row = new StringBuilder("Row 1: ");
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= Math.min(columnCount, 3); i++) {
                        if (i > 1) row.append(", ");
                        row.append(rs.getMetaData().getColumnName(i))
                           .append("=")
                           .append(rs.getString(i));
                    }
                    System.out.println("  " + row.toString());
                    
                    // Continue counting additional rows
                    while (rs.next()) {
                        actualResult++;
                        if (actualResult <= 3) {
                            row = new StringBuilder("Row " + actualResult + ": ");
                            for (int i = 1; i <= Math.min(columnCount, 3); i++) {
                                if (i > 1) row.append(", ");
                                row.append(rs.getMetaData().getColumnName(i))
                                   .append("=")
                                   .append(rs.getString(i));
                            }
                            System.out.println("  " + row.toString());
                        }
                    }
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("  SQL returned " + actualResult + " rows");
            
            return new TestResult(testCase.testId, testCase.name, "SQL", 
                testCase.expectedSQLResult, actualResult, executionTime, testCase.worldAssumption);
                
        } catch (Exception e) {
            return new TestResult(testCase.testId, testCase.name, "SQL", 
                testCase.expectedSQLResult, e.getMessage(), 
                System.currentTimeMillis() - startTime, testCase.worldAssumption);
        }
    }    /**
     * Execute SPARQL test  
     */
    public TestResult executeSPARQLTest(TestCase testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<String> results = sparqlEngine.executeSPARQL(testCase.sparqlQuery);
            
            // Parse SPARQL results (look for count value)
            int actualResult = parseSPARQLCount(results);
            long executionTime = System.currentTimeMillis() - startTime;
            
            return new TestResult(testCase.testId, testCase.name, "SPARQL", 
                testCase.expectedSPARQLResult, actualResult, executionTime, testCase.worldAssumption);
                
        } catch (Exception e) {
            return new TestResult(testCase.testId, testCase.name, "SPARQL", 
                testCase.expectedSPARQLResult, e.getMessage(), 
                System.currentTimeMillis() - startTime, testCase.worldAssumption);
        }
    }
    
    /**
     * Execute reasoning test with improved intersection handling
     */
    public TestResult executeReasoningTest(TestCase testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Initialize reasoning engine
            ReasoningEngine reasoningEngine = new ReasoningEngine();
            reasoningEngine.setup();
            reasoningEngine.addABoxData();
            
            int actualResult;
            
            // Handle intersection cases specially
            if (testCase.name.contains("top_young_player")) {
                // Count individuals that are BOTH TopPlayer AND YoungPlayer
                System.out.println("   Computing TopPlayer ∩ YoungPlayer intersection:");
                actualResult = reasoningEngine.countIndividualsOfBothClasses("TopPlayer", "YoungPlayer");
            } else {
                // Extract class name from test case for single class counting
                String className = extractClassNameFromReasoningTest(testCase);
                actualResult = reasoningEngine.countIndividualsOfClass(className);
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            reasoningEngine.cleanup();
            
            return new TestResult(testCase.testId, testCase.name, "REASONING", 
                testCase.expectedReasoningResult, actualResult, executionTime, "OWA");
                
        } catch (Exception e) {
            return new TestResult(testCase.testId, testCase.name, "REASONING", 
                testCase.expectedReasoningResult, e.getMessage(), 
                System.currentTimeMillis() - startTime, "OWA");
        }
    }
    
    /**
     * Extract class name from reasoning test for counting
     */
    private String extractClassNameFromReasoningTest(TestCase testCase) {
        // Map test names to class names for reasoning
        if (testCase.name.contains("top_player")) {
            return "TopPlayer";
        } else if (testCase.name.contains("young_player")) {
            return "YoungPlayer";
        } else if (testCase.name.contains("experienced_player")) {
            return "ExperiencedPlayer";
        } else if (testCase.name.contains("top_coach")) {
            return "TopCoach";
        }
        
        // Default fallback
        return "Player";
    }
    
    /**
     * Parse SPARQL results to extract count value or count data rows (excluding header)
     */
    private int parseSPARQLCount(List<String> results) {
        if (results == null || results.isEmpty()) {
            return 0;
        }
        
        boolean foundHeader = false;
        String headerLine = null;
        String firstDataLine = null;
        
        for (String line : results) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Capture header line (first non-empty line)
            if (!foundHeader) {
                foundHeader = true;
                headerLine = line;
                System.out.println("  SPARQL Header: " + line);
                continue;
            }
            
            // Capture first data line
            if (firstDataLine == null) {
                firstDataLine = line;
                System.out.println("  SPARQL Row 1: " + line);
                
                // Check if this is a COUNT query (header contains 'count')
                if (headerLine != null && headerLine.toLowerCase().contains("count")) {
                    try {
                        // Try to parse the count value directly
                        String countValue = line.trim();
                        // Handle potential decimal format
                        if (countValue.contains(".")) {
                            countValue = countValue.substring(0, countValue.indexOf('.'));
                        }
                        int count = Integer.parseInt(countValue);
                        System.out.println("  SPARQL returned count value: " + count);
                        return count;
                    } catch (NumberFormatException e) {
                        System.out.println("  Warning: Could not parse count value, falling back to row counting");
                    }
                }
            }
        }
        
        // Fallback: count data rows for non-COUNT queries
        int rowCount = 0;
        foundHeader = false;
        
        for (String line : results) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Skip header line
            if (!foundHeader) {
                foundHeader = true;
                continue;
            }
            
            // Count data rows
            rowCount++;
            
            // For debugging: print first few results
            if (rowCount <= 3) {
                System.out.println("  SPARQL Row " + rowCount + ": " + line);
            }
        }
        
        System.out.println("  SPARQL returned " + rowCount + " data rows");
        return rowCount;
    }
}