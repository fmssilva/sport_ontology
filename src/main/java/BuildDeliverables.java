import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Complete deliverables builder for Sport Ontology project.
 * Handles database creation, file copying, documentation generation, and SPARQL consolidation.
 * 
 * Usage: java BuildDeliverables [output-directory]
 * Default output: ./deliverables/
 */
public class BuildDeliverables {
    
    private static final String ONTOLOGY_NAMESPACE = "http://www.semanticweb.org/sports/ontology#";
    private static final String ABOX_NAMESPACE = "http://www.semanticweb.org/sports/ontology#ABox_";
    private static final String PROJECT_BASE = System.getProperty("user.dir");
    
    public static void main(String[] args) {
        String outputDir = args.length > 0 ? args[0] : "deliverables";
        buildDeliverables(outputDir);
    }
    
    public static void buildDeliverables(String outputDir) {
        try {
            Path outputPath = Paths.get(outputDir);
            Path ontologyPath = outputPath.resolve("ontology");
            Path databasePath = outputPath.resolve("database");
            
            // Create directory structure
            Files.createDirectories(ontologyPath);
            Files.createDirectories(databasePath);
            
            System.out.println("🎯 Building complete deliverables for Sport Ontology...");
            System.out.println("📁 Output directory: " + outputPath.toAbsolutePath());
            
            // Step 1: Generate H2 Database with seed data
            System.out.println("\n📊 Step 1: Creating H2 database with seed data...");
            createH2Database(databasePath);
            
            // Step 2: Copy ontology files
            System.out.println("\n📋 Step 2: Copying ontology files...");
            copyOntologyFiles(ontologyPath);
            
            // Step 3: Copy SPARQL query files separately
            System.out.println("\n📝 Step 3: Copying SPARQL query files separately...");
            consolidateSparqlQueries(ontologyPath);
            
            // Step 4: Copy H2 JDBC driver for portable setup
            System.out.println("\n🔧 Step 4: Copying h2 JDBC driver for portable setup...");
            copyH2Driver(databasePath);
            
            // Step 5: Create Protégé configuration
            System.out.println("\n⚙️ Step 5: Creating Protégé configuration...");
            createProtegeConfig(ontologyPath, databasePath);
            
            // Step 6: Generate comprehensive documentation
            System.out.println("\n📚 Step 6: Generating comprehensive documentation...");
            generateDocumentation(outputPath, databasePath);
            
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
    
    private static void consolidateSparqlQueries(Path ontologyPath) throws IOException {
        Path queriesDir = Paths.get(PROJECT_BASE, "src", "test", "resources", "queries");
        Path queriesOutputDir = ontologyPath.resolve("queries");
        
        if (!Files.exists(queriesDir)) {
            System.out.println("⚠️ Queries directory not found, creating basic SPARQL files");
            Files.createDirectories(queriesOutputDir);
            createBasicSeparateSparqlFiles(queriesOutputDir);
            return;
        }
        
        // Create queries subdirectory in deliverables
        Files.createDirectories(queriesOutputDir);
        
        // Copy all SPARQL query files separately
        try (var paths = Files.walk(queriesDir)) {
            paths.filter(path -> path.toString().endsWith(".sparql"))
                 .sorted()
                 .forEach(queryFile -> {
                     try {
                         String fileName = queryFile.getFileName().toString();
                         Path targetFile = queriesOutputDir.resolve(fileName);
                         Files.copy(queryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                         System.out.println("✅ Copied SPARQL file: " + fileName);
                     } catch (IOException e) {
                         System.err.println("Warning: Could not copy " + queryFile + ": " + e.getMessage());
                     }
                 });
        }
        
        // Also copy SQL files for reference
        try (var paths = Files.walk(queriesDir)) {
            paths.filter(path -> path.toString().endsWith(".sql"))
                 .sorted()
                 .forEach(queryFile -> {
                     try {
                         String fileName = queryFile.getFileName().toString();
                         Path targetFile = queriesOutputDir.resolve(fileName);
                         Files.copy(queryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                         System.out.println("✅ Copied SQL file: " + fileName);
                     } catch (IOException e) {
                         System.err.println("Warning: Could not copy " + queryFile + ": " + e.getMessage());
                     }
                 });
        }
        
        System.out.println("✅ All query files copied separately to queries/ directory");
    }
    
    private static void createBasicSeparateSparqlFiles(Path queriesOutputDir) throws IOException {
        // Create individual SPARQL files with meaningful names
        
        // File 1: Basic queries
        StringBuilder basicQueries = new StringBuilder();
        basicQueries.append("# Sports Ontology - Basic Count Queries\n");
        basicQueries.append("# Load this file in Protégé: Window → Tabs → SPARQL Query → File → Load Queries\n\n");
        basicQueries.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n\n");
        basicQueries.append("# Test 1: Count all teams\n");
        basicQueries.append("SELECT (COUNT(?team) as ?count) WHERE {\n");
        basicQueries.append("    ?team a sports:Team\n");
        basicQueries.append("}\n");
        Files.write(queriesOutputDir.resolve("basic_queries.sparql"), basicQueries.toString().getBytes());
        
        // File 2: Player queries
        StringBuilder playerQueries = new StringBuilder();
        playerQueries.append("# Sports Ontology - Player Queries\n\n");
        playerQueries.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n\n");
        playerQueries.append("# List all players with their teams\n");
        playerQueries.append("SELECT ?playerName ?teamName WHERE {\n");
        playerQueries.append("    ?player a sports:Player ;\n");
        playerQueries.append("            sports:hasName ?playerName ;\n");
        playerQueries.append("            sports:playsFor ?team .\n");
        playerQueries.append("    ?team sports:hasName ?teamName .\n");
        playerQueries.append("}\n");
        playerQueries.append("ORDER BY ?teamName ?playerName\n");
        Files.write(queriesOutputDir.resolve("player_queries.sparql"), playerQueries.toString().getBytes());
        
        // File 3: Reasoning queries
        StringBuilder reasoningQueries = new StringBuilder();
        reasoningQueries.append("# Sports Ontology - Reasoning Queries\n\n");
        reasoningQueries.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n\n");
        reasoningQueries.append("# Find TopPlayers (market value >= 100M)\n");
        reasoningQueries.append("SELECT ?name ?value WHERE {\n");
        reasoningQueries.append("    ?player a sports:TopPlayer ;\n");
        reasoningQueries.append("            sports:hasName ?name ;\n");
        reasoningQueries.append("            sports:hasMarketValue ?value .\n");
        reasoningQueries.append("}\n");
        reasoningQueries.append("ORDER BY DESC(?value)\n");
        Files.write(queriesOutputDir.resolve("reasoning_queries.sparql"), reasoningQueries.toString().getBytes());
        
        System.out.println("✅ Basic SPARQL query files created separately");
    }
    
    private static void createProtegeConfig(Path ontologyPath, Path databasePath) throws IOException {
        String absoluteDbPath = databasePath.toAbsolutePath().toString().replace("\\", "/");
        
        StringBuilder config = new StringBuilder();
        config.append("jdbc.driver=org.h2.Driver\n");
        config.append("jdbc.password=\n");
        config.append("jdbc.url=jdbc:h2:").append(absoluteDbPath).append("/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true\n");
        config.append("jdbc.user=sa\n\n");
        config.append("# Protégé OBDA Configuration - Auto-generated\n");
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
        
        Files.write(ontologyPath.resolve("sport-ontology-protege.properties"), config.toString().getBytes());
        System.out.println("✅ Protégé configuration created with absolute path: " + absoluteDbPath);
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
    
    private static void generateDocumentation(Path outputPath, Path databasePath) throws IOException {
        String absoluteDbPath = databasePath.toAbsolutePath().toString().replace("\\", "/");
        
        // Generate comprehensive seed data reference
        generateSeedDataReference(outputPath);
        
        // Generate Protégé step-by-step guide with correct paths
        generateProtegeGuide(outputPath, absoluteDbPath);
        
        // Generate main README
        generateMainReadme(outputPath, absoluteDbPath);
        
        System.out.println("✅ Documentation generated with automatic path resolution");
    }
    
    private static void generateSeedDataReference(Path outputPath) throws IOException {
        StringBuilder content = new StringBuilder();
        
        content.append("# Sport Ontology - Seed Data Reference\n\n");
        content.append("*Generated automatically by BuildDeliverables.java on ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("*\n\n");
        
        // H2 Database section
        content.append("## H2 Database Content (SQL/SPARQL Data)\n\n");
        content.append("**Namespace:** `").append(ONTOLOGY_NAMESPACE).append("`\n\n");
        
        // Teams data - extracted from CreateH2Database
        content.append("### Teams (7 total)\n");
        content.append("| ID | Name | City | Type | Stadium Capacity |\n");
        content.append("|----|------|------|------|------------------|\n");
        content.append("| 1 | Manchester City | Manchester | SeniorTeam | 55,000 |\n");
        content.append("| 2 | Real Madrid | Madrid | SeniorTeam | 81,000 |\n");
        content.append("| 3 | Bayern Munich | Munich | SeniorTeam | 75,000 |\n");
        content.append("| 4 | Paris Saint-Germain | Paris | SeniorTeam | 48,000 |\n");
        content.append("| 5 | Barcelona | Barcelona | SeniorTeam | 99,000 |\n");
        content.append("| 6 | Manchester City U21 | Manchester | YouthTeam | 7,000 |\n");
        content.append("| 7 | Real Madrid Castilla | Madrid | YouthTeam | 6,000 |\n\n");
        
        // Players data
        content.append("### Players (12 total)\n");
        content.append("| Name | Age | Nationality | Current Team | Position | Market Value |\n");
        content.append("|------|-----|-------------|--------------|----------|---------------|\n");
        
        // Add player data from CreateH2Database knowledge
        String[][] players = {
            {"Erling Haaland", "24", "Norway", "Manchester City", "Forward", "€180M"},
            {"Kevin De Bruyne", "33", "Belgium", "Manchester City", "Midfielder", "€85M"},
            {"Ederson Moraes", "31", "Brazil", "Manchester City", "Goalkeeper", "€40M"},
            {"Vinicius Junior", "24", "Brazil", "Real Madrid", "Forward", "€150M"},
            {"Jude Bellingham", "21", "England", "Real Madrid", "Midfielder", "€180M"},
            {"Thibaut Courtois", "32", "Belgium", "Real Madrid", "Goalkeeper", "€60M"},
            {"Harry Kane", "31", "England", "Bayern Munich", "Forward", "€100M"},
            {"Kylian Mbappe", "26", "France", "Real Madrid", "Forward", "€180M"},
            {"Robert Lewandowski", "36", "Poland", "Barcelona", "Forward", "€45M"},
            {"Pedri Gonzalez", "22", "Spain", "Barcelona", "Midfielder", "€80M"},
            {"Rico Lewis", "19", "England", "Manchester City U21", "Defender", "€15M"},
            {"Nico Paz", "20", "Argentina", "Real Madrid Castilla", "Midfielder", "€8M"}
        };
        
        for (String[] player : players) {
            content.append("| ").append(String.join(" | ", player)).append(" |\n");
        }
        content.append("\n");
        
        // Coaches data
        content.append("### Coaches (5 total)\n");
        content.append("| Name | Age | Nationality | Team | Role |\n");
        content.append("|------|-----|-------------|------|------|\n");
        content.append("| Pep Guardiola | 53 | Spain | Manchester City | Head Coach |\n");
        content.append("| Carlo Ancelotti | 65 | Italy | Real Madrid | Head Coach |\n");
        content.append("| Thomas Tuchel | 51 | Germany | Bayern Munich | Head Coach |\n");
        content.append("| Luis Enrique | 54 | Spain | PSG | Head Coach |\n");
        content.append("| Xavi Hernandez | 44 | Spain | Barcelona | Head Coach |\n\n");
        
        // Reasoning classifications
        content.append("### Key Data for Reasoning Tests\n\n");
        content.append("**TopPlayer Classifications (Market Value ≥ €100M):**\n");
        content.append("- Kylian Mbappe: €180M (Real Madrid)\n");
        content.append("- Erling Haaland: €180M (Manchester City)\n");
        content.append("- Jude Bellingham: €180M (Real Madrid)\n");
        content.append("- Vinicius Junior: €150M (Real Madrid)\n");
        content.append("- Harry Kane: €100M (Bayern Munich)\n\n");
        
        content.append("**YoungPlayer Classifications (Age < 23):**\n");
        content.append("- Rico Lewis: 19 years (Manchester City U21)\n");
        content.append("- Nico Paz: 20 years (Real Madrid Castilla)\n");
        content.append("- Jude Bellingham: 21 years (Real Madrid)\n");
        content.append("- Pedri Gonzalez: 22 years (Barcelona)\n\n");
        
        content.append("**Contracts (13 total):**\n");
        content.append("- 10 Active contracts (is_active = TRUE)\n");
        content.append("- 3 Inactive contracts (loan ended, transfers, etc.)\n\n");
        
        // OWA vs CWA examples
        content.append("## OWA vs CWA Examples\n\n");
        content.append("### Contract Count Difference\n");
        content.append("- **SQL (CWA):** 10 active contracts (only explicitly is_active = TRUE)\n");
        content.append("- **SPARQL (OWA):** 13 total contracts (assumes unknown active status might be true)\n\n");
        
        content.append("### Player Classification\n");
        content.append("- **SQL:** Returns only players with explicit market_value data\n");
        content.append("- **SPARQL/HermiT:** May infer additional classifications based on reasoning rules\n\n");
        
        // Namespace strategy
        content.append("## Namespace Strategy\n\n");
        content.append("### H2/OBDA Namespace (SQL + SPARQL)\n");
        content.append("```\n").append(ONTOLOGY_NAMESPACE).append("\n```\n");
        content.append("**Usage:** Real data from H2 database via OBDA mappings\n\n");
        
        content.append("### ABox Namespace (HermiT Reasoning)\n");
        content.append("```\n").append(ABOX_NAMESPACE).append("\n```\n");
        content.append("**Usage:** Additional test individuals for reasoning demonstrations\n\n");
        
        content.append("### SPARQL Filtering\n");
        content.append("```sparql\n");
        content.append("# Exclude ABox individuals from OBDA queries\n");
        content.append("FILTER(!STRSTARTS(STR(?individual), \"").append(ABOX_NAMESPACE).append("\"))\n");
        content.append("```\n");
        
        Files.write(outputPath.resolve("SEED_DATA_REFERENCE.md"), content.toString().getBytes());
    }
    
    private static void generateProtegeGuide(Path outputPath, String absoluteDbPath) throws IOException {
        StringBuilder content = new StringBuilder();
        
        content.append("# Protégé Step-by-Step Guide\n\n");
        content.append("*Generated automatically by BuildDeliverables.java - Portable setup for any system*\n\n");
        
        content.append("## Automated Setup Instructions\n\n");
        
        content.append("### Step 1: Load Ontology\n");
        content.append("1. Open Protégé 5.5+\n");
        content.append("2. **File → Open**\n");
        content.append("3. Select: `ontology/sport-ontology.owl`\n");
        content.append("4. ✅ Verify: 40+ classes loaded in hierarchy\n\n");
        
        content.append("### Step 2: Start HermiT Reasoner\n");
        content.append("1. **Reasoner Tab → HermiT**\n");
        content.append("2. **Click \"Start reasoner\"**\n");
        content.append("3. ✅ Wait for: \"Consistent\" status\n");
        content.append("4. ✅ Check: TopPlayer/YoungPlayer have inferred members\n\n");
        
        content.append("### Step 3: Install h2 JDBC Driver in Protégé\n");
        content.append("**⚠️ IMPORTANT: Protégé needs the h2 driver to connect to the database**\n\n");
        content.append("1. **Get h2 Driver (Included):**\n");
        content.append("   - **EASY:** Use the included `database/h2-2.4.240.jar` (portable)\n");
        content.append("   - **Alternative:** Download from https://repo1.maven.org/maven2/com/h2database/h2/2.4.240/\n\n");
        content.append("2. **Install in Protégé:**\n");
        content.append("   - **Windows:** Copy `database/h2-2.4.240.jar` to `Protégé_Installation/plugins/`\n");
        content.append("   - **macOS:** Copy `database/h2-2.4.240.jar` to `Protégé.app/Contents/Java/plugins/`\n");
        content.append("   - **Linux:** Copy `database/h2-2.4.240.jar` to `protege/plugins/`\n\n");
        content.append("3. **Restart Protégé** completely\n\n");
        content.append("### Step 4: Setup OBDA Database Connection\n");
        content.append("1. **Window → Tabs → DataSource**\n");
        content.append("2. **Create New Datasource:**\n");
        content.append("   - Name: `Sport_H2_Database`\n");
        content.append("   - Driver: `org.h2.Driver` (should appear after driver installation)\n");
        content.append("   - URL: `jdbc:h2:").append(absoluteDbPath).append("/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`\n");
        content.append("   - Username: `sa`\n");
        content.append("   - Password: (empty)\n\n");
        content.append("**✅ Database path is automatically configured for this system**\n\n");
        content.append("### ❗ Troubleshooting Connection Issues\n");
        content.append("- **\"No suitable driver found\":\" h2 JAR not in Protégé plugins folder\n");
        content.append("- **\"Database not found\":\" Check the absolute path is correct\n");
        content.append("- **\"Cannot connect\":\" Ensure database file exists: `").append(absoluteDbPath).append("/sports-deliverable-db.mv.db`\n\n");
        
        content.append("### ❗ Troubleshooting Ontology Inconsistencies\n");
        content.append("**Common Issue**: `hasJerseyNumber` inconsistency detected\n\n");
        content.append("**Cause**: Jersey number constraints in ontology vs database data\n");
        content.append("- **Ontology constraint**: `hasJerseyNumber Range: integer[>= 1 , <= 99]`\n");
        content.append("- **Database data**: May contain jersey numbers outside this range\n\n");
        content.append("**Solutions**:\n");
        content.append("1. **Check HermiT reasoning**: Reasoner → HermiT → Start reasoner\n");
        content.append("2. **Review inconsistent individuals**: Look for players with jersey numbers < 1 or > 99\n");
        content.append("3. **Fix data**: Update database or relax ontology constraints if needed\n");
        content.append("4. **Expected behavior**: Some database values may violate ontology constraints\n\n");
        
        content.append("### Step 5: Load OBDA Mappings\n");
        content.append("1. **In DataSource tab: Load mappings**\n");
        content.append("2. **Select:** `ontology/sport-ontology-mapping.ttl`\n");
        content.append("3. ✅ Verify: R2RML mappings loaded\n\n");
        
        content.append("### Step 6: Test SPARQL Queries\n");
        content.append("1. **Window → Tabs → SPARQL Query**\n");
        content.append("2. **File → Load queries**\n");
        content.append("3. **Select any .sparql file from:** `ontology/queries/`\n");
        content.append("4. **Execute sample queries:**\n\n");
        
        content.append("```sparql\n");
        content.append("# Test 1: Count teams (should return 7)\n");
        content.append("PREFIX sports: <").append(ONTOLOGY_NAMESPACE).append(">\n");
        content.append("SELECT (COUNT(?team) as ?count) WHERE {\n");
        content.append("    ?team a sports:Team\n");
        content.append("}\n\n");
        
        content.append("# Test 2: Find TopPlayers (should return 5)\n");
        content.append("PREFIX : <").append(ONTOLOGY_NAMESPACE).append(">\n");
        content.append("SELECT ?name ?value WHERE {\n");
        content.append("    ?player a :TopPlayer ;\n");
        content.append("            :hasName ?name ;\n");
        content.append("            :hasMarketValue ?value .\n");
        content.append("}\n");
        content.append("ORDER BY DESC(?value)\n");
        content.append("```\n\n");
        
        content.append("### Step 7: Test Namespace Separation (HermiT vs SPARQL)\n");
        content.append("**Important**: Verify that HermiT reasoning and SPARQL queries use different data sources:\n\n");
        content.append("1. **Switch to HermiT Reasoner**:\n");
        content.append("   - Reasoner → HermiT → Start reasoner\n");
        content.append("   - Navigate to TopPlayer class\n");
        content.append("   - Check inferred instances (should include ABox_Erling_Haaland, ABox_Kylian_Mbappe, etc.)\n\n");
        content.append("2. **Test SPARQL Query (OBDA data only)**:\n");
        content.append("```sparql\n");
        content.append("PREFIX sports: <http://www.semanticweb.org/sports/ontology#>\n");
        content.append("SELECT ?player ?name WHERE {\n");
        content.append("    ?player a sports:TopPlayer ;\n");
        content.append("            sports:hasName ?name .\n");
        content.append("    # This should NOT include ABox_ individuals\n");
        content.append("    FILTER(!STRSTARTS(STR(?player), \"http://www.semanticweb.org/sports/ontology#ABox_\"))\n");
        content.append("}\n");
        content.append("```\n\n");
        content.append("3. **Expected Results**:\n");
        content.append("   - **HermiT**: Shows both H2 database players AND ABox reasoning examples (prefixed with \"ABox_\")\n");
        content.append("   - **SPARQL**: Shows ONLY H2 database players (excludes ABox_ individuals)\n");
        content.append("   - **Namespace separation working**: Different results confirm proper isolation\n\n");
        
        content.append("### Step 8: Validate System\n");
        content.append("1. **Check ontology consistency** ✅\n");
        content.append("2. **Verify database connection** ✅\n");
        content.append("3. **Test SPARQL query execution** ✅\n");
        content.append("4. **Compare reasoning results** ✅\n\n");
        
        content.append("## Expected Results Summary\n\n");
        content.append("- **Teams:** 7 total (5 senior, 2 youth)\n");
        content.append("- **Players:** 12 total (5 TopPlayers, 4 YoungPlayers)\n");
        content.append("- **Coaches:** 5 total\n");
        content.append("- **Contracts:** 13 total (10 active via SQL, 13 via SPARQL - OWA demo)\n");
        content.append("- **Reasoning:** TopPlayer and YoungPlayer classifications working\n");
        content.append("- **OBDA:** SQL-SPARQL consistency on basic queries\n\n");
        
        content.append("## Database Connection Details\n");
        content.append("- **Full Database Path:** `").append(absoluteDbPath).append("/sports-deliverable-db`\n");
        content.append("- **JDBC URL:** `jdbc:h2:").append(absoluteDbPath).append("/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`\n");
        content.append("- **Configuration loaded from:** `ontology/sport-ontology-protege.properties`\n");
        
        Files.write(outputPath.resolve("PROTEGE_STEP_BY_STEP.md"), content.toString().getBytes());
    }
    
    private static void generateMainReadme(Path outputPath, String absoluteDbPath) throws IOException {
        StringBuilder content = new StringBuilder();
        
        content.append("# Sport Ontology - Deliverable Files\n\n");
        content.append("*Generated automatically by BuildDeliverables.java on ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("*\n\n");
        
        content.append("## Overview\n");
        content.append("Complete OBDA system with H2 database, rich OWL ontology, and Protégé integration.\n");
        content.append("All paths are automatically configured for your system.\n\n");
        
        content.append("## Quick Start\n");
        content.append("1. **Open Protégé** and load `ontology/sport-ontology.owl`\n");
        content.append("2. **Follow:** `PROTEGE_STEP_BY_STEP.md` for complete setup\n");
        content.append("3. **Reference:** `SEED_DATA_REFERENCE.md` for data understanding\n\n");
        
        content.append("## Contents\n\n");
        content.append("### Database Files (database/)\n");
        content.append("- `sports-deliverable-db.mv.db` - H2 database with 7 teams, 12 players, 5 coaches\n");
        content.append("- `h2-2.4.240.jar` - h2 JDBC driver for Protégé (portable)\n");
        content.append("- **Full path:** `").append(absoluteDbPath).append("/sports-deliverable-db`\n\n");
        
        content.append("### Ontology Files (ontology/)\n");
        content.append("- `sport-ontology.owl` - Complete OWL ontology (40+ classes)\n");
        content.append("- `sport-ontology-mapping.ttl` - OBDA mappings (SQL→RDF)\n");
        content.append("- `sport-ontology-protege.properties` - Database connection config (auto-generated paths)\n\n");
        content.append("### Query Files (ontology/queries/)\n");
        content.append("- Individual SPARQL and SQL files organized by domain\n");
        content.append("- `*_queries.sparql` - SPARQL query files for Protégé testing\n");
        content.append("- `*_queries.sql` - SQL reference files for comparison\n");
        content.append("- Load SPARQL files individually in Protégé: Window → Tabs → SPARQL Query → File → Load Queries\n\n");
        
        content.append("### Documentation Files\n");
        content.append("- `PROTEGE_STEP_BY_STEP.md` - Complete Protégé setup guide with system-specific paths\n");
        content.append("- `SEED_DATA_REFERENCE.md` - Database content and reasoning examples\n\n");
        
        content.append("## Key Features Demonstrated\n");
        content.append("- ✅ **HermiT Reasoning:** TopPlayer/YoungPlayer classification\n");
        content.append("- ✅ **OBDA Integration:** SQL-SPARQL query translation\n");
        content.append("- ✅ **OWA vs CWA:** Contract counting differences\n");
        content.append("- ✅ **Complex Ontology:** 40+ classes with reasoning rules\n");
        content.append("- ✅ **Namespace Separation:** H2 data vs ABox reasoning data\n");
        content.append("- ✅ **Automatic Path Resolution:** Works on any system without manual configuration\n\n");
        
        content.append("## Database Schema Summary\n");
        content.append("```\n");
        content.append("Teams (7): Manchester City, Real Madrid, Bayern Munich, PSG, Barcelona + 2 youth\n");
        content.append("Players (12): Haaland, Mbappe, Bellingham, Vinicius Jr, Kane + 7 others\n");
        content.append("Coaches (5): Guardiola, Ancelotti, Tuchel, Luis Enrique, Xavi\n");
        content.append("Contracts (13): 10 active, 3 historical/inactive\n");
        content.append("```\n\n");
        
        content.append("## System Information\n");
        content.append("- **Build date:** ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        content.append("- **Database path:** `").append(absoluteDbPath).append("`\n");
        content.append("- **Java version:** ").append(System.getProperty("java.version")).append("\n");
        content.append("- **OS:** ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n\n");
        
        content.append("## ⚠️ Important Setup Requirements\n");
        content.append("- **h2 JDBC Driver:** Must be installed in Protégé plugins folder\n");
        content.append("- **Driver Location:** `database/h2-2.4.240.jar` (included for portability)\n");
        content.append("- **Installation:** Copy JAR to Protégé plugins folder and restart\n\n");
        content.append("## Support\n");
        content.append("- All files are portable and work on any system with Java 11+ and Protégé 5.5+\n");
        content.append("- Database paths are automatically configured for your system\n");
        content.append("- SPARQL queries are pre-loaded and ready to execute\n");
        content.append("- To rebuild: `mvn exec:exec@deliverables` or `java BuildDeliverables`\n");
        content.append("- **Troubleshooting:** See `PROTEGE_STEP_BY_STEP.md` for H2 driver issues\n");
        
        Files.write(outputPath.resolve("README.md"), content.toString().getBytes());
    }
}