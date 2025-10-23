#!/bin/bash

echo "========================================"
echo "Creating H2 Sports Database"
echo "========================================"

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if H2 jar exists
H2_JAR="h2-2.4.240.jar"
if [ ! -f "$H2_JAR" ]; then
    echo "ERROR: $H2_JAR not found!"
    echo "Please download from https://h2database.com/"
    exit 1
fi

echo "Compiling CreateH2Database.java..."
javac -cp "$H2_JAR" CreateH2Database.java

if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo "Running CreateH2Database..."
java -cp ".:$H2_JAR" CreateH2Database

if [ $? -eq 0 ]; then
    echo
    echo "Database created successfully!"
    echo "File: sports-db.mv.db"
    echo
    echo "Next steps:"
    echo "»» To start the H2 server:"
    echo "        run: java -jar $H2_JAR"
    echo "        Connect in browser: http://localhost:8082"
    echo "        Use the local path in the JDBC URL: jdbc:h2:./sports-db"
    echo "»» To start ontop server from protege,"
    echo "        if protege is in a different folder,"
    echo "        a) copy the sports-db.mv.db file to protege folder and config the URL with the local path"
    echo "        b) or change the JDBC URL to the absolute path of the database file"
else
    echo "Error creating database!"
    exit 1
fi