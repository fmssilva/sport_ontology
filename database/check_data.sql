-- Check coaches data
SELECT COUNT(DISTINCT p.person_id) as coach_count 
FROM person p JOIN coach_role cr ON p.person_id = cr.person_id;

-- Check contracts data  
SELECT COUNT(*) as active_contracts 
FROM contract WHERE end_date IS NULL OR end_date > CURRENT_DATE;

-- Check team types
SELECT team_type, COUNT(*) as count FROM team GROUP BY team_type;

-- Check all tables
SELECT 'person' as table_name, COUNT(*) as count FROM person
UNION ALL
SELECT 'coach_role', COUNT(*) FROM coach_role
UNION ALL  
SELECT 'player_role', COUNT(*) FROM player_role
UNION ALL
SELECT 'contract', COUNT(*) FROM contract
UNION ALL
SELECT 'team', COUNT(*) FROM team;