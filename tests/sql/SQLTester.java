package sql;

import integration.TestRegistry;
import tests.categories.TestCase;
import integration.TestResult;
import java.sql.*;
import java.util.*;

/**
 * SQL Tester - Direct H2 database connection
 */
public class SQLTester {
    // Cross-platform database URL
    private static final String DB_URL = getH2DatabaseUrl();
    
    private static String getH2DatabaseUrl() {
        String userDir = System.getProperty("user.dir");
        String projectRoot = new java.io.File(userDir).getParent();
        String dbPath = projectRoot + java.io.File.separator + "database" + java.io.File.separator + "sports-db";
        // H2 accepts forward slashes on all platforms
        dbPath = dbPath.replace("\\", "/");
        return "jdbc:h2:" + dbPath + ";DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    }
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    
    public static List<TestResult> runTests() {
        System.out.println(">> Running SQL Tests...");
        List<TestResult> results = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            
            // Get all test cases and run SQL queries
            for (TestCase testCase : TestRegistry.getIntegrityTests()) {
                TestResult result = executeTest(conn, testCase);
                results.add(result);
                result.display();
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: SQL connection failed: " + e.getMessage());
        }
        
        return results;
    }
    
    private static TestResult executeTest(Connection conn, TestCase testCase) {
        long startTime = System.currentTimeMillis();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(testCase.sqlQuery)) {
            
            if (rs.next()) {
                int actual = rs.getInt(1);
                long executionTime = System.currentTimeMillis() - startTime;
                return new TestResult(testCase.testId, testCase.name, "SQL", testCase.expectedResult, actual, executionTime);
            } else {
                long executionTime = System.currentTimeMillis() - startTime;
                return new TestResult(testCase.testId, testCase.name, "SQL", testCase.expectedResult, 0, executionTime);
            }
            
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new TestResult(testCase.testId, testCase.name, "SQL", testCase.expectedResult, e.getMessage(), executionTime);
        }
    }
}