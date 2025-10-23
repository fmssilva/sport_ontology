package tests.categories;

/**
 * Shared TestCase class for all category-based tests
 * This class is accessible by all test category packages
 */
public class TestCase {
    public final String testId;
    public final String name;
    public final String category;
    public final String description;
    public final String sqlQuery;
    public final String sparqlQuery;
    public final int expectedResult;
    public final String validationType;
    
    public TestCase(String testId, String name, String category, String description, 
                   String sqlQuery, String sparqlQuery, int expectedResult, String validationType) {
        this.testId = testId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.sqlQuery = sqlQuery;
        this.sparqlQuery = sparqlQuery;
        this.expectedResult = expectedResult;
        this.validationType = validationType;
    }
}