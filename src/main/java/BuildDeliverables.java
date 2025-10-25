import java.io.*;
import java.nio.file.*;

/**
 * Complete deliverables builder for Sport Ontology project.
 * Handles database creation, file copying, documentation generation, and SPARQL consolidation.
 * 
 * Usage: java BuildDeliverables [output-directory]
 * Default output: ./protege_files/
 */
public class BuildDeliverables {
    
    private static final String PROJECT_BASE = System.getProperty("user.dir");
    
    public static void main(String[] args) {
        String outputDir = args.length > 0 ? args[0] : "protege_files";
        buildDeliverables(outputDir);
    }
    
    public static void buildDeliverables(String outputDir) {
        try {
            Path outputPath = Paths.get(outputDir);
            Path ontologyPath = outputPath.resolve("ontology");
            Path databasePath = outputPath.resolve("database");
            Path queriesPath = outputPath.resolve("queries");
            Path sparqlPath = queriesPath.resolve("sparql");
            Path sqlPath = queriesPath.resolve("sql");
            
            // Create directory structure
            Files.createDirectories(ontologyPath);
            Files.createDirectories(databasePath);
            Files.createDirectories(sparqlPath);
            Files.createDirectories(sqlPath);
            
            System.out.println("🎯 Building complete deliverables for Sport Ontology...");
            System.out.println("📁 Output directory: " + outputPath.toAbsolutePath());
            
            // Step 1: Generate H2 Database with seed data
            System.out.println("\n📊 Step 1: Creating H2 database with seed data...");
            createH2Database(databasePath);
            
            // Step 2: Copy ontology files
            System.out.println("\n📋 Step 2: Copying ontology files...");
            copyOntologyFiles(ontologyPath);
            
            // Step 3: Copy query files to separate SPARQL and SQL folders
            System.out.println("\n📝 Step 3: Copying query files to SPARQL and SQL folders...");
            copyQueryFiles(sparqlPath, sqlPath);
            
            // Step 4: Copy H2 JDBC driver for portable setup
            System.out.println("\n🔧 Step 4: Copying h2 JDBC driver for portable setup...");
            copyH2Driver(databasePath);
            
            // Step 5: Create H2 configuration for Protégé
            System.out.println("\n⚙️ Step 5: Creating H2 configuration for Protégé...");
            createH2Config(databasePath);
            
            // Step 6: Generate comprehensive documentation
            System.out.println("\n📚 Step 6: Generating PROTEGE_SET_UP and SEED_DATA_SUMMARY...");
            generateProtegeSetup(outputPath, databasePath);
            generateSeedDataSummary(outputPath);
            
            System.out.println("\n✅ Deliverables build completed successfully!");
            System.out.println("📁 Location: " + outputPath.toAbsolutePath());
            System.out.println("📋 Files ready for Protégé and report submission");
            
        } catch (Exception e) {
            System.err.println("❌ Error building deliverables: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void createH2Database(Path databasePath) throws Exception {
        String dbPath = databasePath.toAbsolutePath().resolve("sports-deliverable-db").toString();
        
        // Use reflection to call CreateH2Database to avoid direct dependency
        try {
            Class<?> dbClass = Class.forName("database.CreateH2Database");
            var main = dbClass.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{dbPath});
            System.out.println("✅ H2 database created: " + dbPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create H2 database: " + e.getMessage(), e);
        }
    }
    
    private static void copyOntologyFiles(Path ontologyPath) throws IOException {
        Path sourceOntology = Paths.get(PROJECT_BASE, "src", "main", "resources", "ontology");
        
        // Copy main ontology files
        Files.copy(
            sourceOntology.resolve("sport-ontology.owl"),
            ontologyPath.resolve("sport-ontology.owl"),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        Files.copy(
            sourceOntology.resolve("sport-ontology-mapping.ttl"),
            ontologyPath.resolve("sport-ontology-mapping.ttl"),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        System.out.println("✅ Ontology files copied");
    }
    
    private static void copyQueryFiles(Path sparqlPath, Path sqlPath) throws IOException {
        Path queriesDir = Paths.get(PROJECT_BASE, "src", "test", "resources", "queries");
        
        if (!Files.exists(queriesDir)) {
            System.out.println("⚠️ Queries directory not found, creating basic query files");
            createBasicQueryFiles(sparqlPath, sqlPath);
            return;
        }
        
        // Copy all SPARQL query files to sparql folder
        try (var paths = Files.walk(queriesDir)) {
            paths.filter(path -> path.toString().endsWith(".sparql"))
                 .sorted()
                 .forEach(queryFile -> {
                     try {
                         String fileName = queryFile.getFileName().toString();
                         Path targetFile = sparqlPath.resolve(fileName);
                         Files.copy(queryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                         System.out.println("✅ Copied SPARQL file: " + fileName);
                     } catch (IOException e) {
                         System.err.println("Warning: Could not copy " + queryFile + ": " + e.getMessage());
                     }
                 });
        }
        
        // Copy all SQL files to sql folder
        try (var paths = Files.walk(queriesDir)) {
            paths.filter(path -> path.toString().endsWith(".sql"))
                 .sorted()
                 .forEach(queryFile -> {
                     try {
                         String fileName = queryFile.getFileName().toString();
                         Path targetFile = sqlPath.resolve(fileName);
                         Files.copy(queryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                         System.out.println("✅ Copied SQL file: " + fileName);
                     } catch (IOException e) {
                         System.err.println("Warning: Could not copy " + queryFile + ": " + e.getMessage());
                     }
                 });
        }
        
        System.out.println("✅ All query files organized into sparql/ and sql/ folders");
    }
    
    private static void createBasicQueryFiles(Path sparqlPath, Path sqlPath) throws IOException {
        // Create SPARQL files
        createBasicSparqlFiles(sparqlPath);
        
        // Create SQL files
        createBasicSqlFiles(sqlPath);
        
        System.out.println("✅ Basic SPARQL and SQL query files created");
    }
    
    private static void createBasicSparqlFiles(Path sparqlPath) throws IOException {
        // File 1: Basic queries
        StringBuilder basicQueries = new StringBuilder();
        basicQueries.append("# Sports Ontology - Basic Count Queries\n");
        basicQueries.append("# Load this file in Protégé: Window → Tabs → SPARQL Query → File → Load Queries\n\n");
        basicQueries.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n\n");
        basicQueries.append("# Test 1: Count all teams (H2 database only)\n");
        basicQueries.append("SELECT (COUNT(?team) as ?count) WHERE {\n");
        basicQueries.append("    ?team a sports:Team .\n");
        basicQueries.append("    FILTER(!STRSTARTS(STR(?team), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        basicQueries.append("}\n\n");
        basicQueries.append("# Test 2: Count all players (H2 database only)\n");
        basicQueries.append("SELECT (COUNT(?player) as ?count) WHERE {\n");
        basicQueries.append("    ?player a sports:Player .\n");
        basicQueries.append("    FILTER(!STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        basicQueries.append("}\n");
        Files.write(sparqlPath.resolve("basic_queries.sparql"), basicQueries.toString().getBytes());
        
        // File 2: Player queries
        StringBuilder playerQueries = new StringBuilder();
        playerQueries.append("# Sports Ontology - Player Queries\n\n");
        playerQueries.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n\n");
        playerQueries.append("# List all players with their teams (H2 database only)\n");
        playerQueries.append("SELECT ?playerName ?teamName WHERE {\n");
        playerQueries.append("    ?player a sports:Player ;\n");
        playerQueries.append("            sports:hasName ?playerName ;\n");
        playerQueries.append("            sports:playsFor ?team .\n");
        playerQueries.append("    ?team sports:hasName ?teamName .\n");
        playerQueries.append("    FILTER(!STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        playerQueries.append("}\n");
        playerQueries.append("ORDER BY ?teamName ?playerName\n");
        Files.write(sparqlPath.resolve("player_queries.sparql"), playerQueries.toString().getBytes());
        
        // File 3: Reasoning queries
        StringBuilder reasoningQueries = new StringBuilder();
        reasoningQueries.append("# Sports Ontology - Reasoning Queries\n\n");
        reasoningQueries.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n\n");
        reasoningQueries.append("# Find TopPlayers (market value >= 100M) - H2 database only\n");
        reasoningQueries.append("SELECT ?name ?value WHERE {\n");
        reasoningQueries.append("    ?player a sports:Player ;\n");
        reasoningQueries.append("            sports:hasName ?name ;\n");
        reasoningQueries.append("            sports:hasMarketValue ?value .\n");
        reasoningQueries.append("    FILTER(?value >= 100000000)\n");
        reasoningQueries.append("    FILTER(!STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        reasoningQueries.append("}\n");
        reasoningQueries.append("ORDER BY DESC(?value)\n");
        Files.write(sparqlPath.resolve("reasoning_queries.sparql"), reasoningQueries.toString().getBytes());
    }
    
    private static void createBasicSqlFiles(Path sqlPath) throws IOException {
        // File 1: Basic count queries
        StringBuilder basicQueries = new StringBuilder();
        basicQueries.append("-- Sports Ontology - Basic Count Queries\n");
        basicQueries.append("-- Execute these in H2 Console or any SQL client\n\n");
        basicQueries.append("-- Test 1: Count all teams\n");
        basicQueries.append("SELECT COUNT(*) as count FROM team;\n\n");
        basicQueries.append("-- Test 2: Count all players\n");
        basicQueries.append("SELECT COUNT(DISTINCT person_id) as count FROM player_role;\n\n");
        basicQueries.append("-- Test 3: Count all coaches\n");
        basicQueries.append("SELECT COUNT(DISTINCT person_id) as count FROM coach_role;\n");
        Files.write(sqlPath.resolve("basic_queries.sql"), basicQueries.toString().getBytes());
        
        // File 2: Player queries
        StringBuilder playerQueries = new StringBuilder();
        playerQueries.append("-- Sports Ontology - Player Queries\n\n");
        playerQueries.append("-- List all active players with their teams\n");
        playerQueries.append("SELECT \n");
        playerQueries.append("    p.full_name as player_name,\n");
        playerQueries.append("    t.name as team_name\n");
        playerQueries.append("FROM person p\n");
        playerQueries.append("JOIN player_role pr ON p.person_id = pr.person_id\n");
        playerQueries.append("JOIN team t ON pr.team_id = t.team_id\n");
        playerQueries.append("WHERE pr.end_date IS NULL\n");
        playerQueries.append("ORDER BY t.name, p.full_name;\n");
        Files.write(sqlPath.resolve("player_queries.sql"), playerQueries.toString().getBytes());
        
        // File 3: Value-based queries
        StringBuilder valueQueries = new StringBuilder();
        valueQueries.append("-- Sports Ontology - Market Value Queries\n\n");
        valueQueries.append("-- Find high-value players (≥ 100M euros)\n");
        valueQueries.append("SELECT \n");
        valueQueries.append("    p.full_name as name,\n");
        valueQueries.append("    pr.market_value as value\n");
        valueQueries.append("FROM person p\n");
        valueQueries.append("JOIN player_role pr ON p.person_id = pr.person_id\n");
        valueQueries.append("WHERE pr.market_value >= 100000000\n");
        valueQueries.append("AND pr.end_date IS NULL\n");
        valueQueries.append("ORDER BY pr.market_value DESC;\n");
        Files.write(sqlPath.resolve("value_queries.sql"), valueQueries.toString().getBytes());
    }
    
    private static void createH2Config(Path databasePath) throws IOException {
        String absoluteDbPath = databasePath.toAbsolutePath().toString().replace("\\", "/");
        
        StringBuilder config = new StringBuilder();
        config.append("jdbc.driver=org.h2.Driver\n");
        config.append("jdbc.password=\n");
        config.append("jdbc.url=jdbc:h2:").append(absoluteDbPath).append("/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true\n");
        config.append("jdbc.user=sa\n\n");
        config.append("# H2 Database Configuration for Protégé OBDA - Auto-generated\n");
        config.append("# Database path is automatically set to: ").append(absoluteDbPath).append("\n");
        config.append("# \n");
        config.append("# IMPORTANT: Install h2 JDBC driver first!\n");
        config.append("# 1. Copy database/h2-2.4.240.jar to Protégé/plugins/ folder (portable)\n");
        config.append("# 2. Restart Protégé completely\n");
        config.append("# 3. Then follow these steps:\n");
        config.append("#    - Open Protégé\n");
        config.append("#    - Load ontology: ontology/sport-ontology.owl\n");
        config.append("#    - Window → Tabs → DataSource\n");
        config.append("#    - Create new datasource with above settings\n");
        config.append("#    - Load mappings: ontology/sport-ontology-mapping.ttl\n");
        config.append("# \n");
        config.append("# Troubleshooting:\n");
        config.append("# - \"No suitable driver found\" = h2 JAR not in plugins folder\n");
        config.append("# - \"Database not found\" = Check the absolute path above\n");
        
        Files.write(databasePath.resolve("H2_Config.properties"), config.toString().getBytes());
        System.out.println("✅ H2_Config.properties created with absolute path: " + absoluteDbPath);
    }
    
    private static void copyH2Driver(Path databasePath) throws IOException {
        Path sourceH2Jar = Paths.get(PROJECT_BASE, "tools", "jdbc", "h2-2.4.240.jar");
        Path targetH2Jar = databasePath.resolve("h2-2.4.240.jar");
        
        if (Files.exists(sourceH2Jar)) {
            if (Files.exists(targetH2Jar)) {
                try {
                    Files.delete(targetH2Jar);
                } catch (IOException e) {
                    System.out.println("⚠️ Could not delete existing h2 JAR (file in use): " + e.getMessage());
                    System.out.println("✅ h2 JDBC driver already exists at: " + targetH2Jar.toAbsolutePath());
                    return;
                }
            }
            Files.copy(sourceH2Jar, targetH2Jar, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ h2 JDBC driver copied to: " + targetH2Jar.toAbsolutePath());
        } else {
            System.out.println("⚠️ h2 driver not found at: " + sourceH2Jar + " (run 'mvn compile' first)");
        }
    }
    
    private static void generateProtegeSetup(Path outputPath, Path databasePath) throws IOException {
        String absoluteDbPath = databasePath.toAbsolutePath().toString().replace("\\", "/");
        
        StringBuilder content = new StringBuilder();
        
        content.append("# Protégé Setup Guide - Sport Ontology\n\n");
        content.append("*Simple and direct steps to load the sport ontology in Protégé*\n\n");
        
        content.append("## Prerequisites\n");
        content.append("- Protégé 5.5+ installed\n");
        content.append("- H2 JDBC driver (included in database/ folder)\n\n");
        
        content.append("## Step 1: Install H2 Driver\n");
        content.append("1. Copy `database/h2-2.4.240.jar` to your Protégé plugins folder:\n");
        content.append("   - **Windows:** `Protégé_Installation/plugins/`\n");
        content.append("   - **macOS:** `Protégé.app/Contents/Java/plugins/`\n");
        content.append("   - **Linux:** `protege/plugins/`\n");
        content.append("2. Restart Protégé completely\n\n");
        
        content.append("## Step 2: Load Ontology\n");
        content.append("1. Open Protégé\n");
        content.append("2. **File → Open**\n");
        content.append("3. Select: `ontology/sport-ontology.owl`\n");
        content.append("4. Verify 40+ classes are loaded\n\n");
        
        content.append("## Step 3: Start HermiT Reasoner\n");
        content.append("1. **Reasoner → HermiT**\n");
        content.append("2. **Click \"Start reasoner\"**\n");
        content.append("3. Wait for \"Consistent\" status\n");
        content.append("4. Check that TopPlayer and YoungPlayer classes have inferred instances\n\n");
        
        content.append("## Step 4: Setup Database Connection\n");
        content.append("1. **Window → Tabs → DataSource**\n");
        content.append("2. **Create New Datasource:**\n");
        content.append("   - **Name:** Sport_H2_Database\n");
        content.append("   - **Driver:** org.h2.Driver\n");
        content.append("   - **URL:** `jdbc:h2:").append(absoluteDbPath).append("/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`\n");
        content.append("   - **Username:** sa\n");
        content.append("   - **Password:** (leave empty)\n");
        content.append("3. **Test Connection** - should succeed\n");
        content.append("4. **Alternative:** Load settings from `database/H2_Config.properties`\n\n");
        
        content.append("## Step 5: Load OBDA Mappings\n");
        content.append("1. In DataSource tab: **Load mappings**\n");
        content.append("2. Select: `ontology/sport-ontology-mapping.ttl`\n");
        content.append("3. Verify R2RML mappings are loaded\n\n");
        
        content.append("## Step 6: Test SPARQL Queries\n");
        content.append("1. **Window → Tabs → SPARQL Query**\n");
        content.append("2. **File → Load queries**\n");
        content.append("3. Select any `.sparql` file from `queries/sparql/`\n");
        content.append("4. Execute sample queries to verify everything works\n\n");
        
        content.append("## Expected Results\n");
        content.append("- **Teams:** 7 total (H2 database)\n");
        content.append("- **Players:** 12 total (H2 database)\n");
        content.append("- **TopPlayers:** 5 players with market value ≥ 100M\n");
        content.append("- **YoungPlayers:** 4 players with age < 23\n");
        content.append("- **SPARQL queries:** Should return data from H2 database only (excludes ABox individuals)\n\n");
        
        content.append("## Troubleshooting\n");
        content.append("- **\"No suitable driver found\"** → H2 JAR not in plugins folder, restart Protégé\n");
        content.append("- **\"Database not found\"** → Check the absolute path in Step 4\n");
        content.append("- **No query results** → Restart Ontop reasoner in DataSource tab\n");
        content.append("- **Mixed results** → Use namespace filtering in SPARQL queries\n\n");
        
        content.append("## Namespace Separation\n");
        content.append("- **H2 Database:** `http://www.semanticweb.org/sports/ontology#` (SPARQL access)\n");
        content.append("- **ABox Reasoning:** `http://www.semanticweb.org/sports/ontology#ABox_*` (HermiT only)\n");
        content.append("- **SPARQL filtering:** Queries include filters to access H2 data only\n\n");
        
        content.append("## Quick Test Query\n");
        content.append("```sparql\n");
        content.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n");
        content.append("SELECT (COUNT(?team) as ?count) WHERE {\n");
        content.append("    ?team a sports:Team .\n");
        content.append("    FILTER(!STRSTARTS(STR(?team), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        content.append("}\n");
        content.append("# Should return: 7\n");
        content.append("```\n");
        
        Files.write(outputPath.resolve("PROTEGE_SET_UP.md"), content.toString().getBytes());
        System.out.println("✅ PROTEGE_SET_UP.md created with system-specific paths");
    }
    
    private static void generateSeedDataSummary(Path outputPath) throws IOException {
        StringBuilder content = new StringBuilder();
        
        content.append("# Sport Ontology - Seed Data Summary\n\n");
        content.append("*Overview of test data used for reasoning and OWA vs CWA demonstration*\n\n");
        
        // H2 Database section
        content.append("## H2 Database Content (SQL/SPARQL Access)\n\n");
        content.append("**Namespace:** `http://www.semanticweb.org/sports/ontology#`\n\n");
        content.append("### Data Strategy\n");
        content.append("The H2 database contains carefully selected seed data to demonstrate:\n");
        content.append("- **Basic OBDA functionality** (SQL ↔ SPARQL consistency)\n");
        content.append("- **OWA vs CWA differences** (open vs closed world assumptions)\n");
        content.append("- **Reasoning validation** (classification and inference)\n");
        content.append("- **Small enough for manual verification** (results can be checked by hand)\n\n");
        
        // Teams data
        content.append("### Teams (7 total)\n");
        content.append("| ID | Name | Type | Players | Purpose |\n");
        content.append("|----|------|------|---------|----------|\n");
        content.append("| 1 | Manchester City | Senior | 3 | Top-tier team with star players |\n");
        content.append("| 2 | Real Madrid | Senior | 3 | Another top-tier team for comparison |\n");
        content.append("| 3 | Bayern Munich | Senior | 1 | Single star player team |\n");
        content.append("| 4 | PSG | Senior | 0 | Team without current players |\n");
        content.append("| 5 | Barcelona | Senior | 2 | Mix of star and regular players |\n");
        content.append("| 6 | Manchester City U21 | Youth | 1 | Youth team with young player |\n");
        content.append("| 7 | Real Madrid Castilla | Youth | 1 | Another youth team |\n\n");
        
        // Players data
        content.append("### Players (12 total)\n");
        content.append("| Name | Age | Team | Market Value | Classification |\n");
        content.append("|------|-----|------|--------------|----------------|\n");
        content.append("| Erling Haaland | 24 | Manchester City | €180M | TopPlayer |\n");
        content.append("| Kevin De Bruyne | 33 | Manchester City | €85M | Regular |\n");
        content.append("| Ederson Moraes | 31 | Manchester City | €40M | Regular |\n");
        content.append("| Vinicius Junior | 24 | Real Madrid | €150M | TopPlayer |\n");
        content.append("| Jude Bellingham | 21 | Real Madrid | €180M | TopPlayer + YoungPlayer |\n");
        content.append("| Thibaut Courtois | 32 | Real Madrid | €60M | Regular |\n");
        content.append("| Harry Kane | 31 | Bayern Munich | €100M | TopPlayer |\n");
        content.append("| Kylian Mbappe | 26 | Real Madrid | €180M | TopPlayer |\n");
        content.append("| Robert Lewandowski | 36 | Barcelona | €45M | Regular |\n");
        content.append("| Pedri Gonzalez | 22 | Barcelona | €80M | YoungPlayer |\n");
        content.append("| Rico Lewis | 19 | Man City U21 | €15M | YoungPlayer |\n");
        content.append("| Nico Paz | 20 | Real Madrid Castilla | €8M | YoungPlayer |\n\n");
        
        // Reasoning examples
        content.append("### Key Data for Reasoning Tests\n\n");
        content.append("**TopPlayer Classification (Market Value ≥ €100M):**\n");
        content.append("- Expected: 5 players (Haaland, Vinicius, Bellingham, Kane, Mbappe)\n");
        content.append("- Demonstrates: Automatic classification based on market value\n\n");
        
        content.append("**YoungPlayer Classification (Age < 23):**\n");
        content.append("- Expected: 4 players (Bellingham, Pedri, Rico Lewis, Nico Paz)\n");
        content.append("- Demonstrates: Age-based reasoning rules\n\n");
        
        content.append("**Contract Data (OWA vs CWA Demo):**\n");
        content.append("- **SQL (CWA):** 10 active contracts (only explicitly is_active = TRUE)\n");
        content.append("- **SPARQL (OWA):** 13 total contracts (assumes unknown status might be true)\n");
        content.append("- **Purpose:** Shows philosophical difference in data assumptions\n\n");
        
        // ABox section
        content.append("## ABox Content (HermiT Reasoning Only)\n\n");
        content.append("**Namespace:** `http://www.semanticweb.org/sports/ontology#ABox_*`\n\n");
        content.append("### Purpose\n");
        content.append("ABox individuals are embedded in the ontology for pure reasoning validation:\n");
        content.append("- **Independent from H2 database** (separate namespace)\n");
        content.append("- **HermiT reasoning only** (not accessible via SPARQL/OBDA)\n");
        content.append("- **Test complex reasoning scenarios** (multiple inheritance, property chains)\n\n");
        
        content.append("### Example ABox Individuals\n");
        content.append("- `ABox_Erling_Haaland` - TopPlayer with high market value\n");
        content.append("- `ABox_Young_Talent` - YoungPlayer for age-based reasoning\n");
        content.append("- `ABox_Elite_Club` - EliteTeam for complex team classification\n");
        content.append("- **Purpose:** Validate reasoning rules independently from database data\n\n");
        
        // Namespace strategy
        content.append("## Namespace Strategy Implementation\n\n");
        content.append("### SPARQL Filtering (Access H2 Database Only)\n");
        content.append("```sparql\n");
        content.append("# All SPARQL queries include this filter:\n");
        content.append("FILTER(!STRSTARTS(STR(?individual), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        content.append("```\n\n");
        
        content.append("### Benefits of Separation\n");
        content.append("- ✅ **SPARQL/SQL consistency:** Both access same H2 database data\n");
        content.append("- ✅ **Clean comparisons:** SQL and filtered SPARQL return identical counts\n");
        content.append("- ✅ **Independent reasoning:** HermiT can validate complex scenarios\n");
        content.append("- ✅ **Easy debugging:** Issues can be isolated to specific data sources\n\n");
        
        // OWA vs CWA examples
        content.append("## OWA vs CWA Demonstration Examples\n\n");
        content.append("### Example 1: Contract Counting\n");
        content.append("- **SQL Query:** `SELECT COUNT(*) FROM contract WHERE is_active = TRUE;`\n");
        content.append("- **Result:** 10 contracts (only explicitly active)\n");
        content.append("- **SPARQL Query:** Count all contract relationships via OBDA\n");
        content.append("- **Result:** 13 contracts (includes unknown status)\n");
        content.append("- **Demonstrates:** CWA assumes missing = false, OWA assumes missing = unknown\n\n");
        
        content.append("### Example 2: Player Team Assignment\n");
        content.append("- **SQL:** Returns only players with explicit current team assignments\n");
        content.append("- **SPARQL:** May include players with inferred team relationships\n");
        content.append("- **HermiT:** Can infer additional relationships through property chains\n\n");
        
        content.append("### Example 3: Classification Completeness\n");
        content.append("- **SQL:** Only returns players with explicit market_value ≥ 100M\n");
        content.append("- **SPARQL/HermiT:** May classify additional players through inference\n");
        content.append("- **Reasoning:** Automatic TopPlayer classification even with incomplete data\n\n");
        
        content.append("## Seed Data Size Rationale\n\n");
        content.append("**Small enough for manual verification:**\n");
        content.append("- 7 teams → Easy to count and verify\n");
        content.append("- 12 players → Can manually check classifications\n");
        content.append("- 5 TopPlayers → Simple to validate market value rules\n");
        content.append("- 4 YoungPlayers → Quick age verification\n\n");
        
        content.append("**Large enough for meaningful tests:**\n");
        content.append("- Multiple team types (senior/youth)\n");
        content.append("- Various player value ranges\n");
        content.append("- Different age distributions\n");
        content.append("- Complex contract scenarios\n");
        content.append("- Property chain examples (team → league participation)\n\n");
        
        content.append("**Perfect for educational purposes:**\n");
        content.append("- Results can be verified by hand\n");
        content.append("- Shows clear differences between SQL/SPARQL/HermiT\n");
        content.append("- Demonstrates real-world ontology concepts\n");
        content.append("- Enables easy debugging when tests fail\n");
        
        Files.write(outputPath.resolve("SEED_DATA_SUMMARY.md"), content.toString().getBytes());
        System.out.println("✅ SEED_DATA_SUMMARY.md created with detailed data explanation");
}
}