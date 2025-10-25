package domain;

import engines.H2_SQLEngine;
import engines.SPARQLEngine;
import utils.TestCase;
import utils.TestExecutor;
import utils.TestResult;
import utils.TestResultManager;
import utils.QueryLoader;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Arrays;

/**
 * Data Integrity Tests - Foundation Layer
 * Validates SQL <-> SPARQL consistency for basic OBDA operations
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrityTests {
    
    private H2_SQLEngine sqlEngine;
    private SPARQLEngine sparqlEngine;
    private TestExecutor testExecutor;
    
    @BeforeAll
    void setupEngines() throws Exception {
        System.out.println("Setting up Data Integrity Test engines...");
        
        // Initialize database engine
        sqlEngine = new H2_SQLEngine();
        sqlEngine.start();
        
        // Initialize SPARQL engine
        sparqlEngine = new SPARQLEngine(sqlEngine);
        sparqlEngine.setup();
        
        // Initialize test executor
        testExecutor = new TestExecutor(sqlEngine, sparqlEngine);
        
        System.out.println("Data Integrity Test engines initialized");
    }
    
    @AfterAll
    void cleanupEngines() throws Exception {
        System.out.println("Cleaning up Data Integrity Test engines...");
        
        if (sparqlEngine != null) {
            sparqlEngine.cleanup();
        }
        
        if (sqlEngine != null) {
            sqlEngine.stop();
        }
        
        System.out.println("Data Integrity Test engines cleaned up");
    }
    
    /**
     * Display test summary table for this domain
     */
    private void displayDomainSummary(String domain, java.util.List<TestCase> testCases, java.util.List<java.util.List<TestResult>> allResults) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST RESULTS SUMMARY - " + domain + " DOMAIN");
        System.out.println("=" + "=".repeat(80));
        System.out.printf("%-8s %-35s %-8s %-8s %-8s%n", "TEST ID", "DESCRIPTION", "SQL", "SPARQL", "HERMIT");
        System.out.println("-".repeat(80));
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            java.util.List<TestResult> results = allResults.get(i);
            
            String sql = "---";
            String sparql = "---";
            String hermit = "---";
            
            // Count tests by layer for this testId and build result string
            int sqlPassed = 0, sqlTotal = 0, sparqlPassed = 0, sparqlTotal = 0, hermitPassed = 0, hermitTotal = 0;
            for (TestResult result : results) {
                switch (result.layer) {
                    case "SQL": 
                        sqlTotal++; 
                        if (result.passed) sqlPassed++; 
                        break;
                    case "SPARQL": 
                        sparqlTotal++; 
                        if (result.passed) sparqlPassed++; 
                        break;
                    case "REASONING": 
                        hermitTotal++; 
                        if (result.passed) hermitPassed++; 
                        break;
                }
            }
            
            sql = sqlTotal > 0 ? String.format("%d/%d", sqlPassed, sqlTotal) : "---";
            sparql = sparqlTotal > 0 ? String.format("%d/%d", sparqlPassed, sparqlTotal) : "---";
            hermit = hermitTotal > 0 ? String.format("%d/%d", hermitPassed, hermitTotal) : "---";
            
            String description = getShortDescription(testCase.testId, testCase.name);
            System.out.printf("%-8s %-35s %-8s %-8s %-8s%n", testCase.testId, description, sql, sparql, hermit);
        }
        
        System.out.println("=" + "=".repeat(80));
    }
    
    private String getShortDescription(String testId, String testName) {
        java.util.Map<String, String> descriptions = new java.util.HashMap<>();
        descriptions.put("INT-01", "Teams count consistency");
        descriptions.put("INT-02", "Players count consistency");
        descriptions.put("INT-03", "Coaches count consistency");
        descriptions.put("INT-04", "Database performance test");
        
        String desc = descriptions.get(testId);
        if (desc != null) return desc;
        
        // Fallback: clean up test name
        String cleaned = testName.toLowerCase().replace("_", " ").replace("test", "").trim();
        return cleaned.length() > 35 ? cleaned.substring(0, 32) + "..." : cleaned;
    }
    
    @Test
    @DisplayName("Foundation Tests: SQL <-> SPARQL Consistency")
    void testDataIntegrity() throws Exception {
        System.out.println("\n=== DATA INTEGRITY TESTS ===");
        System.out.println("Testing SQL <-> SPARQL consistency for basic OBDA validation");
        
        // Fixed test cases using CreateH2Database seed data
        List<TestCase> integrityTests = Arrays.asList(
            new TestCase(
                "INT-01",
                "total_teams",
                "integrity", 
                "Validate total team count consistency between SQL and SPARQL",
                QueryLoader.loadSQL("integrity", "count_all_teams"),
                QueryLoader.loadSPARQL("integrity", "count_all_teams"),
                7, // SQL: 7 teams from CreateH2Database (5 senior + 2 youth)
                7, // SPARQL: 7 teams (same as SQL, restricted to H2 data: namespace only)
                null, // No reasoning expectation
                "SQL<->SPARQL",
                "CWA"
            ),
            
            new TestCase(
                "INT-02",
                "total_players",
                "integrity",
                "Validate player count consistency between SQL and SPARQL", 
                QueryLoader.loadSQL("integrity", "count_unique_players"),
                QueryLoader.loadSPARQL("integrity", "count_unique_players"),
                12, // SQL: 12 unique players from CreateH2Database
                12, // SPARQL: 12 players (same as SQL, restricted to H2 data: namespace only)
                null, // No reasoning expectation
                "SQL<->SPARQL",
                "CWA"
            )
        );
        TestResultManager testManager = TestResultManager.getInstance();
        java.util.List<java.util.List<TestResult>> allTestResults = new java.util.ArrayList<>();
        
        boolean allTestsPassed = true;
        
        for (TestCase testCase : integrityTests) {
            System.out.printf("\nExecuting Test: %s - %s%n", testCase.testId, testCase.name);
            System.out.printf("Description: %s%n", testCase.description);
            
            List<TestResult> results = testExecutor.executeTestCase(testCase);
            allTestResults.add(results);
            
            // Register results with TestResultManager
            testManager.registerTestResults("DataIntegrityTest", results);
            
            // Display results
            for (TestResult result : results) {
                if (!result.passed) {
                    allTestsPassed = false;
                }
            }
            
            // Validate SQL and SPARQL layers work correctly (with different expected values due to ABox data)
            if (results.size() >= 2) {
                TestResult sqlResult = results.get(0);
                TestResult sparqlResult = results.get(1);
                
                if (sqlResult.passed && sparqlResult.passed) {
                    System.out.printf("  INTEGRITY CHECK: SQL (%d) + SPARQL (%d) both working - PASS%n", 
                        sqlResult.actual, sparqlResult.actual);
                    System.out.printf("  Architecture: SQL (database) + SPARQL (database + ABox individuals) = Complete OBDA%n");
                } else {
                    System.out.printf("  INTEGRITY CHECK: FAILED - One or both layers failed%n");
                    allTestsPassed = false;
                }
            }
        }
        
        System.out.println("=== DATA INTEGRITY SUMMARY ===");
        System.out.printf("All tests passed: %s%n", allTestsPassed ? "YES" : "NO");
        
        // Display test summary table for this domain
        displayDomainSummary("INTEGRITY", integrityTests, allTestResults);
        
        assertTrue(allTestsPassed, "All data integrity tests must pass");
    }
    
    @Test
    @DisplayName("Performance Benchmarks")
    void testPerformance() throws Exception {
        System.out.println("\n=== PERFORMANCE BENCHMARKS ===");
        
        // Fixed test cases using CreateH2Database seed data
        List<TestCase> integrityTests = Arrays.asList(
            new TestCase(
                "PERF-01",
                "team_count_performance",
                "integrity", 
                "Performance test: Team count query",
                QueryLoader.loadSQL("integrity", "count_all_teams"),
                QueryLoader.loadSPARQL("integrity", "count_all_teams"),
                7, 11, null, "Performance", "CWA"
            ),
            
            new TestCase(
                "PERF-02", 
                "player_count_performance",
                "integrity",
                "Performance test: Player count query",
                QueryLoader.loadSQL("integrity", "count_unique_players"),
                QueryLoader.loadSPARQL("integrity", "count_unique_players"),
                12, 20, null, "Performance", "CWA"
            )
        );
        
        long totalSQLTime = 0;
        long totalSPARQLTime = 0;
        int sqlTestCount = 0;
        int sparqlTestCount = 0;
        
        for (TestCase testCase : integrityTests) {
            List<TestResult> results = testExecutor.executeTestCase(testCase);
            
            for (TestResult result : results) {
                if ("SQL".equals(result.layer)) {
                    totalSQLTime += result.executionTime;
                    sqlTestCount++;
                } else if ("SPARQL".equals(result.layer)) {
                    totalSPARQLTime += result.executionTime;
                    sparqlTestCount++;
                }
            }
        }
        
        long avgSQLTime = sqlTestCount > 0 ? totalSQLTime / sqlTestCount : 0;
        long avgSPARQLTime = sparqlTestCount > 0 ? totalSPARQLTime / sparqlTestCount : 0;
        
        System.out.printf("SQL Average: %dms (%d tests)%n", avgSQLTime, sqlTestCount);
        System.out.printf("SPARQL Average: %dms (%d tests)%n", avgSPARQLTime, sparqlTestCount);
        
        // Performance assertions
        assertTrue(avgSQLTime < 1000, "SQL queries should be fast (< 1000ms average)");
        assertTrue(avgSPARQLTime < 10000, "SPARQL queries should be reasonable (< 10000ms average)");
    }
}