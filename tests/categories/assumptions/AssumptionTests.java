package tests.categories.assumptions;

import tests.categories.TestCase;
import java.util.*;

/**
 * OWA vs CWA Demonstration Tests
 * Purpose: Show understanding of different reasoning assumptions
 * 
 * KEY CONCEPTS:
 * - OWA (Open World Assumption): Missing information ≠ False (could be unknown)  
 * - CWA (Closed World Assumption): Missing information = False (assume completeness)
 */
public class AssumptionTests {
    
    public static List<TestCase> getTests() {
        return Arrays.asList(
            // OWA-01: Missing market values - OWA won't assume "no value" 
            new TestCase(
                "OWA-01",
                "players_with_market_value",
                "assumption",
                "Test OWA: Players with explicit market values (should not assume missing = 0)",
                "SELECT COUNT(*) FROM player_role WHERE market_value IS NOT NULL",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?player) as ?count) WHERE { " +
                "  ?player a sports:Player ; " +
                "           sports:hasMarketValue ?value " +
                "}",
                13, // All players have market values in our data
                "OWA Test"
            ),
            
            // CWA-01: Definite team roster - count only explicit current players
            new TestCase(
                "CWA-01", 
                "current_team_players",
                "assumption",
                "Test CWA: Count only explicitly active players (assume complete roster)",
                "SELECT COUNT(*) FROM player_role WHERE end_date IS NULL",
                "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> " +
                "SELECT (COUNT(?player) as ?count) WHERE { " +
                "  ?player a sports:Player . " +
                "  FILTER NOT EXISTS { ?player sports:hasEndDate ?end } " +
                "}",
                12, // All current active player roles
                "CWA Test"
            )
        );
    }
}