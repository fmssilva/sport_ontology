package utils;

/**
 * Shared TestCase class for all category-based tests
 * Enhanced with reasoning result expectations
 */
public class TestCase {
    public final String testId;
    public final String name;
    public final String category;
    public final String description;
    public final String sqlQuery;
    public final String sparqlQuery;
    public final int expectedSQLResult;
    public final Integer expectedSPARQLResult;  // Can be different from SQL due to OWA/reasoning
    public final Integer expectedReasoningResult; // After inference
    public final String validationType;
    public final String worldAssumption; // "OWA", "CWA", or "REASONING"
    
    // Original constructor (backward compatibility)
    public TestCase(String testId, String name, String category, String description, 
                   String sqlQuery, String sparqlQuery, int expectedResult, String validationType) {
        this(testId, name, category, description, sqlQuery, sparqlQuery, 
             expectedResult, expectedResult, null, validationType, "CWA");
    }
    
    // Enhanced constructor for different result expectations
    public TestCase(String testId, String name, String category, String description,
                   String sqlQuery, String sparqlQuery, 
                   int expectedSQLResult, Integer expectedSPARQLResult, Integer expectedReasoningResult,
                   String validationType, String worldAssumption) {
        this.testId = testId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.sqlQuery = sqlQuery;
        this.sparqlQuery = sparqlQuery;
        this.expectedSQLResult = expectedSQLResult;
        this.expectedSPARQLResult = expectedSPARQLResult != null ? expectedSPARQLResult : expectedSQLResult;
        this.expectedReasoningResult = expectedReasoningResult;
        this.validationType = validationType;
        this.worldAssumption = worldAssumption;
    }
    
    public boolean hasReasoningExpectation() {
        return expectedReasoningResult != null;
    }
    
    public boolean expectsDifferentResults() {
        return expectedSQLResult != expectedSPARQLResult || hasReasoningExpectation();
    }
}