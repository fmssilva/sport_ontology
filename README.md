# Sport Ontology OBDA System

A complete Ontology-Based Data Access (OBDA) system for sports data using H2 Database, Ontop CLI for SPARQL queries, R2RML mappings, and OWL ontology with HermiT reasoning.

## 🚀 Essential Maven Commands

### Core Commands
```bash
# Run all tests (includes automatic OBDA stack setup)
mvn test

# Test if the SQL, SPARQL and Hermit engines work correctly 
mvn test -Dtest=EngineIntegrationTest#testSQLQueries -q

# Generate Protégé files for visual ontology editing
mvn exec:java@protege_files

# Clean and compile project
mvn clean compile

# Full build with tests
mvn clean install
```

### Specific Test Commands
```bash
# Run test categories
mvn test -Dtest=IntegrityTests      # Data integrity validation
mvn test -Dtest=ReasoningTests      # HermiT reasoning validation  
mvn test -Dtest=ValidationTests     # Cross-system validation
mvn test -Dtest=IntegrationTests    # Full OBDA pipeline testing

# Performance testing
mvn test -Dtest=IntegrationTests#testPerformanceBenchmarkingAcrossAllThreeReasoningLayers

# Quiet output
mvn test -q
```

### Generated Files
- **H2 Database:** `database/sport_db.mv.db` (created automatically)
- **Protégé Package:** `protege_files/` folder (complete OBDA setup)
- **Ontop CLI:** `tools/ontop/` (downloaded automatically)

## 🎓 Protégé Setup Guide

### Prerequisites
- Protégé 5.5+ installed
- H2 JDBC driver (included in `protege_files/database/` folder)

### Step 1: Generate Protégé Files
```bash
mvn exec:java@protege_files
```

### Step 2: Load Ontology in Protégé
1. Open Protégé
2. **File → Open** → Select `protege_files/ontology/sport-ontology.owl`
3. Verify 40+ classes are loaded

### Step 3: Configure H2 Database Driver
1. **File → Preferences → JDBC Drivers tab**
2. **Add Driver** with these settings:
   - **Description:** H2 Database for Sport Ontology OBDA
   - **Driver Class:** `org.h2.Driver`
   - **JAR Location:** Browse to `protege_files/database/h2-2.4.240.jar`
3. **OK** to save driver

### Step 4: Connect to Database
1. **Window → Tabs → Ontop Mappings**
2. **Connection parameters** tab
3. Configure connection:
   - **Driver:** org.h2.Driver
   - **URL:** Copy from `protege_files/database/H2_Config.properties`
   - **Username:** sa
   - **Password:** (leave empty)
4. **Test connection** and save

### Step 5: Load OBDA Mappings
1. In **Ontop** tab → **Import R2RML mapping**
2. Select `protege_files/ontology/sport-ontology-mapping.ttl`
3. **Validate** mappings

### Step 6: Start Reasoning
1. **Reasoner → HermiT → Start reasoner**
2. Wait for "Consistent" status
3. **Reasoner → Ontop → Start reasoner** (for OBDA queries)
4. Test SPARQL queries from `protege_files/queries/` folder

## 📊 Project Overview

### Architecture
- **H2 Database** with AUTO_SERVER for concurrent access
- **Ontop CLI 5.1.2** for SPARQL → SQL query rewriting
- **R2RML mappings** for relational-to-RDF transformation
- **OWL ontology** with HermiT reasoning (40+ classes, 500+ axioms)

### Verified Results
- **7 teams** (5 senior, 2 youth)
- **17 people** (12 players, 5 coaches)  
- **Full SPARQL → R2RML → SQL pipeline** working
- **Automatic reasoning** with TopPlayer, YoungPlayer classifications

