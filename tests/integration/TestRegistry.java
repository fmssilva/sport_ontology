package integration;

import tests.categories.TestCase;
import java.util.*;

/**
 * Enhanced test registry with category-based organization
 * Now uses shared TestCase class from tests.categories package
 */
public class TestRegistry {
    
    /**
     * Basic Data Integrity Tests - DISPATCHER ONLY
     * Loads tests from categories/integrity/IntegrityTests.java
     */
    public static List<TestCase> getIntegrityTests() {
        return getTestsByCategory("integrity");
    }
    
    /**
     * OWA vs CWA Demonstration Tests - DISPATCHER ONLY
     * Loads tests from categories/assumptions/AssumptionTests.java
     */
    public static List<TestCase> getOWACWATests() {
        return getTestsByCategory("assumptions");
    }
    
    /**
     * Get all current test cases (for backward compatibility)
     * Now uses category system - no duplication!
     */
    public static List<TestCase> getAllTests() {
        return getAllTestsWithCategories();
    }
    
    /**
     * Category-based test loading - PURE DISPATCHER
     * Loads tests directly from category-specific files
     */
    @SuppressWarnings("unchecked")
    public static List<TestCase> getTestsByCategory(String category) {
        try {
            String className = null;
            switch(category.toLowerCase()) {
                case "integrity":
                    className = "tests.categories.integrity.IntegrityTests";
                    break;
                case "assumptions":
                    className = "tests.categories.assumptions.AssumptionTests";
                    break;
                case "reasoning":
                    className = "tests.categories.reasoning.ReasoningTests";
                    break;
                default:
                    return new ArrayList<>();
            }
            
            Class<?> testClass = Class.forName(className);
            java.lang.reflect.Method getTests = testClass.getMethod("getTests");
            return (List<TestCase>) getTests.invoke(null);
            
        } catch (Exception e) {
            System.err.println("ERROR: Could not load tests for category '" + category + "': " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Enhanced getAllTests with category support
     */
    public static List<TestCase> getAllTestsWithCategories() {
        List<TestCase> allTests = new ArrayList<>();
        allTests.addAll(getTestsByCategory("integrity"));
        allTests.addAll(getTestsByCategory("assumptions"));
        allTests.addAll(getTestsByCategory("reasoning"));
        return allTests;
    }
}