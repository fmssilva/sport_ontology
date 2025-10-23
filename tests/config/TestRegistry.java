package config;

import java.util.*;

/**
 * Test Registry - Single source of truth for all test cases
 */
public class TestRegistry {
    
    public static class TestCase {
        public final String name;
        public final String sqlQuery;
        public final String sparqlQuery;
        public final int expectedResult;
        public final String comparisonType; // "EXACT" or "SPARQL_GTE_SQL"
        
        public TestCase(String name, String sqlQuery, String sparqlQuery, 
                       int expectedResult, String comparisonType) {
            this.name = name;
            this.sqlQuery = sqlQuery;
            this.sparqlQuery = sparqlQuery;
            this.expectedResult = expectedResult;
            this.comparisonType = comparisonType;
        }
    }
    
    // Single test case - count all teams (more deterministic than people)
    private static final List<TestCase> TEST_CASES = Arrays.asList(
        new TestCase(
            "total_teams",
            "SELECT COUNT(*) FROM TEAM",
            "PREFIX sports: <http://www.semanticweb.org/sports/ontology#> SELECT (COUNT(?team) as ?count) WHERE { ?team a sports:Team }",
            7,
            "EXACT"
        )
    );
    
    public static List<TestCase> getAllTests() {
        return new ArrayList<>(TEST_CASES);
    }
}