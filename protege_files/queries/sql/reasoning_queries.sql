-- Sport Ontology Test Queries: Reasoning Domain
-- All SQL queries for reasoning tests with OWL inference validation

-- Query: young_players_by_age
-- Test: REA-01 - Young Player automatic classification
-- Description: Count players under 23 years old for OWL reasoning comparison
-- Expected: 3 players (Rico Lewis: 19, Nico Paz: 20, Jude Bellingham: 21)
/* YOUNG_PLAYERS_BY_AGE */
SELECT COUNT(DISTINCT p.person_id) as young_player_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23
  AND pr.end_date IS NULL;

-- Query: top_players_by_value
-- Test: REA-02 - Top Player market value-based classification
-- Description: Count players worth 100M+ euros for OWL reasoning comparison
-- Expected: 5 players (Haaland, Vinicius, Bellingham, Mbappe, Kane: ≥100M)
/* TOP_PLAYERS_BY_VALUE */
SELECT COUNT(DISTINCT p.person_id) as top_player_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE pr.market_value >= 100000000
  AND pr.end_date IS NULL;

-- Query: top_young_players
-- Test: REA-03 - Multiple inheritance intersection test
-- Description: Count players who are BOTH high-value (≥100M) AND young (<23)
-- Expected: 1 player (Jude Bellingham: 180M + age 21)
/* TOP_YOUNG_PLAYERS */
SELECT COUNT(DISTINCT p.person_id) as top_young_player_count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id 
WHERE pr.market_value >= 100000000
  AND YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23
  AND pr.end_date IS NULL;

-- Query: complex_team_classification
-- Test: REA-04 - Complex class hierarchy inference
-- Description: Count teams meeting EliteTeam criteria (position ≤ 3, has top players, high salaries)
-- Expected: 2 teams (Manchester City, Real Madrid)
/* COMPLEX_TEAM_CLASSIFICATION */
SELECT COUNT(DISTINCT t.team_id) as count
FROM team t
WHERE t.league_position <= 3 
  AND EXISTS(SELECT 1 FROM player_role pr WHERE pr.team_id = t.team_id AND pr.market_value >= 50000000)
  AND EXISTS(SELECT 1 FROM contract c WHERE c.team_id = t.team_id AND c.salary >= 5000000);

-- Query: transitive_league_participation
-- Test: REA-05 - Property chain reasoning  
-- Description: Count Manchester City players (simulating property chain: playsFor ∘ competesIn → participatesIn)
-- Expected: 3 players (Manchester City active players)
/* TRANSITIVE_LEAGUE_PARTICIPATION */
SELECT COUNT(DISTINCT p.person_id) as count
FROM person p
JOIN player_role pr ON p.person_id = pr.person_id
JOIN team t ON pr.team_id = t.team_id
WHERE t.name = 'Manchester City'
  AND pr.end_date IS NULL;

-- Query: bidirectional_player_team_relationships
-- Test: REA-06 - Inverse property demonstration
-- Description: Count all active player-team relationships (demonstrating bidirectional hasPlayer ⇔ playsFor)
-- Expected: 12 relationships (12 active players)
/* BIDIRECTIONAL_PLAYER_TEAM_RELATIONSHIPS */
SELECT COUNT(DISTINCT pr.person_id) as count 
FROM player_role pr 
WHERE pr.end_date IS NULL;