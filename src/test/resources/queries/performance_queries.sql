-- Sport Ontology Test Queries: Performance Domain
-- All SQL queries for performance benchmarking tests

-- Query: simple_player_count
-- Test: PERF-01 - Query response time analysis for simple count operations
-- Description: Basic counting operations for performance baseline measurement
-- Expected: 12 players (fast direct database access)
/* SIMPLE_PLAYER_COUNT */
SELECT COUNT(DISTINCT p.person_id) as player_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id
WHERE pr.end_date IS NULL;

-- Query: complex_team_statistics
-- Test: PERF-02 - Complex query performance for multi-table joins with aggregation
-- Description: Performance analysis for complex multi-table operations with statistical functions
-- Expected: 5 teams with comprehensive statistics (more complex processing)
/* COMPLEX_TEAM_STATISTICS */
SELECT t.name,
       COUNT(pr.role_id) as total_players
FROM team t
JOIN player_role pr ON t.team_id = pr.team_id
WHERE pr.end_date IS NULL
GROUP BY t.team_id, t.name
ORDER BY t.name;

-- Query: team_count_performance
-- Test: PERF-01 - Team count query for performance comparison
-- Description: Simple team counting for performance baseline
-- Expected: 7 teams (very fast operation)
/* TEAM_COUNT_PERFORMANCE */
SELECT COUNT(*) as team_count
FROM team;

-- Query: player_count_performance
-- Test: PERF-02 - Player count query for performance comparison  
-- Description: Simple player counting for performance baseline
-- Expected: 12 current players (fast operation)
/* PLAYER_COUNT_PERFORMANCE */
SELECT COUNT(DISTINCT p.person_id) as player_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id
WHERE pr.end_date IS NULL;