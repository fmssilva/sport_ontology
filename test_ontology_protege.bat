@echo off
echo ===============================================
echo Testing Enhanced Sport Ontology with Protege
echo ===============================================
echo.
echo Ontology Location:
echo %cd%\src\main\resources\ontology\sport-ontology.owl
echo.
echo Instructions for Protege Testing:
echo 1. Open Protege
echo 2. File -> Open -> Navigate to the ontology file above
echo 3. Go to the "Classes" tab
echo 4. Enable "Show object property assertions" if needed
echo 5. Expand the class hierarchy to verify enriched structure
echo.
echo Expected Rich Hierarchy:
echo - Person
echo   - Player (ExperiencedPlayer, VeteranPlayer)
echo   - Coach (TopCoach, HeadCoach, AssistantCoach, SpecialistCoach)
echo   - StaffMember
echo     - MedicalStaff (TeamDoctor, Physiotherapist, SportsPsychologist, Nutritionist)
echo     - AdministrativeStaff (SportingDirector, Scout, DataAnalyst, MediaOfficer, ClubSecretary)
echo     - TechnicalStaff (VideoAnalyst, PerformanceAnalyst, EquipmentManager)
echo - Team (EliteTeam, ReserveTeam)
echo   - YouthTeam (U21Team, U18Team, U16Team)
echo - Organization
echo   - Federation, League (ProfessionalLeague), Club, Academy
echo - Competition
echo   - DomesticCompetition, InternationalCompetition
echo   - CupCompetition, LeagueCompetition, TournamentCompetition
echo - Contract
echo   - PermanentContract (ProfessionalContract)
echo   - LoanContract, ShortTermContract (YouthContract, TrialContract)
echo   - ScholarshipContract, SeasonalContract, PreContract
echo.
echo 6. Test HermiT Reasoner:
echo    - Go to "Reasoner" menu
echo    - Select "HermiT"
echo    - Click "Start reasoner"
echo    - Verify no inconsistencies
echo    - Check inferred class hierarchy
echo.
echo 7. Verify namespace separation:
echo    - Check that ABox individuals use proper namespaces
echo    - Database entities should use base namespace
echo    - Data entities should use data: namespace
echo.
pause