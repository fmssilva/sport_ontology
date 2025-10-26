# Protege Setup Guide - Sport Ontology

*Complete steps to load the sport ontology in Protege with H2 database*

## Prerequisites
- Protege 5.5+ installed
- H2 JDBC driver (included in database/ folder)

## Step 1: Load Ontology
1. Open Protege
2. **File -> Open**
3. Select: `ontology/sport-ontology.owl`
4. Verify 40+ classes are loaded

## Step 2: Start HermiT Reasoner
1. **Reasoner -> HermiT**
2. **Click "Start reasoner"**
3. Wait for "Consistent" status » check the log button on the right bottom corner

## Step 3: Connect to H2 DB
a: Set the H2 JAR location to the absolute path of h2-2.4.240.jar file
1. Open Protege
2. Go to File → Preferences
3. Navigate to JDBC Drivers tab
4. Click 'Add Driver'
5. Set Description: H2 Database for Sport Ontology OBDA
6. Set Driver Class: org.h2.Driver
7. Set H2 JAR location: Browse to h2-2.4.240.jar file or insert the absolute path
8. Click OK to save driver and check if status is ready

####
b: Set the connection to the H2 database file using the following details:
1. Go to Window → Tabs → Ontop Mappings (and in view go and add the ontop view to see the tabs in the main window)
2. Click 'Connection parameters' tab
3. Select the H2 driver class: org.h2.Driver
4. Enter the connection URL from H2_Config.properties
5. Username: sa
6. Password: (leave empty)
7. Test connection and save
### Step 3.1: Using H2 as a built-in Protege backend or shared setup
If you want Protege to use H2 as an internal or shared database backend (not just as an external connection):
a. Copy the file `database/h2-2.4.240.jar` into your Protege `plugins` folder.
b. Restart Protege completely to load the new driver.

## Step 4: Load OBDA Mappings
1. In **Ontop top tab** click **import R2RML mapping**
2. Select: `ontology/sport-ontology-mapping.ttl`
3. Verify R2RML mappings are loaded

4. Click the **Validate** button to check the mapping validity.
5. Open some mappings and execute the given sample queries to test their functionality.
## Step 5: Confirm Ontology + Mapping + H2 DB correctness
1. Run Ontop Reasoner by clicking **Reasoner -> Ontop Reasoner -> Start Reasoner**
2. Ensure no errors occur during reasoning - check the log for any issues
3. Click in **ontop top tab** in **check consistency** button to verify if the seeded data in H2 DB is consistent for disjoint and functional properties
## Step 6: Test SPARQL Queries
1. On **ontop SPARQL tab** write the queries to test the ontology
2. Copy queries from the **queries folder** into the SPARQL tab and execute them
