package integration;

import sql.SQLTester;
import sparql.SPARQLTester;
import java.util.*;

/**
 * Integration Tester - Orchestrates SQL and SPARQL tests
 */
public class IntegrationTester {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(50));
        System.out.println("OBDA Integration Test Suite");
        System.out.println("=".repeat(50));
        
        // Print debug info if requested
        if (args.length > 0 && "--debug".equals(args[0])) {
            System.out.println("Debug mode enabled");
            System.out.println("OS: " + System.getProperty("os.name"));
            System.out.println("Working Directory: " + System.getProperty("user.dir"));
            System.out.println();
        }
        
        // Run SQL tests
        List<TestResult> sqlResults = SQLTester.runTests();
        
        System.out.println();
        
        // Run SPARQL tests
        List<TestResult> sparqlResults = SPARQLTester.runTests();
        
        // Compare results
        compareResults(sqlResults, sparqlResults);
        
        // Summary
        displaySummary(sqlResults, sparqlResults);
    }
    
    private static void compareResults(List<TestResult> sqlResults, List<TestResult> sparqlResults) {
        System.out.println("\nCross-Validation");
        System.out.println("-".repeat(50));
        System.out.printf("%-15s | %6s | %6s | %s%n", "Test", "SQL", "SPARQL", "Match");
        System.out.println("-".repeat(50));
        
        for (int i = 0; i < sqlResults.size() && i < sparqlResults.size(); i++) {
            TestResult sqlResult = sqlResults.get(i);
            TestResult sparqlResult = sparqlResults.get(i);
            
            boolean match = sqlResult.passed && sparqlResult.passed && 
                          sqlResult.actual == sparqlResult.actual;
            
            System.out.printf("%-15s | %6s | %6s | %s%n",
                sqlResult.testName,
                sqlResult.passed ? "YES" : "NO",
                sparqlResult.passed ? "YES" : "NO",
                match ? "YES" : "NO");
        }
    }
    
    private static void displaySummary(List<TestResult> sqlResults, List<TestResult> sparqlResults) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(50));
        
        long sqlPassed = sqlResults.stream().mapToLong(r -> r.passed ? 1 : 0).sum();
        long sparqlPassed = sparqlResults.stream().mapToLong(r -> r.passed ? 1 : 0).sum();
        
        System.out.printf("SQL Tests:    %d PASSED, %d FAILED%n", sqlPassed, sqlResults.size() - sqlPassed);
        System.out.printf("SPARQL Tests: %d PASSED, %d FAILED%n", sparqlPassed, sparqlResults.size() - sparqlPassed);
        
        boolean allPassed = sqlPassed == sqlResults.size() && sparqlPassed == sparqlResults.size();
        System.out.printf("STATUS: %s%n", allPassed ? "ALL TESTS PASSED" : "!!!! SOME TESTS FAILED");
    }
}