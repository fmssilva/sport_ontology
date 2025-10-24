# Protégé Usage Guide - Sport Ontology OBDA System

## Overview
This guide provides step-by-step instructions for using the Sport Ontology in Protégé with full OBDA (Ontology-Based Data Access) capabilities, including HermiT reasoning and SPARQL queries.

## Prerequisites
- Protégé 5.5+ with Ontop plugin installed
- Java 11+ installed
- Generated deliverable files (from `mvn exec:exec@deliverables`)

## Quick Start Guide

### 1. Load the Ontology in Protégé

1. **Open Protégé**
2. **File → Open** 
3. Navigate to: `deliverables/ontology/sport-ontology.owl`
4. **Select and Open**

**Expected Result**: Ontology loads with 40+ classes in hierarchy (Player, Coach, Team, etc.)

### 2. Verify Ontology Structure

**Classes Tab - Check these key classes:**
- `Person` (top-level)
  - `Player` 
    - `TopPlayer` (inferred: market value ≥ 100M€)
    - `YoungPlayer` (inferred: age < 23)
    - `ExperiencedPlayer`
  - `Coach`
    - `TopCoach`
- `Team`
  - `EliteTeam`
- `Contract`, `Position`, `League`

**Object Properties Tab:**
- `playsFor`, `hasCoach`, `hasPlayer`
- `hasContract`, `contractWith`
- `playsPosition`, `canPlayPosition`

**Data Properties Tab:**
- `hasName`, `hasAge`, `hasMarketValue`
- `hasSalary`, `hasStartDate`, `hasEndDate`

### 3. Run HermiT Reasoner

1. **Reasoner Tab → HermiT**
2. **Click "Start reasoner"**
3. **Wait for consistency check** (should show "Consistent")

**Expected Results:**
- Consistency: ✅ Consistent
- Inferred classes appear with computed members
- Individual classifications based on data values

**Verify Reasoning:**
- Go to **Classes → TopPlayer** → Check members
- Should show players with market value ≥ 100M€
- Go to **Classes → YoungPlayer** → Check members  
- Should show players with age < 23

### 4. Setup OBDA (Database Connection)

#### 4.1 Install Ontop Plugin (if not installed)
1. **File → Check for plugins**
2. **Search: "Ontop"**
3. **Install Ontop plugin**
4. **Restart Protégé**

#### 4.2 Configure Database Connection
1. **Window → Tabs → DataSource**
2. **Click "+" to create new datasource**
3. **Connection Settings:**
   - **Name:** `Sport_Ontology_H2_DB`
   - **Driver:** `H2 Database Engine` 
   - **URL:** `jdbc:h2:FULL_ABSOLUTE_PATH/deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`
   - **Username:** `sa`
   - **Password:** (leave empty)

**⚠️ IMPORTANT:** Replace `FULL_ABSOLUTE_PATH` with your actual path:
- **Windows Example:** `jdbc:h2:C:/Users/YourName/Desktop/sport_ontology/deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`
- **Mac/Linux Example:** `jdbc:h2:/Users/YourName/Desktop/sport_ontology/deliverables/database/sports-deliverable-db;DATABASE_TO_UPPER=true;CASE_INSENSITIVE_IDENTIFIERS=true`

#### 4.3 Test Database Connection
1. **Click "Test connection"**
2. **Expected:** ✅ "Connection successful"

#### 4.4 Load OBDA Mappings
1. **In DataSource tab, click "Load mappings"**
2. **Select:** `deliverables/ontology/sport-ontology-mapping.ttl`
3. **Click "Load"**

**Expected Result:** Mappings loaded successfully with R2RML triplemaps

### 5. Run SPARQL Queries

#### 5.1 Load Query File
1. **Window → Tabs → SPARQL Query**
2. **File → Load queries**
3. **Select:** `deliverables/ontology/sports-tests.q`
4. **All queries load in dropdown**

#### 5.2 Execute Test Queries

**Basic Integrity Tests:**
```sparql
# Total Teams Count
PREFIX sports: <http://www.semanticweb.org/sports/ontology#>
SELECT (COUNT(?team) as ?count) WHERE {
    ?team a sports:Team
}
```
**Expected Result:** 7 teams

**Reasoning Tests:**
```sparql
# Top Players (Market Value >= 100M)
PREFIX : <http://www.semanticweb.org/sports/ontology#>
SELECT ?playerName ?marketValue WHERE {
    ?player a :TopPlayer ;
            :hasName ?playerName ;
            :hasMarketValue ?marketValue .
}
ORDER BY DESC(?marketValue)
```
**Expected Result:** Players with market value ≥ 100M€

