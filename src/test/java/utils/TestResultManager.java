package utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global test result manager for collecting and summarizing test results across all test execution
 */
public class TestResultManager {
    private static final TestResultManager INSTANCE = new TestResultManager();
    
    private final Map<String, List<TestResult>> testSuiteResults = new ConcurrentHashMap<>();
    private final AtomicInteger totalTests = new AtomicInteger(0);
    private final AtomicInteger totalPassed = new AtomicInteger(0);
    private final AtomicInteger totalFailed = new AtomicInteger(0);
    private long startTime = 0;
    private long endTime = 0;
    
    private TestResultManager() {}
    
    public static TestResultManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize test session
     */
    public void startTestSession() {
        testSuiteResults.clear();
        totalTests.set(0);
        totalPassed.set(0);
        totalFailed.set(0);
        startTime = System.currentTimeMillis();
        endTime = 0;
        
        System.out.println("Sport Ontology Test Session Started");
        System.out.println("=" + "=".repeat(70));
    }
    
    /**
     * Register test results for a specific test suite
     */
    public void registerTestResults(String testSuite, List<TestResult> results) {
        testSuiteResults.put(testSuite, new ArrayList<>(results));
        
        for (TestResult result : results) {
            totalTests.incrementAndGet();
            if (result.passed) {
                totalPassed.incrementAndGet();
            } else {
                totalFailed.incrementAndGet();
            }
        }
        
        // Display test suite results immediately
        displayTestSuiteResults(testSuite, results);
    }
    
    /**
     * Register a single test result
     */
    public void registerTestResult(String testSuite, TestResult result) {
        testSuiteResults.computeIfAbsent(testSuite, k -> new ArrayList<>()).add(result);
        
        totalTests.incrementAndGet();
        if (result.passed) {
            totalPassed.incrementAndGet();
        } else {
            totalFailed.incrementAndGet();
        }
    }
    
    /**
     * Display results for a specific test suite
     */
    private void displayTestSuiteResults(String testSuite, List<TestResult> results) {
        int passed = (int) results.stream().mapToInt(r -> r.passed ? 1 : 0).sum();
        int failed = results.size() - passed;
        
        System.out.printf("%n Test Suite: %s%n", testSuite);
        System.out.println("-".repeat(50));
        
        for (TestResult result : results) {
            result.display();
        }
        
        String status = failed == 0 ? "PASSED" : "FAILED";
        System.out.printf("%n%n%n%n Suite Summary: %d tests | %d passed | %d failed | %s%n", 
            results.size(), passed, failed, status);
        System.out.println();
    }
    
    /**
     * End test session and display final summary
     */
    public void endTestSession() {
        endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SPORT ONTOLOGY TEST RESULTS SUMMARY");
        System.out.println("=" + "=".repeat(80));
        
        // Display detailed test table by domain
        displayDetailedTestTable();
        
        System.out.println("=" + "=".repeat(80));
        System.out.println("GLOBAL TEST RESULTS");
        System.out.println("=" + "=".repeat(80));
        
        // Overall statistics
        double passRate = totalTests.get() > 0 ? (double) totalPassed.get() / totalTests.get() * 100 : 0;
        System.out.printf("Total Tests: %d | Passed: %d | Failed: %d | Success Rate: %.1f%%%n", 
            totalTests.get(), totalPassed.get(), totalFailed.get(), passRate);
        System.out.printf("Total Duration: %.2f seconds%n%n", duration / 1000.0);
        
        // Per-layer breakdown
        displayLayerBreakdown();
        
        // Per-suite breakdown
        displaySuiteBreakdown();
        
        // Performance analysis
        displayPerformanceAnalysis();
        
        // Final status
        String finalStatus = totalFailed.get() == 0 ? "ALL TESTS PASSED" : "SOME TESTS FAILED";
        System.out.printf("%nFinal Status: %s%n", finalStatus);
        
        if (totalFailed.get() > 0) {
            displayFailuresSummary();
        }
        
        System.out.println("=" + "=".repeat(70));
    }
    
