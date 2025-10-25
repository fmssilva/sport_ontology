package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckJerseyNumbers {
    public static void main(String[] args) {
        String url = "jdbc:h2:./sports-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
        String user = "sa";
        String password = "";
        
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            
            System.out.println("=== CHECKING JERSEY NUMBERS FOR CONSTRAINT VIOLATIONS ===");
            System.out.println("Ontology constraint: hasJerseyNumber Range: integer[>= 1, <= 99]");
            System.out.println();
            
            // Check all jersey numbers
            System.out.println("All jersey numbers in database:");
            ResultSet rs = stmt.executeQuery(
                "SELECT person_id, jersey_number, " +
                "CASE " +
                "  WHEN jersey_number IS NULL THEN 'NULL' " +
                "  WHEN jersey_number < 1 THEN 'TOO_LOW' " +
                "  WHEN jersey_number > 99 THEN 'TOO_HIGH' " +
                "  ELSE 'VALID' " +
                "END as status " +
                "FROM player_role " +
                "ORDER BY jersey_number"
            );
            
            int validCount = 0;
            int invalidCount = 0;
            
            while (rs.next()) {
                int personId = rs.getInt("person_id");
                Integer jerseyNumber = rs.getObject("jersey_number", Integer.class);
                String status = rs.getString("status");
                
                System.out.printf("Person ID: %d, Jersey: %s, Status: %s%n", 
                    personId, 
                    jerseyNumber == null ? "NULL" : jerseyNumber.toString(), 
                    status);
                
                if ("VALID".equals(status)) {
                    validCount++;
                } else {
                    invalidCount++;
                }
            }
            
            System.out.println();
            System.out.printf("Summary: %d valid, %d invalid jersey numbers%n", validCount, invalidCount);
            
            if (invalidCount > 0) {
                System.out.println();
                System.out.println("CONSTRAINT VIOLATIONS FOUND!");
                System.out.println("These violations cause ontology inconsistency.");
                System.out.println("Solutions:");
                System.out.println("1. Fix database data (set valid jersey numbers 1-99)");
                System.out.println("2. Relax ontology constraint (remove range restriction)");
                System.out.println("3. Use OBDA filtering (exclude invalid data in mappings)");
            } else {
                System.out.println("All jersey numbers are valid!");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}