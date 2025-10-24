package config;

/**
 * Test Categories Configuration
 * Defines test category metadata and organization
 */
public class TestCategories {
    
    // Test Categories
    public static final String FOUNDATION = "foundation";
    public static final String REASONING = "reasoning";
    public static final String ASSUMPTIONS = "assumptions";
    public static final String ADVANCED = "advanced";
    public static final String PERFORMANCE = "performance";
    public static final String ROBUSTNESS = "robustness";
    public static final String COMPATIBILITY = "compatibility";
    
    // Test Phases
    public static final String PHASE_1 = "Phase 1: Foundation";
    public static final String PHASE_2 = "Phase 2: Reasoning";
    public static final String PHASE_3 = "Phase 3: Production";
    
    // World Assumptions
    public static final String OWA = "OWA";
    public static final String CWA = "CWA";
    public static final String REASONING_OWA = "REASONING";
    
    /**
     * Get phase for test category
     */
    public static String getPhase(String category) {
        switch (category) {
            case FOUNDATION:
                return PHASE_1;
            case REASONING:
            case ASSUMPTIONS:
                return PHASE_2;
            case ADVANCED:
            case PERFORMANCE:
            case ROBUSTNESS:
            case COMPATIBILITY:
                return PHASE_3;
            default:
                return "Unknown Phase";
        }
    }
    
    /**
     * Check if category is in specified phase
     */
    public static boolean isInPhase(String category, String phase) {
        return getPhase(category).equals(phase);
    }
}