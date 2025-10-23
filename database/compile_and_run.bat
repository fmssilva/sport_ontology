@echo off
echo ========================================
echo Creating H2 Sports Database
echo ========================================

REM Check if H2 jar exists
if not exist h2-2.4.240.jar (
    echo ERROR: h2-2.4.240.jar not found!
    echo Please download from https://h2database.com/
    pause
    exit /b 1
)

echo Compiling CreateH2Database.java...
javac -cp h2-2.4.240.jar CreateH2Database.java

if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo Running CreateH2Database...
java -cp ".;h2-2.4.240.jar" CreateH2Database

if %ERRORLEVEL% equ 0 (
    echo.
    echo Database created successfully!
    echo File: sports-db.mv.db
    echo.
    echo Next steps:
    echo 1. Start H2 server: java -jar h2-2.4.240.jar
    echo 2. Connect in browser: http://localhost:8082
    echo 3. JDBC URL: jdbc:h2:./sports-db
) else (
    echo Error creating database!
)

pause