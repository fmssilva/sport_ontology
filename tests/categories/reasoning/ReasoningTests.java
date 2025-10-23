package tests.categories.reasoning;

import tests.categories.TestCase;
import java.util.*;

/**
 * REASONING TESTS vs OTHER QUERY TYPES - COMPREHENSIVE EXPLANATION
 * 
 * ==== WHAT ARE REASONING TESTS? ====
 * 
 * Reasoning tests evaluate INFERENCE capabilities - the ability to derive NEW facts
 * from existing data using logical rules and ontological knowledge.
 * 
 * KEY DISTINCTION:
 * • Regular SPARQL: "Show me what's explicitly stated"
 * • Reasoning SPARQL: "Show me what can be logically derived"
 * 
 * ==== COMPARISON TABLE ====
 * 
 * Type              | Purpose                    | Example
 * ------------------|----------------------------|------------------------------------------
 * Basic Integrity   | Data mapping validation    | COUNT(*) - does SQL = SPARQL?
 * OWA/CWA Tests     | Assumption demonstration   | Missing data interpretation
 * REASONING Tests   | Logical inference          | Derive facts via rules & ontology
 * 
 * ==== REASONING EXAMPLES ====
 * 
 * 1. TRANSITIVE RELATIONSHIPS
 *    Rule: If A manages B, and B manages C, then A supervises C
 *    Data: Coach X manages Team Y, Team Y contains Player Z  
 *    Inference: Coach X supervises Player Z (not explicitly stated!)
 * 
 * 2. CLASS HIERARCHIES  
 *    Rule: sports:SeniorTeam rdfs:subClassOf sports:Team
 *    Data: "Barcelona" rdf:type sports:SeniorTeam
 *    Inference: "Barcelona" rdf:type sports:Team (automatic!)
 * 
 * 3. PROPERTY CHAINS
 *    Rule: hasContract + worksFor = playsFor  
 *    Data: Player hasContract Contract1, Contract1 worksFor TeamX
 *    Inference: Player playsFor TeamX
 * 
 * 4. DISJOINT CLASSES
 *    Rule: sports:Player owl:disjointWith sports:Coach
 *    Query: Find inconsistencies where person is both Player AND Coach
 * 
 * ==== TECHNICAL REQUIREMENTS ====
 * 
 * Reasoning requires:
 * • OWL reasoner (Pellet, HermiT, Fact++)  
 * • Ontology with inference rules
 * • SPARQL queries that trigger reasoning
 * 
 * Our current setup uses Ontop CLI which does LIMITED reasoning:
 * • Basic RDFS inference (subclass, subproperty)
 * • NO complex OWL reasoning (disjointness, cardinality, etc.)
 * 
 * ==== SAMPLE REASONING TESTS ====
 */
public class ReasoningTests {
    
    public static List<TestCase> getTests() {
        return Arrays.asList(
            
            // REASONING-01: Class Hierarchy Inference
            // Tests if reasoner infers that SeniorTeam instances are also Team instances
            new TestCase(
                "REASONING-01",
                "class_hierarchy_inference",
                "reasoning", 
                "Test class hierarchy: SeniorTeam rdfs:subClassOf Team (reasoner should infer Team membership)",
                // SQL: Count explicit senior teams
                "SELECT COUNT(*) FROM team WHERE team_type = 'SeniorTeam'",
                // SPARQL: Should include inferred Team membership via subclass reasoning
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "SELECT (COUNT(?team) as ?count) WHERE { " +
                "  ?team a sports:SeniorTeam . " +
                "  ?team a sports:Team " +  // This should be inferred, not explicit!
                "}",
                5, // All 5 SeniorTeams should also be inferred as Teams
                "Reasoning: Class Hierarchy"
            ),
            
            // REASONING-02: Property Domain/Range Inference  
            // Tests if reasoner can infer types from property usage
            new TestCase(
                "REASONING-02",
                "property_domain_inference",
                "reasoning",
                "Test property domain: If X hasMarketValue Y, then X must be a Player (domain reasoning)",
                // SQL: Direct count of players with market values
                "SELECT COUNT(DISTINCT pr.person_id) FROM player_role pr WHERE pr.market_value IS NOT NULL",
                // SPARQL: Should infer Player type from hasMarketValue domain
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(DISTINCT ?person) as ?count) WHERE { " +
                "  ?person sports:hasMarketValue ?value . " +
                "  ?person a sports:Player " +  // Should be inferred from domain!
                "}",
                12, // All persons with market values should be inferred as Players
                "Reasoning: Property Domain"
            ),
            
            // REASONING-03: Consistency Check (Negative Test)
            // Tests logical consistency - should find NO conflicts
            new TestCase(
                "REASONING-03", 
                "consistency_check_disjoint",
                "reasoning",
                "Test consistency: No person should be both Player AND Coach simultaneously (disjoint classes)",
                // SQL: Find persons with both roles (should be 0 for consistency)
                "SELECT COUNT(DISTINCT p.person_id) FROM person p " +
                "JOIN player_role pr ON p.person_id = pr.person_id " + 
                "JOIN coach_role cr ON p.person_id = cr.person_id " +
                "WHERE pr.end_date IS NULL AND cr.end_date IS NULL",
                // SPARQL: Reasoning should detect disjoint class violations
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?person) as ?count) WHERE { " +
                "  ?person a sports:Player . " +
                "  ?person a sports:Coach " +  // This should be inconsistent!
                "}",
                0, // Should be 0 - no person can be both Player AND Coach
                "Reasoning: Consistency Check"
            ),
            
            // REASONING-04: Inverse Property Reasoning
            // Tests bidirectional relationship inference
            new TestCase(
                "REASONING-04",
                "inverse_property_inference", 
                "reasoning",
                "Test inverse properties: If Player playsFor Team, then Team hasPlayer Player",
                // SQL: Count team-player relationships from player side
                "SELECT COUNT(*) FROM player_role pr " +
                "JOIN contract c ON pr.person_id = c.person_id " + 
                "WHERE pr.end_date IS NULL",
                // SPARQL: Should infer hasPlayer from playsFor inverse
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?relation) as ?count) WHERE { " +
                "  ?player sports:playsFor ?team . " +
                "  ?team sports:hasPlayer ?player " +  // Should be inferred via inverse!
                "}",
                10, // All active player-team relationships should have inverse
                "Reasoning: Inverse Properties"
            )
        );
    }
    
    /**
     * REASONING vs NON-REASONING SUMMARY:
     * 
     * NON-REASONING (our current tests):
     * ✓ Data integrity validation (SQL ↔ SPARQL consistency)  
     * ✓ OWA vs CWA assumption demonstration
     * ✓ Basic query correctness
     * 
     * REASONING (these new tests):
     * ✓ Class hierarchy inference (subClassOf)
     * ✓ Property domain/range inference  
     * ✓ Consistency checking (disjoint classes)
     * ✓ Inverse property derivation
     * 
     * LIMITATION: Ontop CLI has limited reasoning capabilities compared to 
     * full OWL reasoners like Pellet or HermiT. Some tests may not work as expected.
     */
}