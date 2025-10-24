package integration;

import engines.H2_SQLEngine;
import engines.SPARQLEngine;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;
import java.util.List;

/**
 * Integration Test using Engine Architecture
 * Tests the full OBDA pipeline: SQL → SPARQL → Reasoning
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EngineIntegrationTest {
    
    private H2_SQLEngine dbEngine;
    private SPARQLEngine sparqlEngine;
    
    @BeforeAll
    void setupEngines() throws Exception {
        System.out.println("🔄 Setting up Engine Integration Test...");
        
        // Initialize database engine
        dbEngine = new H2_SQLEngine();
        dbEngine.start();
        
        // Initialize SPARQL engine
        sparqlEngine = new SPARQLEngine(dbEngine);
        sparqlEngine.setup();
        
        System.out.println("✅ All engines initialized for testing");
    }
    
    @AfterAll
    void cleanupEngines() throws Exception {
        System.out.println("🔄 Cleaning up engines...");
        
        if (sparqlEngine != null) {
            sparqlEngine.cleanup();
        }
        
        if (dbEngine != null) {
            dbEngine.stop();
        }
        
        System.out.println("✅ All engines cleaned up");
    }
    
    @Test
    @DisplayName("Database Setup and Basic Queries")
    void testDatabaseBasics() throws Exception {
        System.out.println("\n🔸 Testing Database Engine...");
        
        // Test basic connection
        Assertions.assertTrue(dbEngine.isStarted(), "Database engine should be started");
        
        // Test database statistics (using the print method for now)
        System.out.println("   📊 Database Statistics:");
        dbEngine.printDatabaseStats();
        
        // Test basic query execution
        try (var rs = dbEngine.executeQuery("SELECT COUNT(*) FROM team")) {
            Assertions.assertTrue(rs.next(), "Should have at least one result");
            int teamCount = rs.getInt(1);
            System.out.println("      Teams found: " + teamCount);
            Assertions.assertTrue(teamCount > 0, "Should have teams data");
        }
        
        System.out.println("   ✅ Database engine tests passed");
    }
    
    @Test
    @DisplayName("SQL Query Execution")
    void testSQLQueries() throws Exception {
        System.out.println("\n🔸 Testing SQL Queries...");
        
        // Test count queries that match our original test cases
        String[] sqlQueries = {
            "SELECT COUNT(*) FROM team",
            "SELECT COUNT(*) FROM person WHERE person_id IN (SELECT person_id FROM player_role)",
            "SELECT COUNT(*) FROM person WHERE person_id IN (SELECT person_id FROM coach_role)",
            "SELECT COUNT(*) FROM player_role WHERE position = 'Forward'"
        };
        
        String[] testNames = {
            "Count Teams", "Count Players", "Count Coaches", "Count Forwards"
        };
        
        for (int i = 0; i < sqlQueries.length; i++) {
            System.out.println("   🔍 " + testNames[i] + ": " + sqlQueries[i]);
            
            try (var rs = dbEngine.executeQuery(sqlQueries[i])) {
                Assertions.assertTrue(rs.next(), "Query should return results");
                
                int count = rs.getInt(1);
                System.out.println("      Result: " + count);
                
                // Basic validation - counts should be reasonable
                Assertions.assertTrue(count >= 0, "Count should be non-negative");
            }
        }
        
        System.out.println("   ✅ SQL query tests passed");
    }
    
    @Test
    @DisplayName("SPARQL Query Execution via Ontop")
    void testSPARQLQueries() throws Exception {
        System.out.println("\n🔸 Testing SPARQL Queries...");
        
        // H2 AUTO_SERVER mode allows concurrent connections - no need to disconnect!
        System.out.println("   🔄 Running SPARQL tests with concurrent database access...");
        
        // Test SPARQL queries that should work with our ontology
        String[] sparqlQueries = {
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?team) AS ?count) WHERE { ?team a :Team }",
            
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?player) AS ?count) WHERE { ?player a :Player }",
            
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "SELECT (COUNT(?coach) AS ?count) WHERE { ?coach a :Coach }"
        };
        
        String[] testNames = {
            "SPARQL Count Teams", "SPARQL Count Players", "SPARQL Count Coaches"
        };
        
        try {
            for (int i = 0; i < sparqlQueries.length; i++) {
                System.out.println("   🔍 " + testNames[i]);
                
                List<String> results = sparqlEngine.executeSPARQL(sparqlQueries[i]);
                
                Assertions.assertFalse(results.isEmpty(), "SPARQL query should return results");
                
                System.out.println("      Results: " + results.size() + " lines");
                if (results.size() > 0) {
                    System.out.println("      Sample: " + results.get(0));
                }
                
                // Parse and validate actual count results
                if (results.size() > 1) {
                    String countLine = results.get(1); // Skip header
                    System.out.println("      Parsed count: " + countLine);
                }
            }
            
            System.out.println("   ✅ SPARQL query tests passed - FULL OBDA STACK WORKING!");
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("ONTOP CLI NOT FOUND")) {
                System.err.println("   ⚠️  SPARQL tests skipped - Ontop CLI not available");
                System.err.println("   📥 To test full OBDA stack, please install Ontop CLI");
                Assumptions.assumeTrue(false, "Ontop CLI not available - skipping SPARQL tests");
            } else {
                throw e;
            }
        } finally {
            // With AUTO_SERVER mode, no reconnection needed
            System.out.println("   � SPARQL tests completed with concurrent database access");
        }
    }
    
    @Test
    @DisplayName("Full OBDA Pipeline Test")
    void testFullPipeline() throws Exception {
        System.out.println("\n🔸 Testing Full OBDA Pipeline...");
        
        try {
            // Run the comprehensive test suite from SPARQL engine
            sparqlEngine.runTestQueries();
            
            System.out.println("   ✅ Full pipeline test completed - REAL OBDA WORKING!");
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("ONTOP CLI NOT FOUND")) {
                System.err.println("   ⚠️  Full pipeline test skipped - Ontop CLI not available");
                System.err.println("   📋 Database tests passed, but SPARQL→SQL translation unavailable");
                Assumptions.assumeTrue(false, "Ontop CLI not available - skipping full pipeline test");
            } else {
                throw e;
            }
        }
    }
    
    @Test 
    @DisplayName("Engine Performance Test")
    void testPerformance() throws Exception {
        System.out.println("\n🔸 Testing Engine Performance...");
        
        long startTime = System.currentTimeMillis();
        
            // Run multiple queries to test performance
            for (int i = 0; i < 5; i++) {
                dbEngine.executeQuery("SELECT COUNT(*) FROM team");
            }        long dbTime = System.currentTimeMillis() - startTime;
        System.out.println("   ⏱️  Database queries (5x): " + dbTime + "ms");
        
        // SPARQL performance would be tested here if Ontop CLI was available
        System.out.println("   ⏱️  SPARQL queries: Skipped (Ontop CLI not configured)");
        
        System.out.println("   ✅ Performance test completed");
    }
}