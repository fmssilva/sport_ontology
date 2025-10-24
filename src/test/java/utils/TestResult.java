package utils;

/**
 * Enhanced test result class with reasoning support
 */
public class TestResult {
    public final String testId;
    public final String testName;
    public final String layer; // "SQL", "SPARQL", or "REASONING"
    public final int expected;
    public final int actual;
    public final boolean passed;
    public final String error;
    public final long executionTime;
    public final String worldAssumption;
    
    public TestResult(String testId, String testName, String layer, int expected, int actual, 
                     long executionTime, String worldAssumption) {
        this(testId, testName, layer, expected, actual, null, executionTime, worldAssumption);
    }

    public TestResult(String testId, String testName, String layer, int expected, String error, 
                     long executionTime, String worldAssumption) {
        this(testId, testName, layer, expected, -1, error, executionTime, worldAssumption);
    }

    private TestResult(String testId, String testName, String layer, int expected, int actual, 
                      String error, long executionTime, String worldAssumption) {
        this.testId = testId;
        this.testName = testName;
        this.layer = layer;
        this.expected = expected;
        this.actual = actual;
        this.passed = (actual == expected && error == null);
        this.error = error;
        this.executionTime = executionTime;
        this.worldAssumption = worldAssumption != null ? worldAssumption : "CWA";
    }
    
    public void display() {
        String status = passed ? "PASS" : "FAIL";
        String actualStr = (actual == -1) ? "ERR" : String.valueOf(actual);
        String timeStr = executionTime > 0 ? String.format(" (%dms)", executionTime) : "";
        String assumptionStr = worldAssumption != null ? String.format(" [%s]", worldAssumption) : "";
        
        System.out.printf("  %-8s | %-15s | %-8s | Expected: %2d | Actual: %3s | %s%s%s%n", 
            testId, testName, layer, expected, actualStr, status, assumptionStr, timeStr);
            
        if (error != null) {
            System.out.printf("    Error: %s%n", error);
        }
        
        // Performance warnings
        if (executionTime > 0) {
            boolean isSlowSQL = "SQL".equals(layer) && executionTime > 100;
            boolean isSlowSPARQL = "SPARQL".equals(layer) && executionTime > 5000;
            boolean isSlowReasoning = "REASONING".equals(layer) && executionTime > 10000;
            
            if (isSlowSQL || isSlowSPARQL || isSlowReasoning) {
                long threshold = isSlowSQL ? 100 : (isSlowSPARQL ? 5000 : 10000);
                System.out.printf("    WARNING: Slow %s operation: %dms (threshold: %dms)%n", 
                    layer, executionTime, threshold);
            }
        }
    }
}