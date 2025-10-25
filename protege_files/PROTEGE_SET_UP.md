# Protégé Setup Guide - Sport Ontology

*Simple and direct steps to load the sport ontology in Protégé*

## Prerequisites
- Protégé 5.5+ installed
- H2 JDBC driver (included in database/ folder)

## Step 1: Install H2 Driver
1. Copy `database/h2-2.4.240.jar` to your Protégé plugins folder:
   - **Windows:** `Protégé_Installation/plugins/`
   - **macOS:** `Protégé.app/Contents/Java/plugins/`
   - **Linux:** `protege/plugins/`
2. Restart Protégé completely

## Step 2: Load Ontology
1. Open Protégé
2. **File → Open**
3. Select: `ontology/sport-ontology.owl`
4. Verify 40+ classes are loaded

## Step 3: Start HermiT Reasoner
1. **Reasoner → HermiT**
2. **Click "Start reasoner"**
3. Wait for "Consistent" status
4. Check that TopPlayer and YoungPlayer classes have inferred instances

## Step 4: Setup Database Connection
1. **Window → Tabs → DataSource**
2. **Create New Datasource:**
   - **Name:** Sport_H2_Database
   - **Driver:** org.h2.Driver
   - **URL:** `jdbc:h2:C:/Users/franc/Desktop/RCR/projects/sport_ontology/protege_files/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`
   - **Username:** sa
   - **Password:** (leave empty)
3. **Test Connection** - should succeed

## Step 5: Load OBDA Mappings
1. In DataSource tab: **Load mappings**
2. Select: `ontology/sport-ontology-mapping.ttl`
3. Verify R2RML mappings are loaded

## Step 6: Test SPARQL Queries
1. **Window → Tabs → SPARQL Query**
2. **File → Load queries**
3. Select any `.sparql` file from `queries/sparql/`
4. Execute sample queries to verify everything works

## Expected Results
- **Teams:** 7 total (H2 database)
- **Players:** 12 total (H2 database)
- **TopPlayers:** 5 players with market value ≥ 100M
- **YoungPlayers:** 4 players with age < 23
- **SPARQL queries:** Should return data from H2 database only (excludes ABox individuals)

## Troubleshooting
- **"No suitable driver found"** → H2 JAR not in plugins folder, restart Protégé
- **"Database not found"** → Check the absolute path in Step 4
- **No query results** → Restart Ontop reasoner in DataSource tab
- **Mixed results** → Use namespace filtering in SPARQL queries

## Namespace Separation
- **H2 Database:** `http://www.semanticweb.org/sports/ontology#` (SPARQL access)
- **ABox Reasoning:** `http://www.semanticweb.org/sports/ontology#ABox_*` (HermiT only)
- **SPARQL filtering:** Queries include filters to access H2 data only

## Quick Test Query
```sparql
PREFIX sports: <http://www.semanticweb.org/sports/ontology#>
SELECT (COUNT(?team) as ?count) WHERE {
    ?team a sports:Team .
    FILTER(!STRSTARTS(STR(?team), "http://www.semanticweb.org/sports/ontology#ABox_"))
}
# Should return: 7
```
