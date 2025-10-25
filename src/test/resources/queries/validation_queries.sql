-- Sport Ontology Test Queries: Validation Domain
-- All SQL queries for consistency and validation tests

-- Query: comprehensive_entity_counts
-- Test: CON-01 - Data integrity cross-validation across all entity types
-- Description: Ensure SQL-SPARQL-HermiT consistency for basic counts
-- Expected: 25 total entities (12 players + 7 teams + 8 coaches)
/* COMPREHENSIVE_ENTITY_COUNTS */
SELECT 
    (SELECT COUNT(DISTINCT p.person_id) FROM person p JOIN player_role pr ON p.person_id = pr.person_id WHERE pr.end_date IS NULL) +
    (SELECT COUNT(*) FROM team) +
    (SELECT COUNT(DISTINCT p.person_id) FROM person p JOIN coach_role cr ON p.person_id = cr.person_id WHERE cr.end_date IS NULL) 
    as total_entities;

-- Query: manual_young_player_count
-- Test: CON-02 - Reasoning consistency verification for automatic classification accuracy
-- Description: Manual calculation to verify HermiT reasoning produces expected classifications
-- Expected: 3 young players (manual age < 23 calculation)
/* MANUAL_YOUNG_PLAYER_COUNT */
SELECT COUNT(DISTINCT p.person_id) as manual_young_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id
WHERE YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23
  AND pr.end_date IS NULL;

-- Query: obda_mapping_validation_players
-- Test: CON-03 - OBDA mapping correctness test for Player entities
-- Description: Validate that all SQL players are accessible via SPARQL mappings
-- Expected: All players should be mappable between SQL and SPARQL
/* OBDA_MAPPING_VALIDATION_PLAYERS */
SELECT COUNT(DISTINCT p.id) as sql_player_count
FROM Person p
JOIN PlayerRole pr ON p.id = pr.person_id
WHERE pr.end_date IS NULL;

-- Query: obda_mapping_validation_teams
-- Test: CON-03 - OBDA mapping correctness test for Team entities
-- Description: Validate that all SQL teams are accessible via SPARQL mappings
-- Expected: All teams should be mappable between SQL and SPARQL
/* OBDA_MAPPING_VALIDATION_TEAMS */
SELECT COUNT(*) as sql_team_count
FROM Team;

-- Query: reasoning_accuracy_validation
-- Test: CON-04 - Ontology logical consistency verification
-- Description: Cross-check reasoning results against manual calculations
-- Expected: Reasoning engine should match manual classification counts
/* REASONING_ACCURACY_VALIDATION */
SELECT 
    'TopPlayer' as classification_type,
    COUNT(DISTINCT p.id) as manual_count
FROM Person p
JOIN PlayerRole pr ON p.id = pr.person_id
WHERE p.market_value >= 100000000
  AND pr.end_date IS NULL
UNION ALL
SELECT 
    'YoungPlayer' as classification_type,
    COUNT(DISTINCT p.id) as manual_count
FROM Person p
JOIN PlayerRole pr ON p.id = pr.person_id
WHERE p.age < 23
  AND pr.end_date IS NULL;