# Protégé Step-by-Step Guide

*Generated automatically by BuildDeliverables.java - Portable setup for any system*

## Automated Setup Instructions

### Step 1: Load Ontology
1. Open Protégé 5.5+
2. **File → Open**
3. Select: `ontology/sport-ontology.owl`
4. ✅ Verify: 40+ classes loaded in hierarchy

### Step 2: Start HermiT Reasoner
1. **Reasoner Tab → HermiT**
2. **Click "Start reasoner"**
3. ✅ Wait for: "Consistent" status
4. ✅ Check: TopPlayer/YoungPlayer have inferred members

### Step 3: Install h2 JDBC Driver in Protégé
**⚠️ IMPORTANT: Protégé needs the h2 driver to connect to the database**

1. **Get h2 Driver (Included):**
   - **EASY:** Use the included `database/h2-2.4.240.jar` (portable)
   - **Alternative:** Download from https://repo1.maven.org/maven2/com/h2database/h2/2.4.240/

2. **Install in Protégé:**
   - **Windows:** Copy `database/h2-2.4.240.jar` to `Protégé_Installation/plugins/`
   - **macOS:** Copy `database/h2-2.4.240.jar` to `Protégé.app/Contents/Java/plugins/`
   - **Linux:** Copy `database/h2-2.4.240.jar` to `protege/plugins/`

3. **Restart Protégé** completely

### Step 4: Setup OBDA Database Connection
1. **Window → Tabs → DataSource**
2. **Create New Datasource:**
   - Name: `Sport_H2_Database`
   - Driver: `org.h2.Driver` (should appear after driver installation)
   - URL: `jdbc:h2:C:/Users/franc/Desktop/RCR/projects/sport_ontology/deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`
   - Username: `sa`
   - Password: (empty)

**✅ Database path is automatically configured for this system**

### ❗ Troubleshooting Connection Issues
- **"No suitable driver found":" h2 JAR not in Protégé plugins folder
- **"Database not found":" Check the absolute path is correct
- **"Cannot connect":" Ensure database file exists: `C:/Users/franc/Desktop/RCR/projects/sport_ontology/deliverables/database/sports-deliverable-db.mv.db`

### ❗ Troubleshooting Ontology Inconsistencies
**Common Issue**: `hasJerseyNumber` inconsistency detected

**Cause**: Jersey number constraints in ontology vs database data
- **Ontology constraint**: `hasJerseyNumber Range: integer[>= 1 , <= 99]`
- **Database data**: May contain jersey numbers outside this range

**Solutions**:
1. **Check HermiT reasoning**: Reasoner → HermiT → Start reasoner
2. **Review inconsistent individuals**: Look for players with jersey numbers < 1 or > 99
3. **Fix data**: Update database or relax ontology constraints if needed
4. **Expected behavior**: Some database values may violate ontology constraints

### Step 5: Load OBDA Mappings
1. **In DataSource tab: Load mappings**
2. **Select:** `ontology/sport-ontology-mapping.ttl`
3. ✅ Verify: R2RML mappings loaded

### Step 6: Test SPARQL Queries
1. **Window → Tabs → SPARQL Query**
2. **File → Load queries**
3. **Select any .sparql file from:** `ontology/queries/`
4. **Execute sample queries:**

```sparql
# Test 1: Count teams (should return 7)
PREFIX sports: <http://www.semanticweb.org/sports/ontology#>
SELECT (COUNT(?team) as ?count) WHERE {
    ?team a sports:Team
}

# Test 2: Find TopPlayers (should return 5)
PREFIX : <http://www.semanticweb.org/sports/ontology#>
SELECT ?name ?value WHERE {
    ?player a :TopPlayer ;
            :hasName ?name ;
            :hasMarketValue ?value .
}
ORDER BY DESC(?value)
```

### Step 7: Test Namespace Separation (HermiT vs SPARQL)
**Important**: Verify that HermiT reasoning and SPARQL queries use different data sources:

1. **Switch to HermiT Reasoner**:
   - Reasoner → HermiT → Start reasoner
   - Navigate to TopPlayer class
   - Check inferred instances (should include ABox_Erling_Haaland, ABox_Kylian_Mbappe, etc.)

2. **Test SPARQL Query (OBDA data only)**:
```sparql
PREFIX sports: <http://www.semanticweb.org/sports/ontology#>
SELECT ?player ?name WHERE {
    ?player a sports:TopPlayer ;
            sports:hasName ?name .
    # This should NOT include ABox_ individuals
    FILTER(!STRSTARTS(STR(?player), "http://www.semanticweb.org/sports/ontology#ABox_"))
}
```

3. **Expected Results**:
   - **HermiT**: Shows both H2 database players AND ABox reasoning examples (prefixed with "ABox_")
   - **SPARQL**: Shows ONLY H2 database players (excludes ABox_ individuals)
   - **Namespace separation working**: Different results confirm proper isolation

### Step 8: Validate System
1. **Check ontology consistency** ✅
2. **Verify database connection** ✅
3. **Test SPARQL query execution** ✅
4. **Compare reasoning results** ✅

## Expected Results Summary

- **Teams:** 7 total (5 senior, 2 youth)
- **Players:** 12 total (5 TopPlayers, 4 YoungPlayers)
- **Coaches:** 5 total
- **Contracts:** 13 total (10 active via SQL, 13 via SPARQL - OWA demo)
- **Reasoning:** TopPlayer and YoungPlayer classifications working
- **OBDA:** SQL-SPARQL consistency on basic queries

## Database Connection Details
- **Full Database Path:** `C:/Users/franc/Desktop/RCR/projects/sport_ontology/deliverables/database/sports-deliverable-db`
- **JDBC URL:** `jdbc:h2:C:/Users/franc/Desktop/RCR/projects/sport_ontology/deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`
- **Configuration loaded from:** `ontology/sport-ontology-protege.properties`
