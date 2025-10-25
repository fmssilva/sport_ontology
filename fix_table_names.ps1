#!/bin/bash
# Fix all table references in sport-ontology-mapping.ttl for H2 database schema
# Replace simple table names with fully qualified SPORTSDB.PUBLIC.tablename

# Tables to fix: PERSON, TEAM, PLAYER_ROLE, COACH_ROLE, CONTRACT

# Use Windows/PowerShell compatible commands
cd C:\Users\franc\Desktop\RCR\projects\sport_ontology

# Backup original
cp "src\main\resources\ontology\sport-ontology-mapping.ttl" "src\main\resources\ontology\sport-ontology-mapping.ttl.backup"

# Apply systematic fixes
$content = Get-Content "src\main\resources\ontology\sport-ontology-mapping.ttl"

# Fix table names in FROM clauses and JOIN statements
$content = $content -replace 'FROM PERSON\b', 'FROM SPORTSDB.PUBLIC.PERSON'
$content = $content -replace 'FROM TEAM\b', 'FROM SPORTSDB.PUBLIC.TEAM'
$content = $content -replace 'FROM PLAYER_ROLE\b', 'FROM SPORTSDB.PUBLIC.PLAYER_ROLE'
$content = $content -replace 'FROM COACH_ROLE\b', 'FROM SPORTSDB.PUBLIC.COACH_ROLE'
$content = $content -replace 'FROM CONTRACT\b', 'FROM SPORTSDB.PUBLIC.CONTRACT'

# Fix table names in JOIN statements
$content = $content -replace 'JOIN PERSON\b', 'JOIN SPORTSDB.PUBLIC.PERSON'
$content = $content -replace 'JOIN TEAM\b', 'JOIN SPORTSDB.PUBLIC.TEAM'
$content = $content -replace 'JOIN PLAYER_ROLE\b', 'JOIN SPORTSDB.PUBLIC.PLAYER_ROLE'
$content = $content -replace 'JOIN COACH_ROLE\b', 'JOIN SPORTSDB.PUBLIC.COACH_ROLE'
$content = $content -replace 'JOIN CONTRACT\b', 'JOIN SPORTSDB.PUBLIC.CONTRACT'

# Fix rr:tableName declarations
$content = $content -replace 'rr:tableName "PERSON"', 'rr:tableName "SPORTSDB.PUBLIC.PERSON"'
$content = $content -replace 'rr:tableName "TEAM"', 'rr:tableName "SPORTSDB.PUBLIC.TEAM"'
$content = $content -replace 'rr:tableName "PLAYER_ROLE"', 'rr:tableName "SPORTSDB.PUBLIC.PLAYER_ROLE"'
$content = $content -replace 'rr:tableName "COACH_ROLE"', 'rr:tableName "SPORTSDB.PUBLIC.COACH_ROLE"'
$content = $content -replace 'rr:tableName "CONTRACT"', 'rr:tableName "SPORTSDB.PUBLIC.CONTRACT"'

# Save fixed content
$content | Set-Content "src\main\resources\ontology\sport-ontology-mapping.ttl"

Write-Host "Fixed all table references in mapping file"
Write-Host "Backup saved as sport-ontology-mapping.ttl.backup"