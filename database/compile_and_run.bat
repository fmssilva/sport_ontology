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
    echo - To start the H2 server: 
    echo        run: java -jar h2-2.4.240.jar
    echo        Connect in browser: http://localhost:8082
    echo        Use the local path in the JDBC URL: jdbc:h2:./sports-db
    echo - To start ontop server from protege,
    echo        if protege is in a different folder,
    echo        a: copy the sports-db.mv.db file to protege folder and config the URL with the local path
    echo        b: or change the JDBC URL to the absolute path of the database file
) else (
    echo Error creating database!
)