    /**
     * Display detailed test table organized by domain
     */
    private void displayDetailedTestTable() {
        // Organize tests by domain
        Map<String, List<TestResultRow>> domainTests = new LinkedHashMap<>();
        
        for (Map.Entry<String, List<TestResult>> entry : testSuiteResults.entrySet()) {
            String domain = mapSuiteToDomain(entry.getKey());
            
            for (TestResult result : entry.getValue()) {
                domainTests.computeIfAbsent(domain, k -> new ArrayList<>())
                          .add(new TestResultRow(result.testId, result.testName, result.layer, result.passed));
            }
        }
        
        // Group by test ID to combine SQL/SPARQL/REASONING results
        Map<String, Map<String, String>> testMatrix = new LinkedHashMap<>();
        Map<String, String> testDescriptions = new HashMap<>();
        
        for (Map.Entry<String, List<TestResultRow>> domainEntry : domainTests.entrySet()) {
            String domain = domainEntry.getKey();
            
            for (TestResultRow row : domainEntry.getValue()) {
                String key = domain + "|" + row.testId;
                testMatrix.computeIfAbsent(key, k -> new HashMap<>())
                          .put(row.layer, row.passed ? "OK" : "FAIL");
                
                // Store test description (use the shortest meaningful description)
                String desc = getShortDescription(row.testId, row.testName);
                testDescriptions.put(key, desc);
            }
        }
        
        // Display the table
        System.out.printf("%-15s %-8s %-35s %-8s %-8s %-8s%n", 
            "DOMAIN", "TEST ID", "DESCRIPTION", "SQL", "SPARQL", "HERMIT");
        System.out.println("-".repeat(80));
        
        String currentDomain = "";
        for (Map.Entry<String, Map<String, String>> entry : testMatrix.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String domain = parts[0];
            String testId = parts[1];
            String description = testDescriptions.get(entry.getKey());
            Map<String, String> results = entry.getValue();
            
            // Only show domain name on first test of each domain
            String domainDisplay = domain.equals(currentDomain) ? "" : domain;
            currentDomain = domain;
            
            System.out.printf("%-15s %-8s %-35s %-8s %-8s %-8s%n",
                domainDisplay,
                testId,
                description,
                results.getOrDefault("SQL", "---"),
                results.getOrDefault("SPARQL", "---"),
                results.getOrDefault("REASONING", "---")
            );
        }
        
        System.out.println();
    }
    
    /**
     * Map test suite name to domain
     */
    private String mapSuiteToDomain(String suiteName) {
        if (suiteName.contains("DataIntegrity")) return "INTEGRITY";
        if (suiteName.contains("OpenWorld")) return "ASSUMPTIONS";
        if (suiteName.contains("BasicReasoning")) return "REASONING";
        if (suiteName.contains("EngineIntegration")) return "INTEGRATION";
        return "OTHER";
    }
    
    /**
     * Get short description for test
     */
    private String getShortDescription(String testId, String testName) {
        // Map test IDs to short descriptions
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("INT-01", "Teams count consistency");
        descriptions.put("INT-02", "Players count consistency");
        descriptions.put("INT-03", "Coaches count consistency");
        descriptions.put("INT-04", "Database performance test");
        descriptions.put("OWA-01", "Market values OWA vs CWA");
        descriptions.put("REA-01", "TopPlayer inference");
        descriptions.put("REA-02", "YoungPlayer inference");
        descriptions.put("REA-03", "TopYoungPlayer intersection");
        
        String desc = descriptions.get(testId);
        if (desc != null) return desc;
        
        // Fallback: clean up test name
        String cleaned = testName.toLowerCase()
            .replace("_", " ")
            .replace("test", "")
            .replace("inference", "infer")
            .trim();
        
        // Truncate if too long
        return cleaned.length() > 35 ? cleaned.substring(0, 32) + "..." : cleaned;
    }
    
    /**
     * Helper class for organizing test results
     */
    private static class TestResultRow {
        final String testId;
        final String testName;
        final String layer;
        final boolean passed;
        
        TestResultRow(String testId, String testName, String layer, boolean passed) {
            this.testId = testId;
            this.testName = testName;
            this.layer = layer;
            this.passed = passed;
        }
    }
    
