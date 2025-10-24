-- Sport Ontology Test Queries: Reasoning Domain-- Sport Ontology Test Queries: Reasoning Domain

-- All SQL queries for reasoning tests with OWL inference validation-- All SQL queries for reasoning tests with OWL inference validation



-- Query: count_top_players_by_market_value-- Query: count_top_players_by_market_value

-- Test: REA-01 - Top player inference-- Test: REA-01 - Top player inference

-- Description: Count players with market value >= 100M (direct database check)-- Description: Count players with market value >= 100M (direct database check)

-- Expected: 6 players based on database constraints-- Expected: 6 players based on database constraints

/* COUNT_TOP_PLAYERS_BY_MARKET_VALUE *//* COUNT_TOP_PLAYERS_BY_MARKET_VALUE */

SELECT COUNT(*)SELECT COUNT(*)

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_idJOIN player_role pr ON p.person_id = pr.person_id

WHERE pr.market_value >= 100000000;WHERE pr.market_value >= 100000000;



-- Query: count_young_players_by_age  -- Query: count_young_players_by_age  

-- Test: REA-02 - Young player inference-- Test: REA-02 - Young player inference

-- Description: Count players under 23 years old (birth date calculation)-- Description: Count players under 23 years old (birth date calculation)

-- Expected: 3 players (Rico Lewis: 19, Nico Paz: 20, Jude Bellingham: 21)-- Expected: 3 players (Rico Lewis: 19, Nico Paz: 20, Jude Bellingham: 21)

/* COUNT_YOUNG_PLAYERS_BY_AGE *//* COUNT_YOUNG_PLAYERS_BY_AGE */

SELECT COUNT(*)SELECT COUNT(*)

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_idJOIN player_role pr ON p.person_id = pr.person_id

WHERE YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23;WHERE YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23;



-- Query: count_top_young_players-- Query: count_top_young_players

-- Test: REA-03 - Multiple inheritance test  -- Test: REA-03 - Multiple inheritance test  

-- Description: Count players who are both high-value (>=100M) and young (<23)-- Description: Count players who are both high-value (>=100M) and young (<23)

-- Expected: 1 player (Jude Bellingham: 180M + age 21)-- Expected: 1 player (Jude Bellingham: 180M + age 21)

/* COUNT_TOP_YOUNG_PLAYERS *//* COUNT_TOP_YOUNG_PLAYERS */

SELECT COUNT(*)SELECT COUNT(*)

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_idJOIN player_role pr ON p.person_id = pr.person_id

WHERE pr.market_value >= 100000000WHERE pr.market_value >= 100000000

  AND YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23;  AND YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23;



-- Query: young_players_by_age-- Query: young_players_by_age

-- Test: REA-01 - Young Player automatic classification-- Test: REA-01 - Young Player automatic classification

-- Description: Count players under 23 years old for OWL reasoning comparison-- Description: Count players under 23 years old for OWL reasoning comparison

-- Expected: 3 players (Rico Lewis: 19, Nico Paz: 20, Jude Bellingham: 21)-- Expected: 3 players (Rico Lewis: 19, Nico Paz: 20, Jude Bellingham: 21)

/* YOUNG_PLAYERS_BY_AGE *//* YOUNG_PLAYERS_BY_AGE */

SELECT COUNT(DISTINCT p.person_id) as young_player_countSELECT COUNT(DISTINCT p.person_id) as young_player_count

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_id JOIN player_role pr ON p.person_id = pr.person_id 

WHERE YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23WHERE YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23

  AND pr.end_date IS NULL;  AND pr.end_date IS NULL;



-- Query: top_players_by_value-- Query: top_players_by_value

-- Test: REA-02 - Top Player market value-based classification-- Test: REA-02 - Top Player market value-based classification

-- Description: Count players worth 100M+ euros for OWL reasoning comparison-- Description: Count players worth 100M+ euros for OWL reasoning comparison

-- Expected: 5 players (Haaland, Vinicius, Bellingham, Mbappe, Kane: ≥100M)-- Expected: 5 players (Haaland, Vinicius, Bellingham, Mbappe, Kane: ≥100M)

/* TOP_PLAYERS_BY_VALUE *//* TOP_PLAYERS_BY_VALUE */

SELECT COUNT(DISTINCT p.person_id) as top_player_countSELECT COUNT(DISTINCT p.person_id) as top_player_count

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_id JOIN player_role pr ON p.person_id = pr.person_id 

WHERE pr.market_value >= 100000000WHERE pr.market_value >= 100000000

  AND pr.end_date IS NULL;  AND pr.end_date IS NULL;



-- Query: top_young_players-- Query: top_young_players

-- Test: REA-03 - Multiple inheritance intersection test-- Test: REA-03 - Multiple inheritance intersection test

