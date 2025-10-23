#!/bin/bash

echo "OBDA Integration Test Suite"
echo "========================================"

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo
echo "Creating build directory..."
mkdir -p build

echo "Compiling test classes..."

# Determine H2 jar path (try both locations for cross-platform compatibility)
H2_JAR=""
if [ -f "../database/h2-2.4.240.jar" ]; then
    H2_JAR="../database/h2-2.4.240.jar"
elif [ -f "../tools/h2/h2-2.4.240.jar" ]; then
    H2_JAR="../tools/h2/h2-2.4.240.jar"
else
    echo "ERROR: H2 jar file not found!"
    exit 1
fi

# Compile in correct order (dependencies first) - output to build directory
javac -cp ".:$H2_JAR" -d build config/TestConfig.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build categories/TestCase.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build categories/integrity/IntegrityTests.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build categories/assumptions/AssumptionTests.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build categories/reasoning/ReasoningTests.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build integration/TestRegistry.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build integration/TestResult.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build sql/SQLTester.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build sparql/SPARQLTester.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

javac -cp "build:$H2_JAR" -d build integration/IntegrationTester.java
if [ $? -ne 0 ]; then echo "Compilation failed!"; exit 1; fi

echo "Compilation successful!"
echo
echo "Running integration tests..."
echo

java -cp "build:$H2_JAR" integration.IntegrationTester

echo
echo "Test execution completed!"
echo