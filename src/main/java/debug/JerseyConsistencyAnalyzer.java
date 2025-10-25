package debug;

import java.sql.*;

/**
 * Jersey Number Consistency Analyzer
 * Checks for logical inconsistencies that could cause Ontop errors
 */
public class JerseyConsistencyAnalyzer {
    
    private static final String DB_URL = "jdbc:h2:./deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    
    public static void main(String[] args) {
        System.out.println("🔍 JERSEY NUMBER CONSISTENCY ANALYZER");
        System.out.println("=====================================");
        
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, "sa", "")) {
                
                // Check 1: Jersey number range violations
                System.out.println("\n📊 CHECK 1: Jersey Number Range");
                checkJerseyRange(conn);
                
                // Check 2: Duplicate jersey numbers within teams
                System.out.println("\n📊 CHECK 2: Duplicate Jersey Numbers");
                checkDuplicateJerseys(conn);
                
                // Check 3: NULL jersey numbers (should not exist in mappings)
                System.out.println("\n📊 CHECK 3: NULL Jersey Numbers");
                checkNullJerseys(conn);
                
                // Check 4: Functional property violations
                System.out.println("\n📊 CHECK 4: Functional Property Violations");
                checkFunctionalViolations(conn);
                
                // Check 5: Multiple team assignments
                System.out.println("\n📊 CHECK 5: Multiple Team Assignments");
                checkMultipleTeams(conn);
                
                System.out.println("\n🎯 ANALYSIS COMPLETE");
                
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkJerseyRange(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT person_id, jersey_number, position " +
                "FROM player_role " +
                "WHERE jersey_number IS NOT NULL " +
                "  AND (jersey_number < 1 OR jersey_number > 99) " +
                "ORDER BY jersey_number"
            );
            
            boolean foundViolations = false;
            while (rs.next()) {
                foundViolations = true;
                System.out.printf("  ❌ Person %d: Jersey %d (position: %s) - VIOLATES RANGE [1-99]%n",
                    rs.getInt("person_id"), rs.getInt("jersey_number"), rs.getString("position"));
            }
            
            if (!foundViolations) {
                System.out.println("  ✅ All jersey numbers are within range [1-99]");
            }
        }
    }
    
    private static void checkDuplicateJerseys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT team_id, jersey_number, COUNT(*) as count, " +
                "       GROUP_CONCAT(person_id) as person_ids " +
                "FROM player_role " +
                "WHERE jersey_number IS NOT NULL " +
                "  AND end_date IS NULL " +
                "GROUP BY team_id, jersey_number " +
                "HAVING COUNT(*) > 1 " +
                "ORDER BY team_id, jersey_number"
            );
            
            boolean foundDuplicates = false;
            while (rs.next()) {
                foundDuplicates = true;
                System.out.printf("  ❌ Team %d: Jersey %d used by %d players (persons: %s)%n",
                    rs.getInt("team_id"), rs.getInt("jersey_number"), 
                    rs.getInt("count"), rs.getString("person_ids"));
            }
            
            if (!foundDuplicates) {
                System.out.println("  ✅ No duplicate jersey numbers within teams");
            }
        }
    }
    
    private static void checkNullJerseys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT person_id, team_id, position " +
                "FROM player_role " +
                "WHERE jersey_number IS NULL " +
                "  AND end_date IS NULL " +
                "ORDER BY team_id, person_id"
            );
            
            int nullCount = 0;
            while (rs.next()) {
                nullCount++;
                System.out.printf("  ⚠️  Person %d (Team %d, Position: %s): NULL jersey number%n",
                    rs.getInt("person_id"), rs.getInt("team_id"), rs.getString("position"));
            }
            
            if (nullCount == 0) {
                System.out.println("  ✅ No NULL jersey numbers found");
            } else {
                System.out.printf("  ⚠️  Found %d players with NULL jersey numbers%n", nullCount);
                System.out.println("     (These should be excluded from hasJerseyNumber mappings)");
            }
        }
    }
    
    private static void checkFunctionalViolations(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if any person has multiple jersey numbers (across different roles)
            ResultSet rs = stmt.executeQuery(
                "SELECT person_id, COUNT(DISTINCT jersey_number) as jersey_count, " +
                "       GROUP_CONCAT(DISTINCT jersey_number) as jerseys, " +
                "       GROUP_CONCAT(DISTINCT team_id) as teams " +
                "FROM player_role " +
                "WHERE jersey_number IS NOT NULL " +
                "  AND end_date IS NULL " +
                "GROUP BY person_id " +
                "HAVING COUNT(DISTINCT jersey_number) > 1 " +
                "ORDER BY person_id"
            );
            
            boolean foundViolations = false;
            while (rs.next()) {
                foundViolations = true;
                System.out.printf("  ❌ Person %d: Multiple jerseys (%s) across teams (%s)%n",
                    rs.getInt("person_id"), rs.getString("jerseys"), rs.getString("teams"));
            }
            
            if (!foundViolations) {
                System.out.println("  ✅ No functional property violations (each person has at most one jersey)");
            }
        }
    }
    
    private static void checkMultipleTeams(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                "SELECT person_id, COUNT(DISTINCT team_id) as team_count, " +
                "       GROUP_CONCAT(DISTINCT team_id) as teams, " +
                "       GROUP_CONCAT(DISTINCT jersey_number) as jerseys " +
                "FROM player_role " +
                "WHERE end_date IS NULL " +
                "GROUP BY person_id " +
                "HAVING COUNT(DISTINCT team_id) > 1 " +
                "ORDER BY person_id"
            );
            
            boolean foundMultiple = false;
            while (rs.next()) {
                foundMultiple = true;
                System.out.printf("  ⚠️  Person %d: Plays for %d teams (%s) with jerseys (%s)%n",
                    rs.getInt("person_id"), rs.getInt("team_count"), 
                    rs.getString("teams"), rs.getString("jerseys"));
            }
            
            if (!foundMultiple) {
                System.out.println("  ✅ No players assigned to multiple teams simultaneously");
            } else {
                System.out.println("     (Multiple team assignments can cause functional property conflicts)");
            }
        }
    }
}