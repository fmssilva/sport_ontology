@echo off
echo ========================================
echo Sports Ontology - HermiT Performance Helper
echo ========================================

:menu
echo.
echo Choose an option:
echo 1. Switch to Protege-optimized ontology (Fast GUI reasoning)
echo 2. Switch to Full ontology (Complete reasoning, slower GUI)
echo 3. Test HermiT performance with current ontology
echo 4. Show current ontology info
echo 5. Exit
echo.
set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" goto protege_mode
if "%choice%"=="2" goto full_mode  
if "%choice%"=="3" goto test_performance
if "%choice%"=="4" goto show_info
if "%choice%"=="5" goto exit
goto menu

:protege_mode
echo.
echo Switching to Protege-optimized ontology...
copy "src\main\resources\ontology\sport-ontology.owl" "src\main\resources\ontology\sport-ontology-full-backup.owl" >nul 2>&1
copy "src\main\resources\ontology\sport-ontology-protege-optimized.owl" "src\main\resources\ontology\sport-ontology.owl" >nul
echo ✓ Switched to simplified ontology for Protege
echo ✓ Full ontology backed up as sport-ontology-full-backup.owl
echo ✓ You can now use HermiT in Protege without performance issues
goto menu

:full_mode
echo.
echo Switching to Full ontology...
if exist "src\main\resources\ontology\sport-ontology-full-backup.owl" (
    copy "src\main\resources\ontology\sport-ontology-full-backup.owl" "src\main\resources\ontology\sport-ontology.owl" >nul
    echo ✓ Restored full ontology from backup
) else (
    echo ✗ No backup found. Full ontology may already be active.
)
echo ✓ Full ontology active (complex reasoning, may be slow in Protege GUI)
goto menu

:test_performance
echo.
echo Testing HermiT performance with current ontology...
echo Running ReasoningTest to measure performance...
mvn test -Dtest=ReasoningTest -q
echo.
echo Check the output above for reasoning performance results.
goto menu

:show_info
echo.
echo Current Ontology Information:
echo ============================
if exist "src\main\resources\ontology\sport-ontology.owl" (
    echo ✓ Main ontology file exists
    findstr /C:"EquivalentClasses" "src\main\resources\ontology\sport-ontology.owl" | find /C "EquivalentClasses" >temp_count.txt
    set /p equiv_count=<temp_count.txt
    del temp_count.txt >nul 2>&1
    echo   - EquivalentClasses axioms: %equiv_count%
    
    findstr /C:"ObjectMinCardinality" "src\main\resources\ontology\sport-ontology.owl" >nul
    if errorlevel 1 (
        echo   - Complex cardinality constraints: NONE (Protege-optimized^)
        echo   - Status: ✓ OPTIMIZED FOR PROTEGE
    ) else (
        echo   - Complex cardinality constraints: PRESENT
        echo   - Status: ⚠ FULL VERSION (may be slow in Protege^)
    )
) else (
    echo ✗ Main ontology file not found!
)

if exist "src\main\resources\ontology\sport-ontology-protege-optimized.owl" (
    echo ✓ Protege-optimized version available
) else (
    echo ✗ Protege-optimized version not found
)

if exist "src\main\resources\ontology\sport-ontology-full-backup.owl" (
    echo ✓ Full version backup available
) else (
    echo ℹ No full version backup
)
goto menu

:exit
echo.
echo Thanks for using the Sports Ontology HermiT Performance Helper!
pause
exit /b 0