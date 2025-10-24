# Sport Ontology - Build Commands Reference

## Quick Commands

### 🎯 Generate Complete Deliverables
```bash
# Recommended - Fast and simple
mvn exec:exec@deliverables

# Alternative - Direct Java execution
mvn clean compile exec:java@deliverables-java

# Direct execution (after compilation)
java -cp target/classes BuildDeliverables
```

### 📊 Database Only
```bash
mvn clean compile
java -cp target/classes database.CreateH2Database deliverables/database/sports-db
```

### 🧪 Run Tests
```bash
mvn test
```

## What Gets Generated

The `mvn exec:exec@deliverables` command creates:

```
deliverables/
├── database/
│   └── sports-deliverable-db.mv.db     # H2 database file
├── ontology/
│   ├── sport-ontology.owl              # Main ontology
│   ├── sport-ontology-mapping.ttl      # OBDA mappings  
│   ├── sports-tests.q                  # SPARQL queries
│   └── sport-ontology-protege.properties # Auto-configured paths
├── README.md                           # Main guide
├── PROTEGE_STEP_BY_STEP.md            # Protégé setup
└── SEED_DATA_REFERENCE.md             # Database content
```

## Architecture

- **BuildDeliverables.java** (src/main/java/) - Single class that handles everything
- **CreateH2Database.java** (src/main/java/database/) - Database generation only
- **Maven** - Orchestrates the build with proper compilation and dependencies

## System Requirements

- Java 11+  
- Maven 3.6+
- Protégé 5.5+ (for using the deliverables)

## Path Resolution

All database paths are **automatically resolved** to absolute paths for your system:
- No manual configuration needed
- Works on Windows, macOS, Linux
- Protégé configuration files include system-specific paths

## Changes Made

1. **Moved** from hardcoded Maven Ant tasks to flexible Java utility
2. **Consolidated** all deliverables logic into `BuildDeliverables.java`
3. **Added** automatic path resolution for cross-platform compatibility
4. **Maintained** backward compatibility with `mvn exec:exec@deliverables`
5. **Enhanced** documentation generation with real-time data from seed files