    /**
     * Display breakdown by test layer (SQL, SPARQL, REASONING)
     */
    private void displayLayerBreakdown() {
        Map<String, Integer> layerPassed = new HashMap<>();
        Map<String, Integer> layerTotal = new HashMap<>();
        
        for (List<TestResult> results : testSuiteResults.values()) {
            for (TestResult result : results) {
                layerTotal.merge(result.layer, 1, Integer::sum);
                if (result.passed) {
                    layerPassed.merge(result.layer, 1, Integer::sum);
                }
            }
        }
        
        System.out.println("📊 Results by Layer:");
        for (String layer : Arrays.asList("SQL", "SPARQL", "REASONING")) {
            int total = layerTotal.getOrDefault(layer, 0);
            int passed = layerPassed.getOrDefault(layer, 0);
            int failed = total - passed;
            double rate = total > 0 ? (double) passed / total * 100 : 0;
            
            if (total > 0) {
                String icon = failed == 0 ? "✅" : "⚠️";
                System.out.printf("   %s %-9s: %d/%d passed (%.1f%%)%n", 
                    icon, layer, passed, total, rate);
            }
        }
        System.out.println();
    }
    
    /**
     * Display breakdown by test suite
     */
    private void displaySuiteBreakdown() {
        System.out.println("📋 Results by Test Suite:");
        
        for (Map.Entry<String, List<TestResult>> entry : testSuiteResults.entrySet()) {
            String suite = entry.getKey();
            List<TestResult> results = entry.getValue();
            
            int passed = (int) results.stream().mapToInt(r -> r.passed ? 1 : 0).sum();
            int failed = results.size() - passed;
            double rate = results.size() > 0 ? (double) passed / results.size() * 100 : 0;
            
            String icon = failed == 0 ? "✅" : "❌";
            System.out.printf("   %s %-25s: %d/%d passed (%.1f%%)%n", 
                icon, suite, passed, results.size(), rate);
        }
        System.out.println();
    }
    
    /**
     * Display performance analysis
     */
    private void displayPerformanceAnalysis() {
        List<TestResult> slowTests = new ArrayList<>();
        
        for (List<TestResult> results : testSuiteResults.values()) {
            for (TestResult result : results) {
                boolean isSlow = false;
                if ("SQL".equals(result.layer) && result.executionTime > 100) isSlow = true;
                if ("SPARQL".equals(result.layer) && result.executionTime > 5000) isSlow = true;
                if ("REASONING".equals(result.layer) && result.executionTime > 10000) isSlow = true;
                
                if (isSlow) {
                    slowTests.add(result);
                }
            }
        }
        
        if (!slowTests.isEmpty()) {
            System.out.println("⚠️  Performance Warnings:");
            slowTests.sort((a, b) -> Long.compare(b.executionTime, a.executionTime));
            
            for (TestResult result : slowTests.subList(0, Math.min(5, slowTests.size()))) {
                System.out.printf("   🐌 %s/%s: %dms (%s layer)%n", 
                    result.testId, result.testName, result.executionTime, result.layer);
            }
            
            if (slowTests.size() > 5) {
                System.out.printf("   ... and %d more slow tests%n", slowTests.size() - 5);
            }
            System.out.println();
        }
    }
    
    /**
     * Display summary of failures
     */
    private void displayFailuresSummary() {
        System.out.println("%n❌ Failed Tests Summary:");
        
        for (Map.Entry<String, List<TestResult>> entry : testSuiteResults.entrySet()) {
            List<TestResult> failures = entry.getValue().stream()
                .filter(r -> !r.passed)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                
            if (!failures.isEmpty()) {
                System.out.printf("%n   Suite: %s%n", entry.getKey());
                for (TestResult failure : failures) {
                    System.out.printf("   - %s/%s (%s): %s%n", 
                        failure.testId, failure.testName, failure.layer, 
                        failure.error != null ? failure.error : "Expected " + failure.expected + " but got " + failure.actual);
                }
            }
        }
    }
    
    /**
     * Get current session statistics
     */
    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", totalTests.get());
        stats.put("totalPassed", totalPassed.get());
        stats.put("totalFailed", totalFailed.get());
        stats.put("duration", endTime > 0 ? endTime - startTime : System.currentTimeMillis() - startTime);
        stats.put("testSuites", testSuiteResults.size());
        return stats;
    }
    
    /**
     * Clear all test results (for cleanup)
     */
    public void clear() {
        testSuiteResults.clear();
        totalTests.set(0);
        totalPassed.set(0);
        totalFailed.set(0);
        startTime = 0;
        endTime = 0;
    }
}