### Project Structure
```
src/
├── main/java/
│   ├── engines/           # H2, SPARQL, and reasoning engines
│   ├── database/          # H2 database creation and seeding
│   └── protege_files/     # Automated Protégé package generation
├── main/resources/ontology/ # OWL ontology + R2RML mappings
└── test/java/             # Comprehensive OBDA integration tests

protege_files/             # Generated Protégé-ready package
database/                  # H2 database files (auto-created)
tools/                     # Auto-managed Ontop CLI
```

## 🧠 Advanced Features

### Rich Class Hierarchy (150+ concepts)
- **Person** → Player, Coach, StaffMember hierarchies
- **Team** → SeniorTeam, YouthTeam, EliteTeam classifications  
- **Contract** → PermanentContract, LoanContract types
- **Organization** → Federation, League, Club structures

### Advanced Description Logic
- **DataSomeValuesFrom:** `YoungPlayer ≡ Player ⊓ ∃hasAge.≤23`
- **Property Chains:** `playsFor ∘ competesIn → participatesIn`
- **Qualified Restrictions:** Complex market value constraints
- **Automatic Classification:** HermiT infers TopPlayer, EliteTeam classes

### Performance Characteristics
- **SQL Direct:** ~7ms (database queries)
- **SPARQL via Ontop:** ~4s (OBDA translation)  
- **HermiT Reasoning:** ~150ms (classification + consistency)

## 🔧 Development Notes

### What Happens Automatically
1. **Ontop CLI** downloaded and configured
2. **H2 Database** created with realistic sports data
3. **JDBC Driver** copied to Ontop classpath
4. **Protégé Package** generated with all dependencies
5. **Full Testing** across SQL, SPARQL, and reasoning engines

### Prerequisites
```bash
# Check Maven installation
mvn --version

# Install Maven on Windows (PowerShell as admin):
choco install maven
```

### Cross-Platform Compatibility
- **Windows:** Full support with automated setup
- **Linux/macOS:** Compatible with minor path adjustments
- **H2 AUTO_SERVER:** Enables concurrent database access

## 🧪 Testing Strategy

### Three-Tier Validation
1. **SQL Layer:** Direct H2 database validation (7ms)
2. **SPARQL Layer:** Ontop OBDA translation (4s) 
3. **Reasoning Layer:** HermiT classification (150ms)

### Example Queries

#### SQL (Database)
```sql
-- Count active players
SELECT COUNT(*) FROM player_role WHERE end_date IS NULL;
-- Expected: 12 players
```

#### SPARQL (OBDA)
```sparql
PREFIX : <http://www.semanticweb.org/sports/ontology#>
SELECT (COUNT(?player) AS ?count) WHERE {
  ?player a :Player .
}
# Expected: 12 players (database) + reasoning individuals
```

#### Reasoning (HermiT)
```sparql
PREFIX : <http://www.semanticweb.org/sports/ontology#>
SELECT ?player WHERE {
  ?player a :TopPlayer .
}
# Expected: Automatic classification based on market value ≥100M
```

### Test Categories
- **Integrity Tests:** Data consistency across systems
- **Reasoning Tests:** Automatic classification validation
- **Integration Tests:** Full OBDA pipeline performance
- **Validation Tests:** Cross-system result comparison

## 🎯 Key Achievements

### Academic Requirements Met
✅ **OBDA with Ontop:** Complete SPARQL → SQL rewriting  
✅ **Advanced DL Constructors:** DataSomeValuesFrom, property chains  
✅ **General Class Axioms:** Disjointness, equivalence classes  
✅ **Rich Reasoning:** Beyond basic ALC expressivity  

### Technical Excellence
- **40+ Classes:** Professional-grade domain modeling
- **Multi-Engine Architecture:** SQL, SPARQL, HermiT integration
- **Automated Testing:** 20+ test cases with performance benchmarking
- **Production-Ready:** Complete portable Protégé package

### Performance Validation
- **HermiT:** Sub-second reasoning with 500+ axioms
- **OBDA Pipeline:** Working SPARQL queries via Ontop CLI
- **Data Integrity:** Consistent results across all three engines
- **Cross-Platform:** Windows, Linux, macOS compatible