### 6. Validate System Integration

#### 6.1 Check Consistency
1. **Reasoner Tab → "Check consistency"**
2. **Expected:** ✅ Consistent ontology

#### 6.2 Run Ontop Reasoner
1. **DataSource Tab → "Start Ontop reasoner"**
2. **Expected:** OBDA system ready for queries

#### 6.3 Cross-Validate Results
- **Compare SPARQL results with database content**
- **Verify reasoning inferences match expectations**
- **Check OWA vs CWA behaviors**

## Database Schema Reference

The H2 database contains the following structure:

### Tables and Data
- **7 teams** (5 senior: Manchester City, Real Madrid, Bayern Munich, PSG, Arsenal + 2 youth teams)
- **17 people** (12 players, 5 coaches)
- **13 active player roles** with market values and positions
- **8 coach roles** (including historical data)
- **13 contracts** with salary and date information

### Key Individuals for Testing

**Top Players (Market Value ≥ 100M€):**
- Kylian Mbappé: 180M€
- Erling Haaland: 150M€  
- Vinicius Junior: 120M€
- Jude Bellingham: 100M€

**Young Players (Age < 23):**
- Jude Bellingham: 21 years
- Rico Lewis: 19 years
- Nico Paz: 20 years

**Teams with Rich Data:**
- Manchester City: 3 players, Pep Guardiola (coach)
- Real Madrid: 3 players, Carlo Ancelotti (coach)
- Bayern Munich: 2 players, Thomas Tuchel (coach)

## Namespaces and Data Separation

### H2 Database Data (for SQL/SPARQL)
- **Namespace:** `http://www.semanticweb.org/sports/ontology#`
- **Usage:** Direct database mappings via Ontop
- **Scope:** Production data for OBDA queries

### ABox Data (for HermiT Reasoning)
- **Namespace:** `http://www.semanticweb.org/sports/ontology#ABox_`
- **Usage:** Additional test individuals for reasoning
- **Scope:** Reasoning-only data, filtered out from SPARQL queries

**Filtering in SPARQL:**
```sparql
# This query excludes ABox-only individuals
SELECT ?player ?name WHERE {
    ?player a :Player ;
            :hasName ?name .
    FILTER(!STRSTARTS(STR(?player), "http://www.semanticweb.org/sports/ontology#ABox_"))
}
```

## Troubleshooting

### Common Issues

**1. Database Connection Failed**
- **Check:** Absolute path in JDBC URL
- **Check:** File `sports-deliverable-db.mv.db` exists
- **Solution:** Use forward slashes `/` even on Windows

**2. No Query Results**
- **Check:** OBDA mappings loaded correctly
- **Check:** Ontop reasoner started
- **Solution:** Restart Ontop reasoner in DataSource tab

**3. Reasoner Inconsistency**
- **Check:** Ontology file loaded correctly
- **Check:** No syntax errors in OWL file
- **Solution:** Reload ontology and restart reasoner

**4. Missing Inferred Classifications**
- **Check:** HermiT reasoner running
- **Check:** Data individuals have required property values
- **Solution:** Verify data completeness in database

### Performance Tips

1. **Start HermiT reasoner first** (for classification)
2. **Then start Ontop reasoner** (for OBDA queries)
3. **Use simple queries first** to verify connection
4. **Complex reasoning queries** may take 5-10 seconds

## Advanced Features

### OWA vs CWA Demonstration
- **SQL queries** (CWA): Return only explicitly stored data
- **SPARQL queries** (OWA): May return additional inferred data
- **Example:** Contract counting differences between SQL and SPARQL

### Custom Query Development
1. **Use SPARQL Query tab** for development
2. **Test with simple patterns first**
3. **Add complexity incrementally**
4. **Save successful queries** to your own files

## Success Criteria

After following this guide, you should have:
- ✅ Ontology loaded with full class hierarchy
- ✅ HermiT reasoner running with consistent results
- ✅ Database connected via Ontop OBDA
- ✅ SPARQL queries executing successfully
- ✅ Reasoning classifications working (TopPlayer, YoungPlayer)
- ✅ Cross-system validation between SQL and SPARQL

## Support Files Location
- **Ontology:** `deliverables/ontology/sport-ontology.owl`
- **Database:** `deliverables/database/sports-deliverable-db.mv.db`
- **Mappings:** `deliverables/ontology/sport-ontology-mapping.ttl`
- **Queries:** `deliverables/ontology/sports-tests.q`
- **Properties:** `deliverables/ontology/sport-ontology-protege.properties`