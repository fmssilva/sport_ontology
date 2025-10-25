-- Sport Ontology Test Queries: Integrity Domain
-- All SQL queries for testing data consistency between SQL and SPARQL layers

-- Query: count_all_teams
-- Test: INT-01 - SQL vs SPARQL consistency test
-- Description: Count total number of teams in the database
-- Expected: SQL and SPARQL should return identical counts for data integrity
/* COUNT_ALL_TEAMS */
SELECT COUNT(*) 
FROM team;

-- Query: count_unique_players
-- Test: INT-02 - SQL vs SPARQL consistency test
-- Description: Count distinct players in the database
-- Expected: SQL and SPARQL should return identical counts for mapping integrity
/* COUNT_UNIQUE_PLAYERS */
SELECT COUNT(DISTINCT person_id) 
FROM player_role;