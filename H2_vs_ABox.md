# H2 Database vs ABox Namespace Separation

## Overview
This document describes the namespace-based separation implemented to cleanly distinguish between database-driven data and ABox reasoning data in our sports ontology system.

## Problem Statement
Previously, both H2 database instances (via OBDA mappings) and ABox individuals appeared in SPARQL queries, making SQL↔SPARQL comparisons inconsistent and difficult to interpret.

## Solution: Namespace-Based Separation

### 🎯 **Namespace Design**
We've implemented two distinct namespaces:

1. **ABox Namespace** (`abox:`): `http://www.semanticweb.org/sports/abox#`
   - For reasoning test individuals  
   - Used by HermiT reasoner in Protégé
   - Static, curated data for consistency tests

2. **Database Namespace** (`data:`): `http://www.semanticweb.org/sports/data#`
   - For H2 database-driven instances
   - Generated via OBDA mappings
   - Dynamic data reflecting database state

### 🔧 **Implementation Details**

#### **Ontology Changes (`sport-ontology.owl`)**
- Added namespace prefixes:
  ```xml
  <Prefix name="abox" IRI="http://www.semanticweb.org/sports/abox#"/>
  <Prefix name="data" IRI="http://www.semanticweb.org/sports/data#"/>
  ```

- Updated all ABox individuals to use `abox:` namespace:
  ```xml
  <!-- Before -->
  <NamedIndividual IRI="#Erling_Haaland"/>
  
  <!-- After -->  
  <NamedIndividual IRI="http://www.semanticweb.org/sports/abox#Erling_Haaland"/>
  ```

#### **OBDA Mappings (`sport-ontology-mapping.ttl`)**
- Added namespace declarations:
  ```turtle
  @prefix data: <http://www.semanticweb.org/sports/data#> .
  @prefix abox: <http://www.semanticweb.org/sports/abox#> .
  ```

- Updated all database mapping templates to use `data:` namespace:
  ```turtle
  # Before
  rr:template "http://www.semanticweb.org/sports/ontology#person/{PERSON_ID}";
  
  # After
  rr:template "http://www.semanticweb.org/sports/data#person/{PERSON_ID}";
  ```

### 📊 **Data Distribution**

#### **ABox Individuals** (`abox:` namespace)
- **Players**: 8 individuals (Haaland, De Bruyne, Vinicius, Bellingham, Mbappe, Kane, Rico Lewis, Nico Paz)
- **Teams**: 4 individuals (Manchester City, Real Madrid, Bayern Munich, PSG)
- **Purpose**: Reasoning tests, class inference validation

#### **Database Instances** (`data:` namespace)  
- **Teams**: 7 teams (5 senior, 2 youth)
- **Persons**: 17 persons (12 players, 5 coaches)  
- **Contracts**: 25 contracts (13 player roles, 8 coach roles, 4 additional)
- **Purpose**: Real-world data simulation, OBDA system testing

## 🔍 **SPARQL Filtering Strategies**

### **Include Only Database Data**
```sparql
PREFIX data: <http://www.semanticweb.org/sports/data#>

SELECT ?player ?name WHERE {
    ?player a :Player ;
           :hasName ?name .
    FILTER(STRSTARTS(STR(?player), "http://www.semanticweb.org/sports/data#"))
}
```

### **Include Only ABox Data**  
```sparql
PREFIX abox: <http://www.semanticweb.org/sports/abox#>

SELECT ?player ?name WHERE {
    ?player a :Player ;
           :hasName ?name .
    FILTER(STRSTARTS(STR(?player), "http://www.semanticweb.org/sports/abox#"))
}
```

### **Exclude ABox Data (Database Only)**
```sparql
SELECT ?player ?name WHERE {
    ?player a :Player ;
           :hasName ?name .
    FILTER(!STRSTARTS(STR(?player), "http://www.semanticweb.org/sports/abox#"))
}
```

### **Alternative Filtering with REGEX**
```sparql
# Exclude ABox data
FILTER(!REGEX(STR(?player), "abox"))

# Include only database data  
FILTER(REGEX(STR(?player), "data"))
```

## 🧪 **Testing & Validation**

### **Expected Results After Implementation**

