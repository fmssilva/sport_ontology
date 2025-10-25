-- Sport Ontology Test Queries: Assumptions Domain
-- All SQL queries for testing Closed World vs Open World Assumptions

-- Query: count_active_players
-- Test: ASS-01 - Closed World Assumption test
-- Description: Count players who are currently active (no end date)
-- Expected: Tests whether missing end_date means "active" (CWA) vs "unknown" (OWA)
/* COUNT_ACTIVE_PLAYERS */
SELECT COUNT(*) 
FROM player_role 
WHERE end_date IS NULL;


-- Query: count_players_with_market_value
-- Test: ASS-02 - Open World Assumption test  
-- Description: Count players who have explicit market values
-- Expected: Tests whether missing market_value means "no value" (CWA) vs "unknown value" (OWA)
/* COUNT_PLAYERS_WITH_MARKET_VALUE */
SELECT COUNT(*) 
FROM player_role 
WHERE market_value IS NOT NULL;

-- Query: players_without_market_value
-- Test: OWA-01 - Missing market value handling under Open World Assumption
-- Description: CWA behavior - missing values assumed as NULL/absent
-- Expected: 0 (all players in our test data have market values)
/* PLAYERS_WITHOUT_MARKET_VALUE */
SELECT COUNT(DISTINCT p.person_id) as players_without_value
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE pr.market_value IS NULL
  AND pr.end_date IS NULL;

-- Query: players_without_position
-- Test: OWA-02 - Unspecified player positions under Open World Assumption
-- Description: CWA behavior - missing position data assumed as NULL
-- Expected: 3 players without explicit position in our test data
/* PLAYERS_WITHOUT_POSITION */
SELECT COUNT(DISTINCT p.person_id) as players_without_position
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE pr.position IS NULL
  AND pr.end_date IS NULL;

-- Query: current_team_players
-- Test: CWA-01 - Definite team roster count under Closed World Assumption
-- Description: CWA assumes roster completeness for counting
-- Expected: 12 explicit current players (assumes complete roster data)
/* CURRENT_TEAM_PLAYERS */
SELECT COUNT(DISTINCT p.person_id) as current_players
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE pr.end_date IS NULL;

-- Query: players_with_valid_contracts
-- Test: CWA-02 - Definite contract status validation under Closed World Assumption
-- Description: CWA - if no contract data, then no contract
-- Expected: 8 players with explicit valid contracts
/* PLAYERS_WITH_VALID_CONTRACTS */
SELECT COUNT(DISTINCT p.person_id) as players_with_contracts
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
JOIN contract c ON p.person_id = c.person_id
WHERE c.end_date > CURRENT_DATE
  AND pr.end_date IS NULL;