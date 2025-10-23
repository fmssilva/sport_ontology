package integration;

/**
 * Simple test result class
 */
public class TestResult {
    public final String testName;
    public final String layer; // "SQL" or "SPARQL"
    public final int expected;
    public final int actual;
    public final boolean passed;
    public final String error;
    
    public TestResult(String testName, String layer, int expected, int actual) {
        this.testName = testName;
        this.layer = layer;
        this.expected = expected;
        this.actual = actual;
        this.passed = (actual == expected);
        this.error = null;
    }
    
    public TestResult(String testName, String layer, int expected, String error) {
        this.testName = testName;
        this.layer = layer;
        this.expected = expected;
        this.actual = -1;
        this.passed = false;
        this.error = error;
    }
    
    public void display() {
        String status = passed ? "✅ PASS" : "❌ FAIL";
        String actualStr = (actual == -1) ? "ERR" : String.valueOf(actual);
        
        System.out.printf("  %-15s | %-6s | Expected: %2d | Actual: %3s | %s%n", 
            testName, layer, expected, actualStr, status);
            
        if (error != null) {
            System.out.printf("    └─ Error: %s%n", error);
        }
    }
}