@echo off
echo =======================================================
echo WEBVOWL + BACKGROUND CONVERTER LAUNCHER (ENHANCED)
echo =======================================================
echo Starting both processes in background with enhanced logging...
echo Press Ctrl+C to stop both processes
echo =======================================================

REM Cleanup any existing processes
echo [CLEANUP] Terminating any existing processes...
taskkill /F /IM node.exe 2>nul
taskkill /F /IM java.exe 2>nul
echo [CLEANUP] Killing processes on port 8000...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8000') do taskkill /F /PID %%a 2>nul

REM Create log files directory if it doesn't exist
if not exist "logs" mkdir logs

REM Clean previous logs
del /Q logs\*.log 2>nul

echo [LAUNCHER] Skipping compilation (assuming project is already compiled)...
REM mvn compile -q
REM if %ERRORLEVEL% NEQ 0 (
REM     echo [ERROR] Maven compilation failed!
REM     exit /b 1
REM )

REM Start WebVOWL server first and wait for it to complete its build
echo [LAUNCHER] Starting WebVOWL Server first...
cd tools\WebVOWL-master
start "WebVOWLServer" cmd /c "npm run webserver > ..\..\logs\webvowl-server.log 2>&1"
cd ..\..

echo [LAUNCHER] Waiting for WebVOWL server to finish build process...
timeout /t 15 /nobreak > nul

REM Now start background converter after WebVOWL has finished building
echo [LAUNCHER] Starting Background OWL Converter (after WebVOWL build)...
start "BackgroundConverter" cmd /c "mvn exec:java@background-converter > logs\background-converter.log 2>&1"

echo [LAUNCHER] Both processes started!
echo [LAUNCHER] Background Converter: Uses Official OWL2VOWL + Custom fallback
echo [LAUNCHER] WebVOWL Server: Usually runs on http://localhost:8000
echo [LAUNCHER] Logs available in:
echo [LAUNCHER]   - logs\background-converter.log
echo [LAUNCHER]   - logs\webvowl-server.log
echo =======================================================

:LOOP
echo.
echo [%TIME%] ===== BACKGROUND CONVERTER STATUS =====
powershell -Command "if (Test-Path 'logs\background-converter.log') { Get-Content 'logs\background-converter.log' | Select-String -Pattern 'converter|official|custom|found|copied|conversion|error|✅|❌|⚠️|🔄' -CaseSensitive:$false | Select-Object -Last 8 } else { Write-Output 'No converter status yet...' }"

echo.
echo [%TIME%] ===== WEBVOWL SERVER STATUS =====
powershell -Command "if (Test-Path 'logs\webvowl-server.log') { Get-Content 'logs\webvowl-server.log' | Select-String -Pattern 'server|listening|started|running|port|error' -CaseSensitive:$false | Select-Object -Last 5 } else { Write-Output 'No server status yet...' }"

echo.
echo [%TIME%] ===== SPORT ONTOLOGY FILES =====
dir /B tools\WebVOWL-master\deploy\data\sport-ontology* 2>nul || echo No sport ontology files found yet...
if exist "tools\WebVOWL-master\deploy\data\sport-ontology.json" (
    for %%F in ("tools\WebVOWL-master\deploy\data\sport-ontology.json") do echo   JSON: %%~zF bytes
)
if exist "tools\WebVOWL-master\deploy\data\sport-ontology.owl" (
    for %%F in ("tools\WebVOWL-master\deploy\data\sport-ontology.owl") do echo   OWL: %%~zF bytes
)

echo.
echo [%TIME%] ===== RECENT ACTIVITY =====
powershell -Command "if (Test-Path 'logs\background-converter.log') { Get-Content 'logs\background-converter.log' | Select-String -Pattern 'scan|periodic|found|copied|converted' -CaseSensitive:$false | Select-Object -Last 3 } else { Write-Output 'No recent activity...' }"

echo =======================================================
echo [LAUNCHER] Refreshing in 10 seconds... (Ctrl+C to stop)
echo [LAUNCHER] WebVOWL should be at: http://localhost:8000
timeout /t 10 /nobreak > nul
goto LOOP
