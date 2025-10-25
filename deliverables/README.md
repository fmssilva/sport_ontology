# Sport Ontology - Deliverable Files

*Generated automatically by BuildDeliverables.java on 2025-10-25 01:23*

## Overview
Complete OBDA system with H2 database, rich OWL ontology, and Protégé integration.
All paths are automatically configured for your system.

## Quick Start
1. **Open Protégé** and load `ontology/sport-ontology.owl`
2. **Follow:** `PROTEGE_STEP_BY_STEP.md` for complete setup
3. **Reference:** `SEED_DATA_REFERENCE.md` for data understanding

## Contents

### Database Files (database/)
- `sports-deliverable-db.mv.db` - H2 database with 7 teams, 12 players, 5 coaches
- `h2-2.4.240.jar` - h2 JDBC driver for Protégé (portable)
- **Full path:** `C:/Users/franc/Desktop/RCR/projects/sport_ontology/deliverables/database/sports-deliverable-db`

### Ontology Files (ontology/)
- `sport-ontology.owl` - Complete OWL ontology (40+ classes)
- `sport-ontology-mapping.ttl` - OBDA mappings (SQL→RDF)
- `sport-ontology-protege.properties` - Database connection config (auto-generated paths)

### Query Files (ontology/queries/)
- Individual SPARQL and SQL files organized by domain
- `*_queries.sparql` - SPARQL query files for Protégé testing
- `*_queries.sql` - SQL reference files for comparison
- Load SPARQL files individually in Protégé: Window → Tabs → SPARQL Query → File → Load Queries

### Documentation Files
- `PROTEGE_STEP_BY_STEP.md` - Complete Protégé setup guide with system-specific paths
- `SEED_DATA_REFERENCE.md` - Database content and reasoning examples

## Key Features Demonstrated
- ✅ **HermiT Reasoning:** TopPlayer/YoungPlayer classification
- ✅ **OBDA Integration:** SQL-SPARQL query translation
- ✅ **OWA vs CWA:** Contract counting differences
- ✅ **Complex Ontology:** 40+ classes with reasoning rules
- ✅ **Namespace Separation:** H2 data vs ABox reasoning data
- ✅ **Automatic Path Resolution:** Works on any system without manual configuration

## Database Schema Summary
```
Teams (7): Manchester City, Real Madrid, Bayern Munich, PSG, Barcelona + 2 youth
Players (12): Haaland, Mbappe, Bellingham, Vinicius Jr, Kane + 7 others
Coaches (5): Guardiola, Ancelotti, Tuchel, Luis Enrique, Xavi
Contracts (13): 10 active, 3 historical/inactive
```

## System Information
- **Build date:** 2025-10-25 01:23:13
- **Database path:** `C:/Users/franc/Desktop/RCR/projects/sport_ontology/deliverables/database`
- **Java version:** 25
- **OS:** Windows 11 10.0

## ⚠️ Important Setup Requirements
- **h2 JDBC Driver:** Must be installed in Protégé plugins folder
- **Driver Location:** `database/h2-2.4.240.jar` (included for portability)
- **Installation:** Copy JAR to Protégé plugins folder and restart

## Support
- All files are portable and work on any system with Java 11+ and Protégé 5.5+
- Database paths are automatically configured for your system
- SPARQL queries are pre-loaded and ready to execute
- To rebuild: `mvn exec:exec@deliverables` or `java BuildDeliverables`
- **Troubleshooting:** See `PROTEGE_STEP_BY_STEP.md` for H2 driver issues
