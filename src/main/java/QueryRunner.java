import java.sql.*;

public class QueryRunner {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:./sports-deliverable-db", "sa", "");
            
            System.out.println("=== FUNCTIONAL PROPERTY VIOLATION CHECK ===");
            System.out.println("Looking for persons with multiple different jersey numbers:");
            
            String query = "SELECT p.person_id, p.name, " +
                    "COUNT(DISTINCT ca.jersey_number) as jersey_count, " +
                    "STRING_AGG(DISTINCT CAST(ca.jersey_number AS VARCHAR), ', ') as jersey_numbers " +
                    "FROM Person p " +
                    "JOIN ContractAssignment ca ON p.person_id = ca.person_id " +
                    "WHERE ca.jersey_number IS NOT NULL " +
                    "GROUP BY p.person_id, p.name " +
                    "HAVING COUNT(DISTINCT ca.jersey_number) > 1 " +
                    "ORDER BY jersey_count DESC";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            boolean foundViolations = false;
            while (rs.next()) {
                foundViolations = true;
                System.out.printf("Person %d (%s): %d different jerseys: %s%n", 
                    rs.getInt("person_id"), 
                    rs.getString("name"), 
                    rs.getInt("jersey_count"),
                    rs.getString("jersey_numbers"));
            }
            
            if (!foundViolations) {
                System.out.println("✓ No persons found with multiple different jersey numbers");
            }
            
            System.out.println("\n=== DETAILED JERSEY ASSIGNMENTS ===");
            query = "SELECT p.person_id, p.name, t.name as team_name, " +
                    "ca.jersey_number, ca.start_date, ca.end_date, " +
                    "CASE WHEN ca.end_date IS NULL THEN 'ACTIVE' ELSE 'ENDED' END as status " +
                    "FROM Person p " +
                    "JOIN ContractAssignment ca ON p.person_id = ca.person_id " +
                    "JOIN Team t ON ca.team_id = t.team_id " +
                    "WHERE ca.jersey_number IS NOT NULL " +
                    "ORDER BY p.person_id, ca.start_date";
            
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.printf("Person %d (%s) - Team: %s, Jersey: %d, Period: %s to %s [%s]%n",
                    rs.getInt("person_id"),
                    rs.getString("name"),
                    rs.getString("team_name"),
                    rs.getInt("jersey_number"),
                    rs.getString("start_date"),
                    rs.getString("end_date") != null ? rs.getString("end_date") : "ongoing",
                    rs.getString("status"));
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}