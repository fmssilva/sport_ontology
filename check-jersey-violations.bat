@echo off
cd c:\Users\franc\Desktop\RCR\projects\sport_ontology\deliverables\database
echo === FUNCTIONAL PROPERTY VIOLATION CHECK ===
echo Looking for persons with multiple different jersey numbers:
echo.

java -cp h2-2.4.240.jar org.h2.tools.Shell -url "jdbc:h2:./sports-deliverable-db" -user sa -sql "SELECT p.person_id, p.name, COUNT(DISTINCT ca.jersey_number) as jersey_count, STRING_AGG(DISTINCT CAST(ca.jersey_number AS VARCHAR), ', ') as jersey_numbers FROM Person p JOIN ContractAssignment ca ON p.person_id = ca.person_id WHERE ca.jersey_number IS NOT NULL GROUP BY p.person_id, p.name HAVING COUNT(DISTINCT ca.jersey_number) > 1 ORDER BY jersey_count DESC;"

echo.
echo === DETAILED JERSEY ASSIGNMENTS ===
java -cp h2-2.4.240.jar org.h2.tools.Shell -url "jdbc:h2:./sports-deliverable-db" -user sa -sql "SELECT p.person_id, p.name, t.name as team_name, ca.jersey_number, ca.start_date, ca.end_date, CASE WHEN ca.end_date IS NULL THEN 'ACTIVE' ELSE 'ENDED' END as status FROM Person p JOIN ContractAssignment ca ON p.person_id = ca.person_id JOIN Team t ON ca.team_id = t.team_id WHERE ca.jersey_number IS NOT NULL ORDER BY p.person_id, ca.start_date;"

echo.
echo === ACTIVE JERSEY ASSIGNMENTS ===
java -cp h2-2.4.240.jar org.h2.tools.Shell -url "jdbc:h2:./sports-deliverable-db" -user sa -sql "SELECT p.person_id, p.name, t.name as team_name, ca.jersey_number FROM Person p JOIN ContractAssignment ca ON p.person_id = ca.person_id JOIN Team t ON ca.team_id = t.team_id WHERE ca.jersey_number IS NOT NULL AND ca.end_date IS NULL ORDER BY p.person_id;"