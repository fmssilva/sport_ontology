@echo off
echo OBDA Integration Test Suite
echo ========================================

REM Get the directory where this script is located and change to it
cd /d "%~dp0"

echo.
echo Creating build directory...
if not exist "build" mkdir build

echo Compiling test classes...

REM Find H2 jar in different possible locations
set H2_JAR=
if exist "..\database\h2-2.4.240.jar" set H2_JAR=..\database\h2-2.4.240.jar
if exist "..\tools\h2\h2-2.4.240.jar" set H2_JAR=..\tools\h2\h2-2.4.240.jar

if "%H2_JAR%"=="" (
    echo ERROR: H2 jar file not found!
    exit /b 1
)

REM Compile in correct order (dependencies first) - output to build directory
javac -cp ".;%H2_JAR%" -d build config\TestConfig.java
javac -cp "build;%H2_JAR%" -d build categories\TestCase.java
javac -cp "build;%H2_JAR%" -d build categories\integrity\IntegrityTests.java
javac -cp "build;%H2_JAR%" -d build categories\assumptions\AssumptionTests.java
javac -cp "build;%H2_JAR%" -d build categories\reasoning\ReasoningTests.java
javac -cp "build;%H2_JAR%" -d build integration\TestRegistry.java
javac -cp "build;%H2_JAR%" -d build integration\TestResult.java
javac -cp "build;%H2_JAR%" -d build sql\SQLTester.java
javac -cp "build;%H2_JAR%" -d build sparql\SPARQLTester.java
javac -cp "build;%H2_JAR%" -d build integration\IntegrationTester.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b 1
)

echo Compilation successful!
echo.
echo Running integration tests...
echo.

java -cp "build;%H2_JAR%" integration.IntegrationTester

echo.
echo Test execution completed!
echo.
