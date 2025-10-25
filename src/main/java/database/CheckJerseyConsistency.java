package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckJerseyConsistency {
    public static void main(String[] args) {
        String url = "jdbc:h2:./deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
        String user = "sa";
        String password = "";
        
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            
            System.out.println("=== COMPREHENSIVE JERSEY NUMBER CONSISTENCY CHECK ===");
            System.out.println("Ontology constraint: hasJerseyNumber Range: integer[>= 1, <= 99]");
            System.out.println();
            
            // Check 1: All jersey numbers including NULLs
            System.out.println("=== 1. ALL JERSEY NUMBERS (including nulls) ===");
            ResultSet rs = stmt.executeQuery(
                "SELECT person_id, jersey_number, position, " +
                "CASE " +
                "  WHEN jersey_number IS NULL THEN 'NULL_VALUE' " +
                "  WHEN jersey_number < 1 THEN 'TOO_LOW' " +
                "  WHEN jersey_number > 99 THEN 'TOO_HIGH' " +
                "  ELSE 'VALID' " +
                "END as status " +
                "FROM player_role " +
                "ORDER BY jersey_number"
            );
            
            int totalCount = 0;
            int nullCount = 0;
            int validCount = 0;
            int invalidCount = 0;
            
            while (rs.next()) {
                totalCount++;
                int personId = rs.getInt("person_id");
                Integer jerseyNumber = rs.getObject("jersey_number", Integer.class);
                String position = rs.getString("position");
                String status = rs.getString("status");
                
                System.out.printf("Person ID: %d, Position: %s, Jersey: %s, Status: %s%n", 
                    personId, 
                    position,
                    jerseyNumber == null ? "NULL" : jerseyNumber.toString(), 
                    status);
                
                if ("NULL_VALUE".equals(status)) {
                    nullCount++;
                } else if ("VALID".equals(status)) {
                    validCount++;
                } else {
                    invalidCount++;
                }
            }
            
            System.out.println();
            System.out.printf("SUMMARY: Total=%d, Valid=%d, Invalid=%d, NULL=%d%n", 
                totalCount, validCount, invalidCount, nullCount);
            
            // Check 2: Datatype issues
            System.out.println("\n=== 2. DATATYPE VALIDATION ===");
            rs = stmt.executeQuery(
                "SELECT person_id, jersey_number, " +
                "CAST(jersey_number AS VARCHAR) as jersey_string " +
                "FROM player_role " +
                "WHERE jersey_number IS NOT NULL"
            );
            
            while (rs.next()) {
                int personId = rs.getInt("person_id");
                Object jerseyObj = rs.getObject("jersey_number");
                String jerseyStr = rs.getString("jersey_string");
                
                System.out.printf("Person ID: %d, Jersey Object: %s (class: %s), String: %s%n", 
                    personId, jerseyObj, jerseyObj.getClass().getSimpleName(), jerseyStr);
            }
            
            // Check 3: Range constraint violations in detail
            System.out.println("\n=== 3. RANGE CONSTRAINT VIOLATIONS ===");
            rs = stmt.executeQuery(
                "SELECT person_id, jersey_number, position " +
                "FROM player_role " +
                "WHERE jersey_number IS NOT NULL AND (jersey_number < 1 OR jersey_number > 99)"
            );
            
            boolean hasViolations = false;
            while (rs.next()) {
                hasViolations = true;
                System.out.printf("VIOLATION: Person %d has jersey %d (position: %s)%n", 
                    rs.getInt("person_id"), rs.getInt("jersey_number"), rs.getString("position"));
            }
            
            if (!hasViolations) {
                System.out.println("✅ No range constraint violations found!");
            }
            
            // Check 4: Check what gets mapped to RDF
            System.out.println("\n=== 4. RDF MAPPING SIMULATION ===");
            System.out.println("Simulating what gets mapped to hasJerseyNumber property...");
            rs = stmt.executeQuery(
                "SELECT person_id, jersey_number, " +
                "CASE WHEN jersey_number IS NOT NULL THEN 'MAPPED' ELSE 'NOT_MAPPED' END as mapping_status " +
                "FROM player_role " +
                "ORDER BY person_id"
            );
            
            while (rs.next()) {
                int personId = rs.getInt("person_id");
                Integer jersey = rs.getObject("jersey_number", Integer.class);
                String mappingStatus = rs.getString("mapping_status");
                
                System.out.printf("Person %d → hasJerseyNumber: %s (%s)%n", 
                    personId, 
                    jersey == null ? "NOT_ASSERTED" : jersey.toString(),
                    mappingStatus);
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