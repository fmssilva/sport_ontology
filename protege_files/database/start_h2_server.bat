@echo off
echo Starting H2 Database Server...
echo Database: C:/Users/franc/Desktop/RCR/projects/sport_ontology/protege_files/database/sport_db
echo Web Console: http://localhost:8082
echo H2 JAR: h2-2.4.240.jar (local)
echo.
if not exist "h2-2.4.240.jar" (
    echo ERROR: h2-2.4.240.jar not found in current directory
    echo Please ensure the H2 JAR file is present
    pause
    exit /b 1
)
java -cp "h2-2.4.240.jar" org.h2.tools.Server -tcp -web -tcpAllowOthers -webAllowOthers
pause
