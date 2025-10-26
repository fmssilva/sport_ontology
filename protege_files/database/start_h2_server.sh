#!/bin/bash
echo "Starting H2 Database Server..."
echo "Database: C:/Users/franc/Desktop/RCR/projects/sport_ontology/protege_files/database/sport_db"
echo "Web Console: http://localhost:8082"
echo "H2 JAR: h2-2.4.240.jar (local)"
echo
if [ ! -f "h2-2.4.240.jar" ]; then
    echo "ERROR: h2-2.4.240.jar not found in current directory"
    echo "Please ensure the H2 JAR file is present"
    exit 1
fi
java -cp "h2-2.4.240.jar" org.h2.tools.Server -tcp -web -tcpAllowOthers -webAllowOthers
