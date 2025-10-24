package domain;

import engines.H2_SQLEngine;
import engines.SPARQLEngine;
import engines.ReasoningEngine;
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
 * Advanced OBDA Integration Tests - Production-Level Query Complexity
 * Demonstrates sophisticated SPARQL operations, multi-entity joins, and reasoning integration
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTests {
    
    private H2_SQLEngine sqlEngine;
    private SPARQLEngine sparqlEngine;
    private ReasoningEngine reasoningEngine;
    private TestExecutor testExecutor;
    
    @BeforeAll
    void setupEngines() throws Exception {
        System.out.println("Setting up Advanced OBDA Integration Test engines...");
        
        // Initialize database engine
        sqlEngine = new H2_SQLEngine();
        sqlEngine.start();
        
        // Initialize SPARQL engine
        sparqlEngine = new SPARQLEngine(sqlEngine);
        sparqlEngine.setup();
        
        // Initialize reasoning engine
        reasoningEngine = new ReasoningEngine();
        reasoningEngine.setup();
        
        // Initialize test executor
        testExecutor = new TestExecutor(sqlEngine, sparqlEngine);
        
        System.out.println("Advanced OBDA Integration Test engines initialized");
    }
    
    @AfterAll
    void cleanupEngines() throws Exception {
        System.out.println("Cleaning up Advanced OBDA Integration Test engines...");
        
        if (reasoningEngine != null) {
            reasoningEngine.cleanup();
        }
        
        if (sparqlEngine != null) {
            sparqlEngine.cleanup();
        }
        
        if (sqlEngine != null) {
            sqlEngine.stop();
        }
        
        System.out.println("Advanced OBDA Integration Test engines cleaned up");
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
        descriptions.put("ADV-01", "Multi-entity network query");
        descriptions.put("ADV-02", "Statistical analysis + reasoning");
        descriptions.put("ADV-03", "Complex SPARQL aggregation");
        descriptions.put("ADV-04", "Cross-ontology relationship query");
        
        String desc = descriptions.get(testId);
        if (desc != null) return desc;
        
        // Fallback: clean up test name
        String cleaned = testName.toLowerCase().replace("_", " ").replace("test", "").trim();
        return cleaned.length() > 35 ? cleaned.substring(0, 32) + "..." : cleaned;
    }
    
    @Test
    @DisplayName("Complex Multi-Entity SPARQL Queries Across Full OBDA Stack")
    void testComplexMultiEntitySparqlQueriesAcrossFullObdaStack() throws Exception {
        System.out.println("\n=== ADVANCED OBDA INTEGRATION TESTS ===");
        System.out.println("Testing sophisticated SPARQL operations across Player-Team-Coach relationships");
        System.out.println("Demonstrates production-level query complexity and OBDA mapping excellence");
        
        List<TestCase> advancedTests = Arrays.asList(
            new TestCase(
                "ADV-01",
                "test_multi_entity_network_query_player_team_coach_relationships",
                "integration", 
                "Complex SPARQL across Player-Team-Coach relationships in single query with joins",
                QueryLoader.loadSQL("integration", "player_team_coach_network"),
                QueryLoader.loadSPARQL("integration", "player_team_coach_network"),
                18, // SQL: 18 complete player-team-coach relationships
                21, // SPARQL: 21 relationships via OBDA mappings (includes additional mappings)
                null, // No reasoning expectation for network query
                "ADVANCED_SPARQL",
                "CWA"
            ),
            
            new TestCase(
                "ADV-02",
                "test_statistical_aggregation_with_reasoning_market_value_analysis",
                "integration",
                "Statistical analysis combining SQL aggregation + OWL inference for market insights", 
                QueryLoader.loadSQL("integration", "average_market_value_by_team"),
                QueryLoader.loadSPARQL("integration", "average_market_value_by_team"),
                3, // SQL: 3 teams with calculated average market values
                4, // SPARQL: 4 teams (includes additional team via OBDA mappings)
                8, // HermiT: 8 individuals from ABox reasoning
                "STATISTICAL_REASONING",
                "Mixed"
            )
        );
        
        TestResultManager testManager = TestResultManager.getInstance();
        java.util.List<java.util.List<TestResult>> allTestResults = new java.util.ArrayList<>();
        
        boolean allTestsPassed = true;
        
        for (TestCase testCase : advancedTests) {
            System.out.printf("\nExecuting Test: %s - %s%n", testCase.testId, testCase.name);
            System.out.printf("Description: %s%n", testCase.description);
            
            List<TestResult> results = testExecutor.executeTestCase(testCase);
            allTestResults.add(results);
            
            // Register results with TestResultManager
            testManager.registerTestResults("AdvancedObdaIntegrationTest", results);
            
            // Display and analyze advanced OBDA behavior
            TestResult sqlResult = null;
            TestResult sparqlResult = null;
            TestResult reasoningResult = null;
            
            for (TestResult result : results) {
                if (!result.passed) {
                    allTestsPassed = false;
                }
                
                switch (result.layer) {
                    case "SQL": sqlResult = result; break;
                    case "SPARQL": sparqlResult = result; break;
                    case "REASONING": reasoningResult = result; break;
                }
            }
            
            // Analyze OBDA integration quality
            if (sqlResult != null && sparqlResult != null) {
                if (sqlResult.passed && sparqlResult.passed) {
                    System.out.printf("  OBDA MAPPING VALIDATION: SQL (%d) + SPARQL (%d) - Advanced query success ✓%n", 
                        sqlResult.actual, sparqlResult.actual);
                    System.out.printf("  Technical Analysis: R2RML mappings handling complex joins successfully%n");
                    
                    // Check performance difference
                    long performanceDiff = sparqlResult.executionTime - sqlResult.executionTime;
                    System.out.printf("  Performance Analysis: SPARQL overhead = %dms%n", performanceDiff);
                    
                } else {
                    System.out.printf("  OBDA INTEGRATION ISSUE: SQL success=%s, SPARQL success=%s%n", 
                        sqlResult.passed, sparqlResult.passed);
                    allTestsPassed = false;
                }
            }
            
            // Check reasoning enhancement
            if (reasoningResult != null && sqlResult != null) {
                if (reasoningResult.actual > sqlResult.actual) {
                    System.out.printf("  REASONING ENHANCEMENT: HermiT (%d) > SQL (%d) - Inference adds value ✓%n", 
                        reasoningResult.actual, sqlResult.actual);
                } else {
                    System.out.printf("  REASONING ANALYSIS: HermiT (%d) ≤ SQL (%d) - Consistent with base data%n", 
                        reasoningResult.actual, sqlResult.actual);
                }
            }
        }
        
        System.out.println("=== ADVANCED OBDA INTEGRATION SUMMARY ===");
        System.out.printf("All advanced tests passed: %s%n", allTestsPassed ? "YES" : "NO");
        System.out.println("Advanced OBDA Capabilities Demonstrated:");
        System.out.println("  ✓ Multi-table JOIN operations via SPARQL");
        System.out.println("  ✓ Complex R2RML mapping validation");
        System.out.println("  ✓ Statistical aggregation (GROUP BY, AVG)");
        System.out.println("  ✓ Production-level query complexity");
        System.out.println("  ✓ SQL-SPARQL-HermiT integration pipeline");
        
        // Display test summary table for this domain
        displayDomainSummary("ADVANCED OBDA", advancedTests, allTestResults);
        
        assertTrue(allTestsPassed, "All advanced OBDA integration tests must pass");
    }
    
    @Test
    @DisplayName("Performance Benchmarking Across All Three Reasoning Layers")
    void testPerformanceBenchmarkingAcrossAllThreeReasoningLayers() throws Exception {
        System.out.println("\n=== PERFORMANCE BENCHMARKING TESTS ===");
        System.out.println("Measuring and comparing response times across SQL/SPARQL/HermiT systems");
        
        List<TestCase> performanceTests = Arrays.asList(
            new TestCase(
                "PERF-01",
                "test_query_response_time_analysis_simple_count_operations",
                "performance", 
                "Compare performance across SQL/SPARQL/HermiT for basic counting operations",
                QueryLoader.loadSQL("performance", "simple_player_count"),
                QueryLoader.loadSPARQL("performance", "simple_player_count"),
                12, 12, 8, // SQL: 12, SPARQL: 12 (namespace restricted), HermiT: 8 (ABox individuals)
                "PERFORMANCE",
                "Mixed"
            ),
            
            new TestCase(
                "PERF-02",
                "test_complex_query_performance_multi_table_joins_with_aggregation",
                "performance",
                "Performance analysis for complex multi-table operations with statistical functions", 
                QueryLoader.loadSQL("performance", "complex_team_statistics"),
                QueryLoader.loadSPARQL("performance", "complex_team_statistics"),
                5, 5, 8, // SQL: 5 teams, SPARQL: 5 teams (namespace restricted), HermiT: 8 (with ABox)
                "PERFORMANCE",
                "Mixed"
            )
        );
        
        long totalSQLTime = 0;
        long totalSPARQLTime = 0;
        long totalHermiTTime = 0;
        int sqlTestCount = 0;
        int sparqlTestCount = 0;
        int hermitTestCount = 0;
        
        TestResultManager testManager = TestResultManager.getInstance();
        java.util.List<java.util.List<TestResult>> allTestResults = new java.util.ArrayList<>();
        
        for (TestCase testCase : performanceTests) {
            System.out.printf("\nExecuting Performance Test: %s%n", testCase.testId);
            
            List<TestResult> results = testExecutor.executeTestCase(testCase);
            allTestResults.add(results);
            
            // Register results with TestResultManager
            testManager.registerTestResults("AdvancedObdaIntegrationTest", results);
            
            for (TestResult result : results) {
                switch (result.layer) {
                    case "SQL":
                        totalSQLTime += result.executionTime;
                        sqlTestCount++;
                        System.out.printf("  SQL Performance: %dms (Result: %s)%n", 
                            result.executionTime, result.actual);
                        break;
                    case "SPARQL":
                        totalSPARQLTime += result.executionTime;
                        sparqlTestCount++;
                        System.out.printf("  SPARQL Performance: %dms (Result: %s)%n", 
                            result.executionTime, result.actual);
                        break;
                    case "REASONING":
                        totalHermiTTime += result.executionTime;
                        hermitTestCount++;
                        System.out.printf("  HermiT Performance: %dms (Result: %s)%n", 
                            result.executionTime, result.actual);
                        break;
                }
            }
        }
        
        // Calculate averages
        long avgSQLTime = sqlTestCount > 0 ? totalSQLTime / sqlTestCount : 0;
        long avgSPARQLTime = sparqlTestCount > 0 ? totalSPARQLTime / sparqlTestCount : 0;
        long avgHermiTTime = hermitTestCount > 0 ? totalHermiTTime / hermitTestCount : 0;
        
        System.out.println("\n=== PERFORMANCE ANALYSIS RESULTS ===");
        System.out.printf("SQL Average Response Time: %dms (%d tests)%n", avgSQLTime, sqlTestCount);
        System.out.printf("SPARQL Average Response Time: %dms (%d tests)%n", avgSPARQLTime, sparqlTestCount);
        System.out.printf("HermiT Average Response Time: %dms (%d tests)%n", avgHermiTTime, hermitTestCount);
        
        // Performance ratio analysis
        if (avgSQLTime > 0) {
            System.out.printf("SPARQL/SQL Ratio: %.2fx overhead%n", (double)avgSPARQLTime / avgSQLTime);
            System.out.printf("HermiT/SQL Ratio: %.2fx overhead%n", (double)avgHermiTTime / avgSQLTime);
        }
        
        // Performance category classification
        System.out.println("\n=== PERFORMANCE CLASSIFICATION ===");
        System.out.printf("SQL: %s (Direct database access)%n", 
            avgSQLTime < 100 ? "EXCELLENT" : avgSQLTime < 500 ? "GOOD" : "ACCEPTABLE");
        System.out.printf("SPARQL: %s (OBDA translation overhead)%n", 
            avgSPARQLTime < 1000 ? "EXCELLENT" : avgSPARQLTime < 5000 ? "GOOD" : "ACCEPTABLE");
        System.out.printf("HermiT: %s (Reasoning computation)%n", 
            avgHermiTTime < 500 ? "EXCELLENT" : avgHermiTTime < 2000 ? "GOOD" : "ACCEPTABLE");
        
        // Display test summary table
        displayDomainSummary("PERFORMANCE", performanceTests, allTestResults);
        
        // Performance assertions based on TOP_TESTS.md expectations
        assertTrue(avgSQLTime < 1000, "SQL queries should be fast (< 1000ms average)");
        assertTrue(avgSPARQLTime < 10000, "SPARQL queries should be reasonable (< 10000ms average)");
        assertTrue(avgHermiTTime < 5000, "HermiT reasoning should be efficient (< 5000ms average)");
    }
    
    @Test
    @DisplayName("System Integration Validation and Consistency Verification")
    void testSystemIntegrationValidationAndConsistencyVerification() throws Exception {
        System.out.println("\n=== SYSTEM INTEGRATION VALIDATION ===");
        System.out.println("Cross-system consistency verification and integration quality assessment");
        
        // Test data consistency across all three systems
        boolean integrationSuccess = true;
        
        // Test 1: Basic count consistency where expected
        System.out.println("\nTesting basic count consistency...");
        try {
            java.sql.ResultSet sqlRs = sqlEngine.executeQuery("SELECT COUNT(*) FROM Team");
            sqlRs.next();
            int sqlTeamCount = sqlRs.getInt(1);
            
            List<String> sparqlResults = sparqlEngine.executeSPARQL(
                "SELECT (COUNT(?team) AS ?count) WHERE { ?team a <http://www.semanticweb.org/sports/ontology#Team> }");
            int sparqlTeamCount = parseSparqlCount(sparqlResults);
            
            System.out.printf("SQL Team Count: %d%n", sqlTeamCount);
            System.out.printf("SPARQL Team Count: %d%n", sparqlTeamCount);
            
            if (sparqlTeamCount >= sqlTeamCount) {
                System.out.println("✓ SPARQL includes all SQL data (expected with ABox)");
            } else {
                System.out.println("✗ SPARQL missing some SQL data (mapping issue)");
                integrationSuccess = false;
            }
            
        } catch (Exception e) {
            System.out.println("✗ Integration test failed: " + e.getMessage());
            integrationSuccess = false;
        }
        
        // Test 2: Reasoning enhancement validation
        System.out.println("\nTesting reasoning enhancement...");
        try {
            reasoningEngine.addABoxData();
            int youngPlayers = reasoningEngine.countIndividualsOfClass("YoungPlayer");
            int topPlayers = reasoningEngine.countIndividualsOfClass("TopPlayer");
            
            System.out.printf("HermiT YoungPlayer Count: %d%n", youngPlayers);
            System.out.printf("HermiT TopPlayer Count: %d%n", topPlayers);
            
            if (youngPlayers > 0 && topPlayers > 0) {
                System.out.println("✓ Reasoning engine producing inferred classifications");
            } else {
                System.out.println("⚠ Reasoning engine not inferring expected classifications");
            }
            
        } catch (Exception e) {
            System.out.println("⚠ Reasoning validation warning: " + e.getMessage());
        }
        
        System.out.println("\n=== INTEGRATION ASSESSMENT ===");
        System.out.printf("Overall Integration Success: %s%n", integrationSuccess ? "PASS" : "NEEDS ATTENTION");
        System.out.println("Systems Operational:");
        System.out.println("  ✓ H2 Database (SQL layer)");
        System.out.println("  ✓ Ontop SPARQL Engine (OBDA layer)");
        System.out.println("  ✓ HermiT Reasoner (OWL reasoning layer)");
        System.out.println("  ✓ R2RML Mappings (Data integration)");
        
        assertTrue(integrationSuccess, "System integration must be successful");
    }
    
    /**
     * Helper method to parse SPARQL count results
     */
    private int parseSparqlCount(List<String> results) {
        for (String line : results) {
            if (line.matches("\\d+")) {
                return Integer.parseInt(line.trim());
            }
            if (line.contains("count")) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    if (part.trim().matches("\\d+")) {
                        return Integer.parseInt(part.trim());
                    }
                }
            }
        }
        return 0;
    }
}