package utils;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * JUnit Test Execution Listener that integrates with TestResultManager
 * This listener initializes and finalizes test sessions
 */
public class TestManagerListener implements TestExecutionListener {
    
    private TestResultManager testManager;
    
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        testManager = TestResultManager.getInstance();
        testManager.startTestSession();
        
        System.out.println("📋 Test Plan Started with " + testPlan.getRoots().size() + " test suites");
    }
    
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (testManager != null) {
            testManager.endTestSession();
        }
        
        System.out.println("🏁 Test Plan Execution Completed");
    }
    
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            System.out.printf("▶️  Starting: %s%n", testIdentifier.getDisplayName());
        }
    }
    
    @Override
    public void executionFinished(TestIdentifier testIdentifier, 
                                 org.junit.platform.engine.TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            String status = testExecutionResult.getStatus().name();
            String icon = testExecutionResult.getStatus() == 
                org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL ? "✅" : "❌";
            
            System.out.printf("%s Finished: %s [%s]%n", 
                icon, testIdentifier.getDisplayName(), status);
        }
    }
}