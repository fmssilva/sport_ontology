-- Quick check for functional property violations
-- Check if any person has multiple jersey numbers across active roles

SELECT 
    person_id,
    COUNT(DISTINCT jersey_number) as distinct_jerseys,
    GROUP_CONCAT(DISTINCT jersey_number) as jerseys,
    GROUP_CONCAT(DISTINCT team_id) as teams,
    GROUP_CONCAT(DISTINCT position) as positions
FROM player_role 
WHERE end_date IS NULL 
  AND jersey_number IS NOT NULL
GROUP BY person_id 
HAVING COUNT(DISTINCT jersey_number) > 1
ORDER BY person_id;