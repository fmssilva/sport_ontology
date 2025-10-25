# Sport Ontology - Seed Data Reference

*Generated automatically by BuildDeliverables.java on 2025-10-25 07:46*

## H2 Database Content (SQL/SPARQL Data)

**Namespace:** `http://www.semanticweb.org/sports/ontology#`

### Teams (7 total)
| ID | Name | City | Type | Stadium Capacity |
|----|------|------|------|------------------|
| 1 | Manchester City | Manchester | SeniorTeam | 55,000 |
| 2 | Real Madrid | Madrid | SeniorTeam | 81,000 |
| 3 | Bayern Munich | Munich | SeniorTeam | 75,000 |
| 4 | Paris Saint-Germain | Paris | SeniorTeam | 48,000 |
| 5 | Barcelona | Barcelona | SeniorTeam | 99,000 |
| 6 | Manchester City U21 | Manchester | YouthTeam | 7,000 |
| 7 | Real Madrid Castilla | Madrid | YouthTeam | 6,000 |

### Players (12 total)
| Name | Age | Nationality | Current Team | Position | Market Value |
|------|-----|-------------|--------------|----------|---------------|
| Erling Haaland | 24 | Norway | Manchester City | Forward | €180M |
| Kevin De Bruyne | 33 | Belgium | Manchester City | Midfielder | €85M |
| Ederson Moraes | 31 | Brazil | Manchester City | Goalkeeper | €40M |
| Vinicius Junior | 24 | Brazil | Real Madrid | Forward | €150M |
| Jude Bellingham | 21 | England | Real Madrid | Midfielder | €180M |
| Thibaut Courtois | 32 | Belgium | Real Madrid | Goalkeeper | €60M |
| Harry Kane | 31 | England | Bayern Munich | Forward | €100M |
| Kylian Mbappe | 26 | France | Real Madrid | Forward | €180M |
| Robert Lewandowski | 36 | Poland | Barcelona | Forward | €45M |
| Pedri Gonzalez | 22 | Spain | Barcelona | Midfielder | €80M |
| Rico Lewis | 19 | England | Manchester City U21 | Defender | €15M |
| Nico Paz | 20 | Argentina | Real Madrid Castilla | Midfielder | €8M |

### Coaches (5 total)
| Name | Age | Nationality | Team | Role |
|------|-----|-------------|------|------|
| Pep Guardiola | 53 | Spain | Manchester City | Head Coach |
| Carlo Ancelotti | 65 | Italy | Real Madrid | Head Coach |
| Thomas Tuchel | 51 | Germany | Bayern Munich | Head Coach |
| Luis Enrique | 54 | Spain | PSG | Head Coach |
| Xavi Hernandez | 44 | Spain | Barcelona | Head Coach |

### Key Data for Reasoning Tests

**TopPlayer Classifications (Market Value ≥ €100M):**
- Kylian Mbappe: €180M (Real Madrid)
- Erling Haaland: €180M (Manchester City)
- Jude Bellingham: €180M (Real Madrid)
- Vinicius Junior: €150M (Real Madrid)
- Harry Kane: €100M (Bayern Munich)

**YoungPlayer Classifications (Age < 23):**
- Rico Lewis: 19 years (Manchester City U21)
- Nico Paz: 20 years (Real Madrid Castilla)
- Jude Bellingham: 21 years (Real Madrid)
- Pedri Gonzalez: 22 years (Barcelona)

**Contracts (13 total):**
- 10 Active contracts (is_active = TRUE)
- 3 Inactive contracts (loan ended, transfers, etc.)

## OWA vs CWA Examples

### Contract Count Difference
- **SQL (CWA):** 10 active contracts (only explicitly is_active = TRUE)
- **SPARQL (OWA):** 13 total contracts (assumes unknown active status might be true)

### Player Classification
- **SQL:** Returns only players with explicit market_value data
- **SPARQL/HermiT:** May infer additional classifications based on reasoning rules

## Namespace Strategy

### H2/OBDA Namespace (SQL + SPARQL)
```
http://www.semanticweb.org/sports/ontology#
```
**Usage:** Real data from H2 database via OBDA mappings

### ABox Namespace (HermiT Reasoning)
```
http://www.semanticweb.org/sports/ontology#ABox_
```
**Usage:** Additional test individuals for reasoning demonstrations

### SPARQL Filtering
```sparql
# Exclude ABox individuals from OBDA queries
FILTER(!STRSTARTS(STR(?individual), "http://www.semanticweb.org/sports/ontology#ABox_"))
```