#### **SQL Queries** (Database only)
- Teams: 7
- Players: 12  
- Coaches: 5

#### **SPARQL Queries (Database only)**
```sparql
# Should match SQL results exactly
SELECT (COUNT(?team) AS ?teamCount) WHERE {
    ?team a :Team .
    FILTER(STRSTARTS(STR(?team), "http://www.semanticweb.org/sports/data#"))
}
# Expected: 7
```

#### **SPARQL Queries (ABox only)**
```sparql
# Should return only reasoning test data
SELECT (COUNT(?team) AS ?teamCount) WHERE {
    ?team a :Team .
    FILTER(STRSTARTS(STR(?team), "http://www.semanticweb.org/sports/abox#"))
}
# Expected: 4 (Manchester City, Real Madrid, Bayern Munich, PSG)
```

#### **SPARQL Queries (Combined)**
```sparql
# Should return both database + ABox data
SELECT (COUNT(?team) AS ?teamCount) WHERE {
    ?team a :Team .
}
# Expected: 11 (7 from database + 4 from ABox)
```

## 🔧 **Protégé Integration Tasks**

### **Steps to Use in Protégé**

1. **Open Ontology in Protégé**
   - Load `sport-ontology.owl`
   - Verify namespace prefixes are recognized

2. **Configure HermiT Reasoner**
   - Go to `Reasoner` → `HermiT`
   - Start reasoner to process ABox individuals
   - Verify inferred classifications (TopPlayer, YoungPlayer, etc.)

3. **Test Namespace Separation**
   - Use SPARQL query tab
   - Run filtering queries to verify separation works
   - Confirm ABox individuals appear in reasoning results

4. **SPARQL Query Examples for Protégé**
   ```sparql
   PREFIX : <http://www.semanticweb.org/sports/ontology#>
   PREFIX abox: <http://www.semanticweb.org/sports/abox#>
   PREFIX data: <http://www.semanticweb.org/sports/data#>
   
   # Test ABox reasoning
   SELECT ?player WHERE {
     ?player a :TopPlayer .
     FILTER(STRSTARTS(STR(?player), "http://www.semanticweb.org/sports/abox#"))
   }
   # Expected: abox:Haaland, abox:Bellingham, abox:Mbappe, abox:Kane, abox:Vinicius_Junior
   ```

### **Protégé Namespace Configuration**
The ontology now includes proper prefix declarations that Protégé will automatically recognize:
- `abox:` for ABox individuals
- `data:` for database instances  
- `:` for ontology classes and properties

### **Troubleshooting in Protégé**
- If ABox individuals don't appear: Check reasoner is running
- If namespaces aren't recognized: Verify prefix declarations in ontology header
- If SPARQL queries fail: Ensure correct namespace URIs in FILTER statements

## 🎯 **Benefits**

### **For Local Testing**
- **Clean SQL↔SPARQL comparisons**: Database queries now match exactly
- **Predictable results**: No interference between static ABox and dynamic database data
- **Debugging simplicity**: Easy to isolate data source issues

### **For Protégé Usage**
- **Dedicated reasoning data**: ABox individuals specifically for HermiT testing
- **Namespace clarity**: Clear visual distinction in individual browser
- **SPARQL flexibility**: Easy filtering for different analysis needs

### **For System Architecture**
- **Scalability**: Can add more data sources with their own namespaces
- **Maintainability**: Clear separation of concerns
- **Semantic clarity**: Follows semantic web best practices

## 📋 **Validation Checklist**

- [ ] ABox individuals use `abox:` namespace
- [ ] Database mappings use `data:` namespace  
- [ ] SPARQL filters work correctly
- [ ] SQL↔SPARQL counts match (when filtered)
- [ ] HermiT reasoning works in Protégé
- [ ] Namespace prefixes recognized in Protégé
- [ ] No broken references in ontology

## 🔄 **Future Enhancements**

- **Multiple Database Sources**: Additional namespaces for different databases
- **Temporal Data**: Time-based namespace separation
- **Data Provenance**: Source tracking via namespace metadata
- **Quality Indicators**: Namespace-based data quality annotations

This implementation provides a robust foundation for clean data separation while maintaining full semantic functionality in both local testing and Protégé environments.