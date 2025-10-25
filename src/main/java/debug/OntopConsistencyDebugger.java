package debug;

import java.sql.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Comprehensive Ontop Consistency Debugger
 * Analyzes database, mappings, and ontology to find consistency issues
 */
public class OntopConsistencyDebugger {
    
    private static final String DB_URL = "jdbc:h2:./deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true";
    private static final String MAPPING_FILE = "deliverables/ontology/sport-ontology-mapping.ttl";
    private static final String ONTOLOGY_FILE = "deliverables/ontology/sport-ontology.owl";
    
    public static void main(String[] args) {
        System.out.println("🔍 ONTOP CONSISTENCY DEBUGGER");
        System.out.println("=====================================");
        
        try {
            // Step 1: Analyze database for constraint violations
            analyzeDatabase();
            
            // Step 2: Check mapping file for jersey number mappings
            analyzeMappings();
            
            // Step 3: Check ontology constraints
            analyzeOntologyConstraints();
            
            // Step 4: Simulate OBDA mapping process
            simulateOBDAMapping();
            
            // Step 5: Generate debugging queries
            generateDebuggingQueries();
            
        } catch (Exception e) {
            System.err.println("❌ Error during debugging: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void analyzeDatabase() throws Exception {
        System.out.println("\n📊 STEP 1: DATABASE ANALYSIS");
        System.out.println("----------------------------");
        
        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection(DB_URL, "sa", "")) {
            
            // Check 1: All jersey numbers with detailed analysis
            System.out.println("🔍 Jersey Number Analysis:");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT person_id, jersey_number, position, " +
                    "CASE " +
                    "  WHEN jersey_number IS NULL THEN 'NULL' " +
                    "  WHEN jersey_number < 1 THEN 'TOO_LOW' " +
                    "  WHEN jersey_number > 99 THEN 'TOO_HIGH' " +
                    "  ELSE 'VALID' " +
                    "END as status " +
                    "FROM player_role " +
                    "ORDER BY jersey_number NULLS LAST"
                );
                
                Map<String, Integer> statusCount = new HashMap<>();
                while (rs.next()) {
                    String status = rs.getString("status");
                    statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
                    
                    if (!"VALID".equals(status)) {
                        System.out.printf("  ⚠️  Person %d: Jersey=%s, Position=%s, Status=%s%n",
                            rs.getInt("person_id"),
                            rs.getObject("jersey_number"),
                            rs.getString("position"),
                            status
                        );
                    }
                }
                
                System.out.println("📈 Summary:");
                statusCount.forEach((status, count) -> 
                    System.out.printf("  %s: %d players%n", status, count));
            }
            
            // Check 2: Data types validation
            System.out.println("\n🔍 Data Type Analysis:");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT person_id, jersey_number, " +
                    "CAST(jersey_number AS VARCHAR) as jersey_str, " +
                    "TYPEOF(jersey_number) as data_type " +
                    "FROM player_role " +
                    "WHERE jersey_number IS NOT NULL " +
                    "ORDER BY person_id"
                );
                
