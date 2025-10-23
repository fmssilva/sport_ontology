package integration;

/**
 * Enhanced test result class with test ID and timing
 */
public class TestResult {
    public final String testId;
    public final String testName;
    public final String layer; // "SQL" or "SPARQL"
    public final int expected;
    public final int actual;
    public final boolean passed;
    public final String error;
    public final long executionTime;
    
    public TestResult(String testId, String testName, String layer, int expected, int actual, long executionTime) {
        this(testId, testName, layer, expected, actual, null, executionTime);
    }

    public TestResult(String testId, String testName, String layer, int expected, String error, long executionTime) {
        this(testId, testName, layer, expected, -1, error, executionTime);
    }

    private TestResult(String testId, String testName, String layer, int expected, int actual, String error, long executionTime) {
        this.testId = testId;
        this.testName = testName;
        this.layer = layer;
        this.expected = expected;
        this.actual = actual;
        this.passed = (actual == expected && error == null);
        this.error = error;
        this.executionTime = executionTime;
    }
    
    
    // Backward compatibility constructor
    public TestResult(String testName, String layer, int expected, int actual) {
        this("UNK", testName, layer, expected, actual, 0);
    }
    
    // Backward compatibility constructor
    public TestResult(String testName, String layer, int expected, String error) {
        this("UNK", testName, layer, expected, error, 0);
    }
    
    public void display() {
        String status = passed ? "PASS" : "FAIL";
        String actualStr = (actual == -1) ? "ERR" : String.valueOf(actual);
        String timeStr = executionTime > 0 ? String.format(" (%dms)", executionTime) : "";
        
        System.out.printf("  %-8s | %-15s | %-6s | Expected: %2d | Actual: %3s | %s%s%n", 
            testId, testName, layer, expected, actualStr, status, timeStr);
            
        if (error != null) {
            System.out.printf("    └─ Error: %s%n", error);
        }
        
        // Performance warning using static method (avoid config dependency)
        if (executionTime > 0) {
            boolean isSlowSQL = "SQL".equals(layer) && executionTime > 100;
            boolean isSlowSPARQL = "SPARQL".equals(layer) && executionTime > 5000;
            
            if (isSlowSQL || isSlowSPARQL) {
                long threshold = isSlowSQL ? 100 : 5000;
                System.out.printf("    -> WARNING: Slow %s query: %dms (threshold: %dms)%n", layer, executionTime, threshold);
            }
        }
    }
}