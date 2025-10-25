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
import java.util.Set;
import java.util.Arrays;

/**
 * Advanced Reasoning Tests - Core OWL 2 Inference Capabilities
 * Demonstrates automatic classification and complex reasoning axioms
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReasoningTests {
    
    private H2_SQLEngine sqlEngine;
    private SPARQLEngine sparqlEngine;
    private ReasoningEngine reasoningEngine;
    private TestExecutor testExecutor;
    
    @BeforeAll
    void setupEngines() throws Exception {
        System.out.println("Setting up Advanced Reasoning Test engines...");
        
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
        
        System.out.println("Advanced Reasoning Test engines initialized");
    }
    
    @AfterAll
    void cleanupEngines() throws Exception {
        System.out.println("Cleaning up Advanced Reasoning Test engines...");
        
        if (reasoningEngine != null) {
            reasoningEngine.cleanup();
        }
        
        if (sparqlEngine != null) {
            sparqlEngine.cleanup();
        }
        
        if (sqlEngine != null) {
            sqlEngine.stop();
        }
        
        System.out.println("Advanced Reasoning Test engines cleaned up");
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
        descriptions.put("REA-01", "Young Player auto-classification");
        descriptions.put("REA-02", "Top Player market value inference");
        descriptions.put("REA-03", "Multiple inheritance reasoning");
        descriptions.put("REA-04", "Elite Team complex reasoning");
        
        String desc = descriptions.get(testId);
        if (desc != null) return desc;
        
        // Fallback: clean up test name
        String cleaned = testName.toLowerCase().replace("_", " ").replace("test", "").trim();
        return cleaned.length() > 35 ? cleaned.substring(0, 32) + "..." : cleaned;
    }
    
    @Test
    @DisplayName("Advanced OWL 2 Reasoning: Automatic Player Classification Tests")
    void testAdvancedOwl2ReasoningForPlayerClassification() throws Exception {
        System.out.println("\n=== ADVANCED REASONING TESTS ===");
        System.out.println("Testing OWL 2 automatic classification and complex reasoning axioms");
        
        // Priority reasoning tests based on TOP_TESTS.md
        List<TestCase> reasoningTests = Arrays.asList(
            new TestCase(
                "REA-01",
                "test_automatic_young_player_classification_by_age_threshold",
                "reasoning", 
                "Demonstrate automatic classification via age-based reasoning: YoungPlayer ≡ Player ⊓ ∃hasAge.<23",
                QueryLoader.loadSQL("reasoning", "young_players_by_age"),
                QueryLoader.loadSPARQL("reasoning", "young_players_by_age"),
                3, // SQL: 3 players (Rico Lewis: 19, Nico Paz: 20, Jude Bellingham: 21)
                3, // SPARQL: 3 (H2 database access via Ontop OBDA)
                3, // HermiT: 3 (automatic inference)
                "OWL_REASONING",
                "OWA"
            ),
            
            new TestCase(
                "REA-02",
                "test_automatic_top_player_classification_by_market_value",
                "reasoning",
                "Show complex market value-based classification: TopPlayer ≡ Player ⊓ ∃hasMarketValue.≥100M", 
                QueryLoader.loadSQL("reasoning", "top_players_by_value"),
                QueryLoader.loadSPARQL("reasoning", "top_players_by_value"),
                5, // SQL: 5 players (Haaland, Vinicius, Bellingham, Mbappe, Kane: ≥100M)
                5, // SPARQL: 5 (H2 database access via Ontop OBDA)
                5, // HermiT: 5 (automatic inference)
                "OWL_REASONING",
                "OWA"
            ),
            
            new TestCase(
                "REA-03",
                "test_multiple_inheritance_top_young_player_intersection",
                "reasoning",
                "Demonstrate players belonging to multiple inferred classes: TopPlayer AND YoungPlayer", 
                QueryLoader.loadSQL("reasoning", "top_young_players"),
                QueryLoader.loadSPARQL("reasoning", "top_young_players"),
                1, // SQL: 1 player (Jude Bellingham: 180M + age 21)
                1, // SPARQL: 1 (H2 database access via Ontop OBDA)
                1, // HermiT: 1 (dual classification)
                "OWL_REASONING",
                "OWA"
            )
        );
        
        TestResultManager testManager = TestResultManager.getInstance();
        java.util.List<java.util.List<TestResult>> allTestResults = new java.util.ArrayList<>();
        
        boolean allTestsPassed = true;
        
        for (TestCase testCase : reasoningTests) {
            System.out.printf("\nExecuting Test: %s - %s%n", testCase.testId, testCase.name);
            System.out.printf("Description: %s%n", testCase.description);
            
            List<TestResult> results = testExecutor.executeTestCase(testCase);
            allTestResults.add(results);
            
            // Register results with TestResultManager
            testManager.registerTestResults("ReasoningTest", results);
            
            // Display results and validate reasoning behavior
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
            
            // Validate OWL reasoning behavior: SQL should work, SPARQL should be 0, HermiT should infer
            if (sqlResult != null && reasoningResult != null) {
                if (sqlResult.passed && reasoningResult.passed && sqlResult.actual == reasoningResult.actual) {
                    System.out.printf("  REASONING VALIDATION: SQL (%d) = HermiT (%d) - Perfect inference! ✓%n", 
                        sqlResult.actual, reasoningResult.actual);
                    System.out.printf("  OWL Axiom Working: Automatic classification successful%n");
                } else {
                    System.out.printf("  REASONING ISSUE: SQL (%d) ≠ HermiT (%d) - Inference mismatch%n", 
                        sqlResult.actual, reasoningResult.actual);
                    // Note: This might still be acceptable if the logic is correct
                }
            }
            
            // Show SPARQL behavior (accesses H2 database via Ontop OBDA)
            if (sparqlResult != null) {
                System.out.printf("  SPARQL (H2 via OBDA): %d - Expected behavior for H2 database access%n", 
                    sparqlResult.actual);
            }
        }
        
        System.out.println("=== REASONING SUMMARY ===");
        System.out.printf("All tests passed: %s%n", allTestsPassed ? "YES" : "NO");
        System.out.println("Reasoning Capabilities Demonstrated:");
        System.out.println("  ✓ Automatic classification via EquivalentClasses");
        System.out.println("  ✓ Data property-based reasoning (age, market value)");
        System.out.println("  ✓ Multiple inheritance and class intersections");
        System.out.println("  ✓ SQL-HermiT consistency validation");
        
        // Display test summary table for this domain
        displayDomainSummary("REASONING", reasoningTests, allTestResults);
        
        assertTrue(allTestsPassed, "All advanced reasoning tests must pass");
    }
    
    @Test
    @DisplayName("Individual Player Classification Verification Using HermiT Reasoner")
    void testIndividualPlayerClassificationWithHermitReasoner() throws Exception {
        System.out.println("\n=== INDIVIDUAL CLASSIFICATION ANALYSIS ===");
        System.out.println("Testing specific individual reasoning for key players");
        
        // Test specific individuals mentioned in TOP_TESTS.md
        String[] keyPlayers = {
            "ABox_Rico_Lewis",       // Should be YoungPlayer (age 19)
            "ABox_Nico_Paz",         // Should be YoungPlayer (age 20)  
            "ABox_Jude_Bellingham",  // Should be YoungPlayer (age 21) AND TopPlayer (180M)
            "ABox_Erling_Haaland",   // Should be TopPlayer (180M)
            "ABox_Vinicius_Junior",  // Should be TopPlayer (150M)
            "ABox_Kylian_Mbappe",    // Should be TopPlayer (180M)
            "ABox_Harry_Kane"        // Should be TopPlayer (100M)
        };
        
        String[] expectedClasses = {
            "YoungPlayer",
            "YoungPlayer", 
            "YoungPlayer,TopPlayer",
            "TopPlayer",
            "TopPlayer",
            "TopPlayer",
            "TopPlayer"
        };
        
        boolean allIndividualTestsPassed = true;
        
        for (int i = 0; i < keyPlayers.length; i++) {
            String individual = keyPlayers[i];
            String expected = expectedClasses[i];
            
            System.out.printf("\nAnalyzing Individual: %s%n", individual);
            System.out.printf("Expected Classes: %s%n", expected);
            
            try {
                // Get inferred classes for this individual
                Set<String> inferredClassesSet = reasoningEngine.getInferredClassesForIndividual(individual);
                List<String> inferredClasses = new java.util.ArrayList<>(inferredClassesSet);
                
                System.out.printf("Inferred Classes: %s%n", 
                    inferredClasses.isEmpty() ? "None" : String.join(", ", inferredClasses));
                
                // Check if expected classes are present
                String[] expectedArray = expected.split(",");
                boolean individualPassed = true;
                
                for (String expectedClass : expectedArray) {
                    boolean hasClass = reasoningEngine.isIndividualOfClass(individual, expectedClass);
                    System.out.printf("  %s: %s%n", expectedClass, hasClass ? "✓" : "✗");
                    
                    if (!hasClass) {
                        individualPassed = false;
                        allIndividualTestsPassed = false;
                    }
                }
                
                System.out.printf("Individual Test Result: %s%n", individualPassed ? "PASS" : "FAIL");
                
            } catch (Exception e) {
                System.out.printf("Error analyzing individual %s: %s%n", individual, e.getMessage());
                allIndividualTestsPassed = false;
            }
        }
        
        System.out.println("\n=== INDIVIDUAL ANALYSIS SUMMARY ===");
        System.out.printf("All individual tests passed: %s%n", allIndividualTestsPassed ? "YES" : "NO");
        
        assertTrue(allIndividualTestsPassed, "All individual classification tests must pass");
    }
}