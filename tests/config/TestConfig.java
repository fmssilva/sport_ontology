package config;

import java.util.*;

/**
 * Test Configuration - Control which test suites to run
 * Enables selective execution without running all tests every time
 */
public class TestConfig {
    
    // Test Suite Flags - Set to true to enable, false to skip
    public static final boolean RUN_BASIC_INTEGRITY_TESTS = true;
    public static final boolean RUN_OWA_CWA_DEMO_TESTS = true;
    public static final boolean RUN_PERFORMANCE_BENCHMARKS = false; // Future expansion
    public static final boolean RUN_REASONING_TESTS = false;        // Future expansion
    
    // Execution Configuration
    public static final boolean ENABLE_TIMING = true;
    public static final boolean ENABLE_VERBOSE_OUTPUT = true;
    public static final boolean ENABLE_CROSS_VALIDATION = true;
    public static final boolean STOP_ON_FIRST_FAILURE = false;
    
    // Performance Thresholds (in milliseconds)
    public static final long SQL_PERFORMANCE_THRESHOLD = 100;    // Warn if SQL takes longer
    public static final long SPARQL_PERFORMANCE_THRESHOLD = 5000; // Warn if SPARQL takes longer
    
    /**
     * Get configuration summary for display
     */
    public static void printConfiguration() {
        System.out.println("📋 Test Configuration");
        System.out.println("=" .repeat(50));
        System.out.printf("Basic Integrity Tests: %s%n", RUN_BASIC_INTEGRITY_TESTS ? "ENABLED" : "DISABLED");
        System.out.printf("OWA vs CWA Demo Tests: %s%n", RUN_OWA_CWA_DEMO_TESTS ? "ENABLED" : "DISABLED");
        System.out.printf("Performance Benchmarks: %s%n", RUN_PERFORMANCE_BENCHMARKS ? "ENABLED" : "DISABLED");
        System.out.printf("Reasoning Tests: %s%n", RUN_REASONING_TESTS ? "ENABLED" : "DISABLED");
        System.out.println();
        System.out.printf("Timing: %s | Verbose: %s | Cross-Validation: %s%n", 
            ENABLE_TIMING ? "ON" : "OFF",
            ENABLE_VERBOSE_OUTPUT ? "ON" : "OFF", 
            ENABLE_CROSS_VALIDATION ? "ON" : "OFF");
        System.out.println("=" .repeat(50));
        System.out.println();
    }
    
    /**
     * Check if any test suites are enabled
     */
    public static boolean hasEnabledTests() {
        return RUN_BASIC_INTEGRITY_TESTS || RUN_OWA_CWA_DEMO_TESTS || 
               RUN_PERFORMANCE_BENCHMARKS || RUN_REASONING_TESTS;
    }
    
    /**
     * Get list of enabled test suite names
     */
    public static List<String> getEnabledTestSuites() {
        List<String> enabled = new ArrayList<>();
        if (RUN_BASIC_INTEGRITY_TESTS) enabled.add("Basic Integrity");
        if (RUN_OWA_CWA_DEMO_TESTS) enabled.add("OWA vs CWA Demo");
        if (RUN_PERFORMANCE_BENCHMARKS) enabled.add("Performance Benchmarks");
        if (RUN_REASONING_TESTS) enabled.add("Reasoning Tests");
        return enabled;
    }
    
    /**
     * Check if a query execution time exceeds performance thresholds
     */
    public static boolean isSlowQuery(String layer, long executionTime) {
        if ("SQL".equals(layer)) {
            return executionTime > SQL_PERFORMANCE_THRESHOLD;
        } else if ("SPARQL".equals(layer)) {
            return executionTime > SPARQL_PERFORMANCE_THRESHOLD;
        }
        return false;
    }
    
    /**
     * Get performance warning message
     */
    public static String getPerformanceWarning(String layer, long executionTime) {
        long threshold = "SQL".equals(layer) ? SQL_PERFORMANCE_THRESHOLD : SPARQL_PERFORMANCE_THRESHOLD;
        return String.format("WARNING: Slow %s query: %dms (threshold: %dms)", layer, executionTime, threshold);
    }
}