-- Deep analysis of all potential hasJerseyNumber violations
-- Since we fixed Mbappe transfer, let's check for other issues

-- Check 1: Any remaining functional violations?
SELECT 'FUNCTIONAL_VIOLATION' as issue_type,
       person_id,
       COUNT(DISTINCT jersey_number) as jersey_count,
       GROUP_CONCAT(DISTINCT jersey_number) as jerseys,
       GROUP_CONCAT(DISTINCT team_id) as teams
FROM player_role 
WHERE end_date IS NULL 
  AND jersey_number IS NOT NULL
GROUP BY person_id 
HAVING COUNT(DISTINCT jersey_number) > 1;

-- Check 2: Duplicate jerseys within same team?
SELECT 'DUPLICATE_JERSEY' as issue_type,
       team_id,
       jersey_number,
       COUNT(*) as player_count,
       GROUP_CONCAT(person_id) as persons
FROM player_role 
WHERE end_date IS NULL 
  AND jersey_number IS NOT NULL
GROUP BY team_id, jersey_number 
HAVING COUNT(*) > 1;

-- Check 3: All current active jersey assignments
SELECT 'ACTIVE_ASSIGNMENTS' as issue_type,
       person_id,
       team_id,
       jersey_number,
       position,
       start_date,
       end_date
FROM player_role 
WHERE end_date IS NULL 
  AND jersey_number IS NOT NULL
ORDER BY person_id, team_id;

-- Check 4: Check for any jersey numbers that might cause issues
SELECT 'JERSEY_ANALYSIS' as issue_type,
       jersey_number,
       COUNT(*) as usage_count,
       GROUP_CONCAT(DISTINCT person_id) as persons,
       GROUP_CONCAT(DISTINCT team_id) as teams
FROM player_role 
WHERE end_date IS NULL 
  AND jersey_number IS NOT NULL
GROUP BY jersey_number
ORDER BY jersey_number;