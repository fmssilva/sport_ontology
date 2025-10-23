package tests.categories.integrity;

import tests.categories.TestCase;
import java.util.*;

/**
 * Basic Data Integrity Tests (Foundation Layer)  
 * Purpose: Validate that OBDA mappings correctly bridge relational and semantic data
 */
public class IntegrityTests {
    
    public static List<TestCase> getTests() {
        return Arrays.asList(
            // INT-01: total_teams - Verify basic entity counting consistency
            new TestCase(
                "INT-01",
                "total_teams",
                "integrity",
                "Verify basic entity counting consistency",
                "SELECT COUNT(*) FROM team",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?team) as ?count) WHERE { ?team a sports:Team }",
                7,
                "SQL ↔ SPARQL"
            ),
            
            // INT-02: total_players - Validate player entity mapping accuracy  
            new TestCase(
                "INT-02",
                "total_players",
                "integrity", 
                "Validate player entity mapping accuracy",
                "SELECT COUNT(DISTINCT p.person_id) FROM person p " +
                "JOIN player_role pr ON p.person_id = pr.person_id",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(DISTINCT ?player) as ?count) WHERE { ?player a sports:Player }",
                12,
                "SQL ↔ SPARQL"
            ),
            
            // INT-03: total_coaches - Ensure coach role mapping integrity
            new TestCase(
                "INT-03",
                "total_coaches",
                "integrity",
                "Ensure coach role mapping integrity", 
                "SELECT COUNT(DISTINCT p.person_id) FROM person p " +
                "JOIN coach_role cr ON p.person_id = cr.person_id",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(DISTINCT ?coach) as ?count) WHERE { ?coach a sports:Coach }",
                7,
                "SQL ↔ SPARQL"
            ),
            
            // INT-04: total_contracts - Demonstrate OWA vs CWA difference
            new TestCase(
                "INT-04", 
                "total_contracts",
                "integrity",
                "Count all contracts (demonstrates OWA vs CWA: SQL=active only, SPARQL=all)",
                "SELECT COUNT(*) FROM contract",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?contract) as ?count) WHERE { " +
                "  ?contract a sports:Contract " +
                "}",
                10,
                "OWA vs CWA Demo"
            ),
            
            // INT-05: team_distribution - Validate hierarchical data mapping  
            new TestCase(
                "INT-05",
                "team_distribution", 
                "integrity",
                "Validate hierarchical data mapping",
                "SELECT COUNT(*) FROM team WHERE team_type = 'SeniorTeam'",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?team) as ?count) WHERE { ?team a sports:SeniorTeam }",
                5,
                "SQL ↔ SPARQL"
            )
        );
    }
}