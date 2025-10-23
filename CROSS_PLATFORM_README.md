# Cross-Platform OBDA Testing Framework

This project now supports **Windows**, **Linux**, and **macOS** platforms.

## 🚀 Quick Start

### Option 1: Automatic Platform Detection (Recommended)

```bash
# Create database
cd database
./compile_and_run

# Run tests  
cd ../tests
./run-tests
```

### Option 2: Manual Platform-Specific Scripts

#### Windows
```cmd
cd database
compile_and_run.bat

cd ..\tests  
run-tests.bat
```

#### Linux/macOS
```bash
cd database
chmod +x compile_and_run.sh
./compile_and_run.sh

cd ../tests
chmod +x run-tests.sh
./run-tests.sh
```

## 📁 File Structure

```
sport_ontology/
├── database/
│   ├── compile_and_run        # 🔄 Cross-platform launcher
│   ├── compile_and_run.bat    # 🪟 Windows script  
│   ├── compile_and_run.sh     # 🐧🍎 Linux/Mac script
│   ├── CreateH2Database.java  # Java database creator
│   └── h2-2.4.240.jar        # H2 driver (for our Java code)
├── tests/
│   ├── run-tests              # 🔄 Cross-platform launcher
│   ├── run-tests.bat          # 🪟 Windows test runner
│   ├── run-tests.sh           # 🐧🍎 Linux/Mac test runner
│   ├── config/                # Test configuration
│   ├── sql/                   # SQL tests
│   ├── sparql/               # SPARQL tests  
│   └── integration/          # Integration tests
└── tools/
    └── ontop/
        ├── ontop.bat         # 🪟 Windows Ontop CLI
        ├── ontop             # 🐧🍎 Linux/Mac Ontop CLI
        └── jdbc/
            └── h2-2.4.240.jar  # H2 driver (for Ontop CLI)
```

### 📋 **Why Two H2 JARs?**

We maintain **two copies** of the H2 JAR for different purposes:

1. **`database/h2-2.4.240.jar`**: Used by our Java test code and database creation
2. **`tools/ontop/jdbc/h2-2.4.240.jar`**: Required by Ontop CLI (hardcoded classpath)

This is the **standard Ontop installation pattern** and cannot be avoided.

**⚠️ Important**: When updating H2, both JARs must be updated to the same version to avoid compatibility issues.

## ✅ Cross-Platform Compatibility

| Component | Windows | Linux | macOS | Notes |
|-----------|---------|-------|-------|-------|
| **H2 Database** | ✅ | ✅ | ✅ | Pure Java |
| **Ontop CLI** | ✅ | ✅ | ✅ | Includes both .bat and shell scripts |
| **Java Tests** | ✅ | ✅ | ✅ | Uses proper path handling |
| **Build Scripts** | ✅ | ✅ | ✅ | Platform-specific + auto-detection |

## 🔧 Requirements

- **Java 8+** (JDK or JRE)
- **H2 Database JAR** (included)
- **Ontop CLI** (included)

## 🧪 What the Tests Do

1. **SQL Tests**: Direct H2 database connection
2. **SPARQL Tests**: Via Ontop CLI with clean CSV output  
3. **Cross-Validation**: Ensures SQL and SPARQL results match
4. **Platform Detection**: Automatically uses correct scripts

## 📊 Expected Output

```
OBDA Integration Test Suite
========================================
✅ Running SQL Tests...
  total_teams     | SQL    | Expected:  7 | Actual:   7 | ✅ PASS

✅ Running SPARQL Tests (via Ontop CLI)...  
  total_teams     | SPARQL | Expected:  7 | Actual:   7 | ✅ PASS

Cross-Validation
--------------------------------------------------
Test            |    SQL | SPARQL | Match
--------------------------------------------------  
total_teams     |    YES |    YES | YES

SUMMARY
========================================
SQL Tests:    1 PASSED, 0 FAILED
SPARQL Tests: 1 PASSED, 0 FAILED  
STATUS: ALL TESTS PASSED
```

## 🐛 Troubleshooting

### Common Issues:

1. **"H2 jar not found"**
   - Ensure `h2-2.4.240.jar` exists in the `database/` folder

2. **"ontop command not found"**  
   - Check that `tools/ontop/ontop` (Linux/Mac) or `tools/ontop/ontop.bat` (Windows) exists

3. **Permission denied (Linux/Mac)**
   ```bash
   chmod +x run-tests.sh compile_and_run.sh
   ```

4. **Path issues**
   - The scripts automatically detect relative paths
   - Ensure you run from the correct directory

## 🔄 Adding More Tests

Edit `tests/config/TestRegistry.java` to add new test cases:

```java
new TestCase(
    "test_name",
    "SELECT COUNT(*) FROM TABLE_NAME",  // SQL query
    "PREFIX sports: <...> SELECT (COUNT(?x) as ?count) WHERE { ?x a sports:Entity }", // SPARQL
    expectedNumber,
    "EXACT"
)
```