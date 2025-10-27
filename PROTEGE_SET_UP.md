# Protégé Setup Guide - Sport Ontology OBDA System

*Complete step-by-step instructions to load the Sport Ontology OBDA system in Protégé*

## Prerequisites
- Protégé 5.5+ installed with Ontop plugin
- Complete protégé_files package generated via `mvn exec:java@protege_files`

## Step 1: H2 Driver Configuration

**Choose ONE of the following options:**

### Option A: Automatic Plugin Installation (Recommended)
1. **Copy H2 JAR to Protégé plugins folder:**
   - Source: `database/h2-2.4.240.jar`
   - Destination: `[Protégé Installation]/plugins/h2-2.4.240.jar`
   - **Result:** OBDA file will work automatically (uses `./plugins/h2-2.4.240.jar` path)
   - **Restart Protégé** after copying the JAR

### Option B: Manual Configuration
1. **Keep JAR in database folder** (current location)
2. **Configure Protégé Preferences:**
   - Go to: **File → Preferences → JDBC Drivers**
   - Click **Add Driver**
   - Use configuration from `database/H2_Config.properties`:
     - **Driver Class:** `org.h2.Driver`
     - **JAR Path:** `[full absolute path to]/database/h2-2.4.240.jar`
     - **Description:** `H2 Database for Sport Ontology OBDA`
3. **⚠️ Important:** If using this option, you must update the OBDA file:
   - Open `ontology/sport-ontology.obda` in a text editor
   - Update the `driverJar` line in the `[SourceDeclaration]` section:
   - Change: `driverJar\t./plugins/h2-2.4.240.jar`
   - To: `driverJar\t[full absolute path to]/database/h2-2.4.240.jar`
   - This ensures Ontop can locate the H2 driver JAR file

## Step 2: Import Ontology with Automatic OBDA Loading

1. **Open Protégé**
2. **Import ontology:** File → Open → Select `ontology/sport-ontology.owl`
3. **Automatic OBDA loading:** Protégé will automatically detect and load `ontology/sport-ontology.obda`
4. **Verify loading:** Check that 40+ classes are loaded in the Classes tab

## Step 3: Alternative - Manual R2RML Import (Optional)

*If you prefer to use R2RML instead of the automatic OBDA loading:*

1. **Load ontology only:** File → Open → Select `ontology/sport-ontology.owl`
2. **Go to Ontop top tab** → Click **Import R2RML mapping**
3. **Select:** `ontology/sport-ontology-mapping.ttl`
4. **Configure connection** manually in Ontop Mappings → Connection parameters

## Step 4: Verify Database Connection

1. **Go to:** Window → Tabs → **Ontop Mappings**
2. **Check Connection parameters tab:**
   - Connection URL should be pre-filled from OBDA file
   - Driver: `org.h2.Driver`
   - Username: `sa`
   - Password: (empty)
3. **Test connection** - should show green/success status

## Step 5: Confirm Ontology Correctness

1. **Start HermiT reasoner:**
   - **Reasoner → HermiT → Start reasoner**
   - Wait for "Consistent" status
   - **Check logs:** Click log button in right bottom corner for any errors
2. **Start Pellet reasoner (alternative):**
   - **Reasoner → Pellet → Start reasoner**
   - Verify consistency and check for errors in logs

## Step 6: Confirm Mappings Work Correctly

1. **Validate mappings:**
   - Go to **Ontop Mappings** tab → **Mappings** tab
   - **Select all mappings** (Ctrl+A)
   - Click **Validate** button
   - Verify all mappings are valid (no errors shown)

2. **Test Ontop reasoner:**
   - **Reasoner → Ontop → Start reasoner**
   - This connects ontology to database via mappings
   - Check that reasoner synchronizes successfully

3. **Check mapping consistency:**
   - Go to **Ontop** top tab
   - Click **Check inconsistencies** button
   - Verify: "No inconsistencies found" or similar success message
   - This confirms ontology + mappings + database are all consistent

## Step 7: Test SPARQL Queries

1. **Go to Ontop SPARQL tab**
2. **Test with sample queries** from the `queries/sparql/` folder:
   - Copy queries from files like `validation_queries.sparql`
   - Execute queries to verify data is accessible
   - Check that results match expected database content

## Troubleshooting

**H2 Driver Issues:**
- Ensure JAR is in plugins/ folder OR properly configured in Preferences
- Restart Protégé after adding driver
- Check that paths use forward slashes in connection URL

**Connection Issues:**
- Verify database files exist in `database/` folder
- Check connection URL in `database/H2_Config.properties`
- Test connection in Ontop Mappings tab

**Mapping Issues:**
- Validate all mappings first
- Check reasoner logs for specific errors
- Verify database schema matches mapping expectations

## Files Overview

- **`ontology/sport-ontology.owl`** - Main OWL ontology
- **`ontology/sport-ontology.obda`** - OBDA mappings with DB connection
- **`ontology/sport-ontology-mapping.ttl`** - R2RML mappings (alternative)
- **`database/h2-2.4.240.jar`** - H2 JDBC driver
- **`database/H2_Config.properties`** - Connection configuration
- **`queries/sparql/`** - Sample SPARQL queries for testing