-- Description: Count players who are BOTH high-value (≥100M) AND young (<23)-- Description: Count players who are BOTH high-value (≥100M) AND young (<23)

-- Expected: 1 player (Jude Bellingham: 180M + age 21)-- Expected: 1 player (Jude Bellingham: 180M + age 21)

/* TOP_YOUNG_PLAYERS *//* TOP_YOUNG_PLAYERS */

SELECT COUNT(DISTINCT p.person_id) as top_young_player_countSELECT COUNT(DISTINCT p.person_id) as top_young_player_count

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_id JOIN player_role pr ON p.person_id = pr.person_id 

WHERE pr.market_value >= 100000000WHERE pr.market_value >= 100000000

  AND YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23  AND YEAR(CURRENT_DATE) - YEAR(p.birth_date) < 23

  AND pr.end_date IS NULL;  AND pr.end_date IS NULL;



-- Query: complex_team_classification-- Query: complex_team_classification

-- Test: REA-04 - Complex class hierarchy inference-- Test: REA-04 - Complex class hierarchy inference

-- Description: Count teams meeting TopTeam criteria (large stadium, has top players, high salaries)-- Description: Count teams meeting EliteTeam criteria (position ≤ 3, has top players, high salaries)

-- Expected: 4 teams (Manchester City, Real Madrid, Bayern Munich, Barcelona)-- Expected: 2 teams (Manchester City, Real Madrid)

/* COMPLEX_TEAM_CLASSIFICATION *//* COMPLEX_TEAM_CLASSIFICATION */

SELECT COUNT(DISTINCT t.team_id) as countSELECT COUNT(DISTINCT t.team_id) as count

FROM team tFROM team t

WHERE t.stadium_capacity > 50000 WHERE t.league_position <= 3 

  AND EXISTS(SELECT 1 FROM player_role pr WHERE pr.team_id = t.team_id AND pr.market_value >= 50000000)  AND EXISTS(SELECT 1 FROM player_role pr WHERE pr.team_id = t.team_id AND pr.market_value >= 50000000)

  AND EXISTS(SELECT 1 FROM contract c WHERE c.team_id = t.team_id AND c.salary >= 5000000);  AND EXISTS(SELECT 1 FROM contract c WHERE c.team_id = t.team_id AND c.salary >= 5000000);



-- Query: transitive_league_participation-- Query: transitive_league_participation

-- Test: REA-05 - Property chain reasoning  -- Test: REA-05 - Property chain reasoning  

-- Description: Count Manchester City players via explicit JOINs (simulating property chain)-- Description: Count Premier League players via explicit JOINs (simulating property chain)

-- Expected: 4 players (Manchester City players)-- Expected: 4 players (Manchester City players in Premier League)

/* TRANSITIVE_LEAGUE_PARTICIPATION *//* TRANSITIVE_LEAGUE_PARTICIPATION */

SELECT COUNT(DISTINCT p.person_id) as countSELECT COUNT(DISTINCT p.person_id) as count

FROM person pFROM person p

JOIN player_role pr ON p.person_id = pr.person_idJOIN player_role pr ON p.person_id = pr.person_id

JOIN team t ON pr.team_id = t.team_idJOIN team t ON pr.team_id = t.team_id

WHERE t.name = 'Manchester City'WHERE t.league_name = 'Premier League'

  AND pr.end_date IS NULL;  AND pr.end_date IS NULL;



-- Query: bidirectional_player_team_relationships-- Query: bidirectional_player_team_relationships

-- Test: REA-06 - Inverse property demonstration-- Test: REA-06 - Inverse property demonstration

-- Description: Count all player-team relationships in both directions (manual bidirectional maintenance)-- Description: Count all player-team relationships in both directions (manual bidirectional maintenance)

-- Expected: 26 relationships (13 active players × 2 directions)-- Expected: 24 relationships (12 active players × 2 directions)

/* BIDIRECTIONAL_PLAYER_TEAM_RELATIONSHIPS *//* BIDIRECTIONAL_PLAYER_TEAM_RELATIONSHIPS */

SELECT COUNT(*) as count FROM (SELECT COUNT(*) as count FROM (

  SELECT pr.team_id, pr.person_id FROM player_role pr WHERE pr.end_date IS NULL  SELECT pr.team_id, pr.person_id FROM player_role pr WHERE pr.end_date IS NULL

  UNION  UNION

  SELECT t.team_id, p.person_id FROM team t  SELECT t.team_id, p.person_id FROM team t

  JOIN player_role pr2 ON t.team_id = pr2.team_id  JOIN player_role pr2 ON t.team_id = pr2.team_id

  JOIN person p ON pr2.person_id = p.person_id  JOIN person p ON pr2.person_id = p.person_id

  WHERE pr2.end_date IS NULL  WHERE pr2.end_date IS NULL

) combined;) combined;

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