                while (rs.next()) {
                    System.out.printf("  Person %d: Value=%s, String='%s', Type=%s%n",
                        rs.getInt("person_id"),
                        rs.getObject("jersey_number"),
                        rs.getString("jersey_str"),
                        rs.getString("data_type")
                    );
                }
            }
        }
    }
    
    private static void analyzeMappings() throws Exception {
        System.out.println("\n📝 STEP 2: MAPPING ANALYSIS");
        System.out.println("---------------------------");
        
        String mappingContent = Files.readString(Paths.get(MAPPING_FILE));
        
        // Check for jersey number mappings
        System.out.println("🔍 Jersey Number Mapping Analysis:");
        
        Pattern jerseyMappingPattern = Pattern.compile(
            "hasJerseyNumber[^;]*;[^.]*\\.", 
            Pattern.DOTALL | Pattern.MULTILINE
        );
        
        Matcher matcher = jerseyMappingPattern.matcher(mappingContent);
        int mappingCount = 0;
        while (matcher.find()) {
            mappingCount++;
            System.out.printf("  Mapping %d:%n%s%n", mappingCount, matcher.group().trim());
        }
        
        if (mappingCount == 0) {
            System.out.println("  ❌ No hasJerseyNumber mappings found!");
        }
        
        // Check for NULL handling
        boolean hasNullHandling = mappingContent.contains("IS NOT NULL") || 
                                 mappingContent.contains("JERSEY_NUMBER IS NOT NULL");
        System.out.printf("  NULL handling present: %s%n", hasNullHandling ? "✅ YES" : "❌ NO");
        
        // Check for separate jersey mapping
        boolean hasSeparateJerseyMapping = mappingContent.contains("jersey-number-mapping");
        System.out.printf("  Separate jersey mapping: %s%n", hasSeparateJerseyMapping ? "✅ YES" : "❌ NO");
    }
    
    private static void analyzeOntologyConstraints() throws Exception {
        System.out.println("\n🎯 STEP 3: ONTOLOGY CONSTRAINT ANALYSIS");
        System.out.println("---------------------------------------");
        
        String ontologyContent = Files.readString(Paths.get(ONTOLOGY_FILE));
        
        // Find hasJerseyNumber constraint
        Pattern constraintPattern = Pattern.compile(
            "hasJerseyNumber.*?integer\\[.*?\\]", 
            Pattern.DOTALL
        );
        
        Matcher matcher = constraintPattern.matcher(ontologyContent);
        if (matcher.find()) {
            System.out.println("🔍 Found hasJerseyNumber constraint:");
            System.out.println("  " + matcher.group().trim());
        } else {
            System.out.println("❌ No hasJerseyNumber constraint found in ontology!");
        }
        
        // Check for datatype consistency
        boolean hasDecimalConstraints = ontologyContent.contains("xsd:decimal");
        boolean hasFloatConstraints = ontologyContent.contains("xsd:float");
        boolean hasIntegerConstraints = ontologyContent.contains("xsd:integer");
        
        System.out.printf("  Decimal constraints: %s%n", hasDecimalConstraints ? "YES" : "NO");
        System.out.printf("  Float constraints: %s%n", hasFloatConstraints ? "YES (potential issue)" : "NO");
        System.out.printf("  Integer constraints: %s%n", hasIntegerConstraints ? "YES" : "NO");
    }
    
    private static void simulateOBDAMapping() throws Exception {
        System.out.println("\n🔄 STEP 4: OBDA MAPPING SIMULATION");
        System.out.println("----------------------------------");
        
        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection(DB_URL, "sa", "")) {
            
            // Simulate the exact query from the mapping
            System.out.println("🔍 Simulating jersey number mapping query:");
            try (Statement stmt = conn.createStatement()) {
                
                // Test the query from our fixed mapping
                String testQuery = "SELECT PERSON_ID, JERSEY_NUMBER FROM PLAYER_ROLE WHERE END_DATE IS NULL AND JERSEY_NUMBER IS NOT NULL";
                System.out.println("  Query: " + testQuery);
                
                ResultSet rs = stmt.executeQuery(testQuery);
                System.out.println("  Results (will be mapped to hasJerseyNumber):");
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    int personId = rs.getInt("PERSON_ID");
                    int jersey = rs.getInt("JERSEY_NUMBER");
                    String uri = "http://www.semanticweb.org/sports/data#person/" + personId;
                    
                    System.out.printf("    %s hasJerseyNumber %d%n", uri, jersey);
                    
                    // Check constraint violation
                    if (jersey < 1 || jersey > 99) {
                        System.out.printf("      ❌ CONSTRAINT VIOLATION: %d not in range [1..99]%n", jersey);
                    }
                }
                
                System.out.printf("  Total hasJerseyNumber assertions: %d%n", count);
            }
            
            // Test the OLD query (what might still be cached)
            System.out.println("\n🔍 Testing OLD mapping query (for comparison):");
            try (Statement stmt = conn.createStatement()) {
                String oldQuery = "SELECT PERSON_ID, JERSEY_NUMBER, MARKET_VALUE FROM PLAYER_ROLE WHERE END_DATE IS NULL";
                System.out.println("  Query: " + oldQuery);
                
                ResultSet rs = stmt.executeQuery(oldQuery);
                int totalCount = 0;
                int nullCount = 0;
                int violationCount = 0;
                
                while (rs.next()) {
                    totalCount++;
                    Integer jersey = rs.getObject("JERSEY_NUMBER", Integer.class);
                    
                    if (jersey == null) {
                        nullCount++;
                        System.out.printf("    ❌ Person %d: NULL jersey → would cause inconsistency%n", rs.getInt("PERSON_ID"));
                    } else if (jersey < 1 || jersey > 99) {
                        violationCount++;
                        System.out.printf("    ❌ Person %d: Jersey %d → constraint violation%n", rs.getInt("PERSON_ID"), jersey);
                    }
                }
                
                System.out.printf("  OLD mapping would create: %d total, %d NULL violations, %d range violations%n", 
                    totalCount, nullCount, violationCount);
            }
        }
    }
    
    private static void generateDebuggingQueries() throws Exception {
        System.out.println("\n📋 STEP 5: DEBUGGING QUERIES GENERATED");
        System.out.println("--------------------------------------");
        
        // Generate SPARQL queries for testing
        String sparqlQueries = "# Query 1: Find all hasJerseyNumber assertions\n" +
            "PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n" +
            "SELECT ?player ?jersey WHERE {\n" +
            "    ?player sports:hasJerseyNumber ?jersey .\n" +
            "} ORDER BY ?jersey\n" +
            "\n" +
            "# Query 2: Find constraint violations\n" +
            "PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n" +
            "SELECT ?player ?jersey WHERE {\n" +
            "    ?player sports:hasJerseyNumber ?jersey .\n" +
            "    FILTER(?jersey < 1 || ?jersey > 99)\n" +
            "}\n" +
            "\n" +
            "# Query 3: Count players with and without jerseys\n" +
            "PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n" +
            "SELECT \n" +
            "    (COUNT(DISTINCT ?player) as ?totalPlayers)\n" +
            "    (COUNT(DISTINCT ?playerWithJersey) as ?playersWithJerseys)\n" +
            "WHERE {\n" +
            "    ?player a sports:Player .\n" +
            "    OPTIONAL { ?player sports:hasJerseyNumber ?jersey . BIND(?player as ?playerWithJersey) }\n" +
            "}";
        
        Files.writeString(Paths.get("debug-queries.sparql"), sparqlQueries);
        System.out.println("✅ SPARQL debugging queries saved to: debug-queries.sparql");
        
        // Generate SQL debugging queries
        String sqlQueries = "-- Query 1: Check all jersey numbers\n" +
            "SELECT person_id, jersey_number, \n" +
            "       CASE WHEN jersey_number IS NULL THEN 'NULL'\n" +
            "            WHEN jersey_number < 1 OR jersey_number > 99 THEN 'VIOLATION'\n" +
            "            ELSE 'OK' END as status\n" +
            "FROM player_role \n" +
            "ORDER BY jersey_number NULLS LAST;\n" +
            "\n" +
            "-- Query 2: Simulate new mapping\n" +
            "SELECT PERSON_ID, JERSEY_NUMBER \n" +
            "FROM PLAYER_ROLE \n" +
            "WHERE END_DATE IS NULL AND JERSEY_NUMBER IS NOT NULL;\n" +
            "\n" +
            "-- Query 3: Simulate old mapping\n" +
            "SELECT PERSON_ID, JERSEY_NUMBER, MARKET_VALUE \n" +
            "FROM PLAYER_ROLE \n" +
            "WHERE END_DATE IS NULL;";
        
        Files.writeString(Paths.get("debug-queries.sql"), sqlQueries);
        System.out.println("✅ SQL debugging queries saved to: debug-queries.sql");
        
        System.out.println("\n🎯 NEXT STEPS:");
        System.out.println("1. Check if Protégé is using cached mappings");
        System.out.println("2. Restart Protégé completely");
        System.out.println("3. Re-import the mapping file");
        System.out.println("4. Test with the generated SPARQL queries");
        System.out.println("5. If still failing, check Protégé logs for detailed error info");
    }
}