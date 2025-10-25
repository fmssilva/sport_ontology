package domain;

import utils.TestCase;
import utils.TestExecutor;
import utils.TestResult;
import engines.H2_SQLEngine;
import engines.SPARQLEngine;
import org.junit.jupiter.api.*;
import java.util.List;

/**
 * DL Constructor Demonstration Test Suite
 * 
 * This test suite demonstrates the characteristics, advantages, and limitations
 * of different Description Logic constructors beyond ALC.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DLConstructorDemoTests {
    
    private H2_SQLEngine sqlEngine;
    private SPARQLEngine sparqlEngine;
    private TestExecutor testExecutor;
    
    @BeforeAll
    void setup() throws Exception {
        System.out.println("Setting up DL Constructor Demonstration Test engines...");
        
        // Initialize database engine
        sqlEngine = new H2_SQLEngine();
        sqlEngine.start();
        
        // Initialize SPARQL engine  
        sparqlEngine = new SPARQLEngine(sqlEngine);
        sparqlEngine.setup();
        
        // Initialize test executor
        testExecutor = new TestExecutor(sqlEngine, sparqlEngine);
        
        System.out.println("DL Constructor Demonstration Test engines initialized\n");
    }
    
    @AfterAll
    void cleanup() throws Exception {
        System.out.println("\nCleaning up DL Constructor Demonstration Test engines...");
        if (sparqlEngine != null) sparqlEngine.cleanup();
        if (sqlEngine != null) sqlEngine.stop();
        System.out.println("DL Constructor Demonstration Test engines cleaned up");
    }
    
    private void printTestResult(TestResult result) {
        System.out.println(String.format("  %s   | %s | %s | Expected: %s | Actual: %s | %s [%s] (%dms)",
            result.testId, result.testName, result.layer, 
            result.expected, result.actual,
            result.passed ? "PASS" : "FAIL", result.worldAssumption, result.executionTime));
    }
    
    /**
     * Test Case DC-01: ObjectIntersectionOf Constructor
     */
    @Test
    void testObjectIntersectionOf_StarPlayer() {
        System.out.println("=== DL CONSTRUCTOR DEMONSTRATION TESTS ===");
        System.out.println("Testing ObjectIntersectionOf constructor capabilities");
        System.out.println("StarPlayer ≡ Player ⊓ ∃hasInternationalCaps.integer ⊓ ∃hasMarketValue.float\n");
        
        TestCase testCase = new TestCase(
            "DC-01", "object_intersection_star_player", "DL_CONSTRUCTOR",
            "ObjectIntersectionOf: Complex class definition with multiple existential restrictions",
            "SELECT p.full_name, pr.market_value, " +
            "CASE WHEN pr.international_caps IS NOT NULL THEN pr.international_caps ELSE 0 END as international_caps " +
            "FROM PERSON p " +
            "JOIN PLAYER_ROLE pr ON p.person_id = pr.person_id " +
            "WHERE pr.end_date IS NULL " +
            "AND pr.market_value IS NOT NULL " +
            "AND pr.international_caps IS NOT NULL " +
            "AND pr.international_caps > 0 " +
            "ORDER BY pr.market_value DESC",
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "PREFIX data: <http://www.semanticweb.org/sports/data#> " +
            "SELECT ?playerName ?marketValue ?internationalCaps WHERE { " +
            "?player a :StarPlayer ; " +
            ":hasName ?playerName ; " +
            ":hasMarketValue ?marketValue ; " +
            ":hasInternationalCaps ?internationalCaps . " +
            "FILTER(STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/data#\")) " +
            "} ORDER BY DESC(?marketValue)",
            4, 5, 8, "CONSTRUCTOR_DEMO", "Mixed"
        );
        
        List<TestResult> results = testExecutor.executeTestCase(testCase);
        
        System.out.println("\n Test Suite: DLConstructorDemoTest");
        System.out.println("--------------------------------------------------");
        for (TestResult result : results) {
            printTestResult(result);
        }
        
        System.out.println("\n  CONSTRUCTOR ANALYSIS: ObjectIntersectionOf");
        System.out.println("  ✓ SQL: Basic join conditions (limited semantic reasoning)");
        System.out.println("  ✓ SPARQL/Ontop: Approximates intersection via mapping rules");
        System.out.println("  ✓ HermiT: Complete intersection logic with inference closure");
        System.out.println("  ⚠ Performance: HermiT complexity increases exponentially with intersections");
    }
    
    /**
     * Test Case DC-02: DatatypeRestriction Constructor
     */
    @Test
    void testDatatypeRestriction_TopPlayer() {
        System.out.println("\n\nTesting DatatypeRestriction constructor capabilities");
        System.out.println("TopPlayer ≡ Player ⊓ ∃hasMarketValue.(≥1.0E8)\n");
        
        TestCase testCase = new TestCase(
            "DC-02", "datatype_restriction_top_player", "DL_CONSTRUCTOR",
            "DatatypeRestriction: Numeric constraints with facet restrictions",
            "SELECT p.full_name, pr.market_value, t.name as team_name " +
            "FROM PERSON p " +
            "JOIN PLAYER_ROLE pr ON p.person_id = pr.person_id " +
            "JOIN TEAM t ON pr.team_id = t.team_id " +
            "WHERE pr.end_date IS NULL " +
            "AND pr.market_value >= 100000000 " +
            "ORDER BY pr.market_value DESC",
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "PREFIX data: <http://www.semanticweb.org/sports/data#> " +
            "SELECT ?playerName ?marketValue ?teamName WHERE { " +
            "?player a :TopPlayer ; " +
            ":hasName ?playerName ; " +
            ":hasMarketValue ?marketValue ; " +
            ":playsFor ?team . " +
            "?team :hasName ?teamName . " +
            "FILTER(STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/data#\")) " +
            "} ORDER BY DESC(?marketValue)",
            6, 4, 8, "CONSTRUCTOR_DEMO", "CWA"
        );
        
        List<TestResult> results = testExecutor.executeTestCase(testCase);
        
        for (TestResult result : results) {
            printTestResult(result);
        }
        
        System.out.println("\n  CONSTRUCTOR ANALYSIS: DatatypeRestriction");
        System.out.println("  ✓ SQL: Efficient numeric comparisons");
        System.out.println("  ⚠ SPARQL/Ontop: Limited support for complex datatype restrictions");
        System.out.println("  ✓ HermiT: Complete datatype reasoning but computationally expensive");
        System.out.println("  ⚠ OWL 2 QL Limitation: Facet restrictions not fully supported in OBDA");
    }
    
    /**
     * Test Case DC-03: ObjectPropertyChain Constructor
     */
    @Test
    void testObjectPropertyChain_LeagueParticipation() {
        System.out.println("\n\nTesting ObjectPropertyChain constructor capabilities");
        System.out.println("playsFor ∘ competesIn ⊆ participatesIn\n");
        
        TestCase testCase = new TestCase(
            "DC-03", "object_property_chain_league", "DL_CONSTRUCTOR",
            "ObjectPropertyChain: Transitive property composition",
            "SELECT DISTINCT p.full_name, l.name as league_name " +
            "FROM PERSON p " +
            "JOIN PLAYER_ROLE pr ON p.person_id = pr.person_id " +
            "JOIN TEAM t ON pr.team_id = t.team_id " +
            "JOIN LEAGUE l ON t.league_id = l.league_id " +
            "WHERE pr.end_date IS NULL " +
            "ORDER BY l.name, p.full_name",
            "PREFIX : <http://www.semanticweb.org/sports/ontology#> " +
            "PREFIX data: <http://www.semanticweb.org/sports/data#> " +
            "SELECT ?playerName ?leagueName WHERE { " +
            "?player a :Player ; " +
            ":hasName ?playerName ; " +
            ":participatesIn ?league . " +
            "?league a :League ; " +
            ":hasName ?leagueName . " +
            "FILTER(STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/data#\")) " +
            "} ORDER BY ?leagueName ?playerName",
            12, 0, 16, "CONSTRUCTOR_DEMO", "OWA"
        );
        
        List<TestResult> results = testExecutor.executeTestCase(testCase);
        
        for (TestResult result : results) {
            printTestResult(result);
        }
        
        System.out.println("\n  CONSTRUCTOR ANALYSIS: ObjectPropertyChain");
        System.out.println("  ⚠ SQL: Requires manual JOIN chains, no automatic inference");
        System.out.println("  ✗ SPARQL/Ontop: Property chains not supported in OWL 2 QL");
        System.out.println("  ✓ HermiT: Complete property chain inference with closure computation");
        System.out.println("  ⚠ Complexity: Property chains can cause reasoning performance issues");
        
        System.out.println("\n=== DL CONSTRUCTOR DEMONSTRATION SUMMARY ===");
        System.out.println("Constructor Complexity Beyond ALC:");
        System.out.println("  ✓ ObjectIntersectionOf: Enables rich class definitions but increases complexity");
        System.out.println("  ✓ DatatypeRestriction: Precise value constraints with performance trade-offs");
        System.out.println("  ✓ ObjectPropertyChain: Powerful transitive reasoning, not supported in OWL 2 QL");
        System.out.println("  ⚠ Complex Interactions: Exponential complexity growth with multiple constructors");
        System.out.println("  \n  RECOMMENDATION: Choose constructors based on reasoning requirements vs. performance needs");
    }
}