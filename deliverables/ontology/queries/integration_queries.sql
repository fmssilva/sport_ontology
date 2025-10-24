-- Sport Ontology Test Queries: Integration Domain
-- All SQL queries for advanced OBDA integration tests

-- Query: player_team_coach_network
-- Test: ADV-01 - Multi-entity network query across Player-Team-Coach relationships
-- Description: Complex SPARQL across Player-Team-Coach relationships in single query with joins
-- Expected: 5 complete player-team-coach relationships
/* PLAYER_TEAM_COACH_NETWORK */
SELECT p.full_name as player_name, 
       t.name as team_name, 
       coach.full_name as coach_name,
       pr.market_value
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id
JOIN team t ON pr.team_id = t.team_id
JOIN coach_role cr ON t.team_id = cr.team_id
JOIN person coach ON cr.person_id = coach.person_id
WHERE pr.end_date IS NULL 
  AND cr.end_date IS NULL
ORDER BY t.name, p.full_name;

-- Query: average_market_value_by_team
-- Test: ADV-02 - Statistical analysis combining SQL aggregation + OWL inference
-- Description: Statistical analysis with market value calculations by team
-- Expected: 3 teams with calculated average market values
/* AVERAGE_MARKET_VALUE_BY_TEAM */
SELECT t.name as team_name, 
       AVG(pr.market_value) as avg_market_value,
       COUNT(pr.role_id) as player_count,
       MAX(pr.market_value) as highest_value_player,
       t.stadium_capacity
FROM team t
JOIN player_role pr ON t.team_id = pr.team_id
JOIN person p ON pr.person_id = p.person_id
WHERE pr.end_date IS NULL
  AND pr.market_value IS NOT NULL
GROUP BY t.team_id, t.name, t.stadium_capacity
HAVING COUNT(pr.role_id) > 1
ORDER BY avg_market_value DESC;

-- Query: simple_player_count
-- Test: PERF-01 - Query response time analysis for simple count operations
-- Description: Basic counting operations for performance comparison
-- Expected: 12 players (direct count)
/* SIMPLE_PLAYER_COUNT */
SELECT COUNT(DISTINCT p.person_id) as player_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id
WHERE pr.end_date IS NULL;

-- Query: complex_team_statistics
-- Test: PERF-02 - Complex query performance for multi-table joins with aggregation
-- Description: Complex multi-table operations with statistical functions
-- Expected: 5 teams with comprehensive statistics
/* COMPLEX_TEAM_STATISTICS */
SELECT t.name,
       COUNT(DISTINCT pr.player_role_id) as total_players,
       AVG(p.market_value) as avg_market_value,
       MAX(p.market_value) as max_market_value,
       AVG(p.age) as avg_age,
       COUNT(DISTINCT cr.coach_role_id) as coaching_staff
FROM team t
LEFT JOIN player_role pr ON t.team_id = pr.team_id AND pr.end_date IS NULL
LEFT JOIN person p ON pr.person_id = p.person_id
LEFT JOIN coach_role cr ON t.team_id = cr.team_id AND cr.end_date IS NULL
GROUP BY t.team_id, t.name
ORDER BY t.name;

-- Query: comprehensive_entity_counts
-- Test: Validation entity count verification
-- Description: Cross-system validation for comprehensive entity counting
-- Expected: Match SPARQL query results
/* COMPREHENSIVE_ENTITY_COUNTS */
SELECT 
    'Team' as entity_type, 
    COUNT(*) as entity_count 
FROM team
UNION ALL
SELECT 
    'Player' as entity_type, 
    COUNT(DISTINCT p.person_id) as entity_count
FROM person p 
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE pr.end_date IS NULL
UNION ALL
SELECT 
    'Coach' as entity_type, 
    COUNT(DISTINCT p.person_id) as entity_count
FROM person p 
JOIN coach_role cr ON p.person_id = cr.person_id 
WHERE cr.end_date IS NULL
UNION ALL
SELECT 
    'Contract' as entity_type, 
    COUNT(*) as entity_count
FROM contract
ORDER BY entity_type;