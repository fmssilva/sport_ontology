# Sport Ontology OBDA System

A complete Ontology-Based Data Access (OBDA) system for sports data using:
- **H2 Database** with AUTO_SERVER for concurrent access
- **Ontop CLI** for real SPARQL → SQL query rewriting  
- **R2RML mappings** for relational-to-RDF transformation
- **OWL ontology** for semantic data modeling

## 🚀 Quick Start

### Prerequisites
```bash
# Check if Maven is installed
mvn --version

# If not installed on Windows (PowerShell as admin):
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
choco install maven
```

### Standard Maven Commands

```bash
# Clean and compile
mvn clean compile

# Run all tests (includes automatic OBDA stack setup)
mvn clean test

# Run specific integration test
mvn test -Dtest=EngineIntegrationTest

# Build project (compile + test + package)
mvn clean install

# Development build (active by default)
mvn clean compile

# Production build (skip tests)
mvn clean install -Pprod
```

### What happens automatically:
1. **Ontop CLI 5.1.2** - Downloaded and configured automatically
2. **H2 JDBC Driver** - Copied to Ontop's classpath  
3. **Sports Database** - Created and populated with test data
4. **Full OBDA Testing** - SPARQL queries executed via real Ontop CLI

## 📊 Verified Results
- **7 teams** (5 senior, 2 youth)
- **12 players** with roles and contracts  
- **7 coaches** with team assignments
- **Full SPARQL → R2RML → SQL pipeline** working

## 🏗️ Project Structure

```
src/
├── main/java/engines/     # Database and SPARQL engines
├── main/resources/ontology/ # OWL ontology + R2RML mappings
└── test/java/integration/   # Full OBDA stack tests

tools/                     # Auto-managed Ontop CLI (don't edit)
```