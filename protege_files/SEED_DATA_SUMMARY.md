# Sport Ontology - Seed Data Summary

*Overview of test data used for reasoning and OWA vs CWA demonstration*

## H2 Database Content (SQL/SPARQL Access)

**Namespace:** `http://www.semanticweb.org/sports/ontology#`

### Data Strategy
The H2 database contains carefully selected seed data to demonstrate:
- **Basic OBDA functionality** (SQL ↔ SPARQL consistency)
- **OWA vs CWA differences** (open vs closed world assumptions)
- **Reasoning validation** (classification and inference)
- **Small enough for manual verification** (results can be checked by hand)

### Teams (7 total)
| ID | Name | Type | Players | Purpose |
|----|------|------|---------|----------|
| 1 | Manchester City | Senior | 3 | Top-tier team with star players |
| 2 | Real Madrid | Senior | 3 | Another top-tier team for comparison |
| 3 | Bayern Munich | Senior | 1 | Single star player team |
| 4 | PSG | Senior | 0 | Team without current players |
| 5 | Barcelona | Senior | 2 | Mix of star and regular players |
| 6 | Manchester City U21 | Youth | 1 | Youth team with young player |
| 7 | Real Madrid Castilla | Youth | 1 | Another youth team |

### Players (12 total)
| Name | Age | Team | Market Value | Classification |
|------|-----|------|--------------|----------------|
| Erling Haaland | 24 | Manchester City | €180M | TopPlayer |
| Kevin De Bruyne | 33 | Manchester City | €85M | Regular |
| Ederson Moraes | 31 | Manchester City | €40M | Regular |
| Vinicius Junior | 24 | Real Madrid | €150M | TopPlayer |
| Jude Bellingham | 21 | Real Madrid | €180M | TopPlayer + YoungPlayer |
| Thibaut Courtois | 32 | Real Madrid | €60M | Regular |
| Harry Kane | 31 | Bayern Munich | €100M | TopPlayer |
| Kylian Mbappe | 26 | Real Madrid | €180M | TopPlayer |
| Robert Lewandowski | 36 | Barcelona | €45M | Regular |
| Pedri Gonzalez | 22 | Barcelona | €80M | YoungPlayer |
| Rico Lewis | 19 | Man City U21 | €15M | YoungPlayer |
| Nico Paz | 20 | Real Madrid Castilla | €8M | YoungPlayer |

### Key Data for Reasoning Tests

**TopPlayer Classification (Market Value ≥ €100M):**
- Expected: 5 players (Haaland, Vinicius, Bellingham, Kane, Mbappe)
- Demonstrates: Automatic classification based on market value

**YoungPlayer Classification (Age < 23):**
- Expected: 4 players (Bellingham, Pedri, Rico Lewis, Nico Paz)
- Demonstrates: Age-based reasoning rules

**Contract Data (OWA vs CWA Demo):**
- **SQL (CWA):** 10 active contracts (only explicitly is_active = TRUE)
- **SPARQL (OWA):** 13 total contracts (assumes unknown status might be true)
- **Purpose:** Shows philosophical difference in data assumptions

## ABox Content (HermiT Reasoning Only)

**Namespace:** `http://www.semanticweb.org/sports/ontology#ABox_*`

### Purpose
ABox individuals are embedded in the ontology for pure reasoning validation:
- **Independent from H2 database** (separate namespace)
- **HermiT reasoning only** (not accessible via SPARQL/OBDA)
- **Test complex reasoning scenarios** (multiple inheritance, property chains)

### Example ABox Individuals
- `ABox_Erling_Haaland` - TopPlayer with high market value
- `ABox_Young_Talent` - YoungPlayer for age-based reasoning
- `ABox_Elite_Club` - EliteTeam for complex team classification
- **Purpose:** Validate reasoning rules independently from database data

## Namespace Strategy Implementation

### SPARQL Filtering (Access H2 Database Only)
```sparql
# All SPARQL queries include this filter:
FILTER(!STRSTARTS(STR(?individual), "http://www.semanticweb.org/sports/ontology#ABox_"))
```

### Benefits of Separation
- ✅ **SPARQL/SQL consistency:** Both access same H2 database data
- ✅ **Clean comparisons:** SQL and filtered SPARQL return identical counts
- ✅ **Independent reasoning:** HermiT can validate complex scenarios
- ✅ **Easy debugging:** Issues can be isolated to specific data sources

## OWA vs CWA Demonstration Examples

### Example 1: Contract Counting
- **SQL Query:** `SELECT COUNT(*) FROM contract WHERE is_active = TRUE;`
- **Result:** 10 contracts (only explicitly active)
- **SPARQL Query:** Count all contract relationships via OBDA
- **Result:** 13 contracts (includes unknown status)
- **Demonstrates:** CWA assumes missing = false, OWA assumes missing = unknown

### Example 2: Player Team Assignment
- **SQL:** Returns only players with explicit current team assignments
- **SPARQL:** May include players with inferred team relationships
- **HermiT:** Can infer additional relationships through property chains

### Example 3: Classification Completeness
- **SQL:** Only returns players with explicit market_value ≥ 100M
- **SPARQL/HermiT:** May classify additional players through inference
- **Reasoning:** Automatic TopPlayer classification even with incomplete data

## Seed Data Size Rationale

**Small enough for manual verification:**
- 7 teams → Easy to count and verify
- 12 players → Can manually check classifications
- 5 TopPlayers → Simple to validate market value rules
- 4 YoungPlayers → Quick age verification

**Large enough for meaningful tests:**
- Multiple team types (senior/youth)
- Various player value ranges
- Different age distributions
- Complex contract scenarios
- Property chain examples (team → league participation)

**Perfect for educational purposes:**
- Results can be verified by hand
- Shows clear differences between SQL/SPARQL/HermiT
- Demonstrates real-world ontology concepts
- Enables easy debugging when tests fail
