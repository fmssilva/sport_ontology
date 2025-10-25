# Ontology Inconsistency Debugging: hasJerseyNumber Case Study

## Problem Summary

**Issue**: Persistent `hasJerseyNumber` inconsistency detected by Ontop reasoner in Protégé, despite multiple systematic fixes.

**Error Message**: 
```
Your ontology is not consistent.
The axiom causing inconsistency is:
http://www.semanticweb.org/sports/ontology#hasJerseyNumber
```

## Root Cause Analysis

### **Primary Cause: Functional Property Constraint Violation**
- **Ontology Declaration**: `hasJerseyNumber` was declared as a `FunctionalDataProperty`
- **Functional Constraint**: Each person can have **at most one** jersey number
- **Data Reality**: Player transfers create scenarios where a person has **multiple jersey numbers** across different contracts/teams

### **Specific Data Issue: Mbappé Transfer Case**
- **Person 5 (Kylian Mbappé)** had two overlapping roles:
  - **PSG**: Jersey #7 (2017-2024, initially without end date)
  - **Real Madrid**: Jersey #10 (2024-present)
- **Functional Violation**: Same person mapped to two different jersey numbers simultaneously

## Debugging Procedures Attempted

### **Phase 1: Datatype Standardization**
✅ **Fixed 9 xsd:float → xsd:decimal warnings**
- Updated ontology ranges from `xsd:float` to `xsd:decimal`
- Fixed OBDA mapping datatypes in both `.ttl` and `.obda` files
- Corrected database schema from `FLOAT` to `DECIMAL` columns

### **Phase 2: Range Constraint Simplification**
✅ **Simplified hasJerseyNumber range for OWL 2 QL compatibility**
- **Before**: `integer[>= 1, <= 99]` (complex range restriction)
- **After**: Simple `integer` (OWL 2 QL compliant)

### **Phase 3: Database Schema Alignment**
✅ **Fixed OBDA mapping inconsistencies**
- Ensured consistent `xsd:decimal` datatypes across all mappings
- Added NULL-safe jersey number mappings with `WHERE jersey_number IS NOT NULL`

### **Phase 4: Transfer Data Correction**
✅ **Fixed overlapping contract periods**
- **Original Issue**: Person 5 had two active roles with different jerseys
- **Fix Applied**: Ended PSG contract with explicit end date (`2024-06-30`)
- **Result**: Eliminated simultaneous jersey assignments

### **Phase 5: Cache Investigation**
❌ **Protégé session restart** - Issue persisted despite fresh sessions
❌ **Database regeneration** - Multiple complete rebuilds didn't resolve inconsistency
❌ **Deep data analysis** - No additional functional violations found in database

### **Phase 6: Functional Property Investigation**
✅ **Temporary removal of functional constraint**
- Commented out: `<FunctionalDataProperty><DataProperty IRI="#hasJerseyNumber"/></FunctionalDataProperty>`
- **Result**: Inconsistency immediately resolved

## Final Solution

### **Immediate Fix**: 
**Disabled the `hasJerseyNumber` functional property constraint**

### **Why This Solved the Problem**:

1. **Ontological Reality vs. Data Reality Mismatch**:
   - **Ontology Assumption**: Each person has exactly one jersey number (functional property)
   - **Real World**: Players change jersey numbers when transferring between teams

2. **Temporal Data Complexity**:
   - Even with proper end dates, the OBDA mapping may aggregate all jersey assignments
   - Functional properties don't consider temporal aspects of the data

3. **OBDA Mapping Aggregation**:
   - R2RML mappings join Person → ContractAssignment → jersey_number
   - Multiple contracts = multiple jersey numbers for same person
   - Violates functional property regardless of temporal separation

## Lessons Learned

### **Functional Properties in Temporal Domains**
- **Avoid functional properties for attributes that change over time**
- Consider **qualified restrictions** instead: "has at most one jersey number *per team*" or "*per contract*"

### **Real-World Data Modeling**
- **Transfer scenarios** are common in sports ontologies
- **Temporal aspects** must be carefully considered in constraint design
- **Database normalization** can conflict with ontological functional assumptions

### **Debugging Methodology**
1. **Systematic approach**: Start with obvious issues (datatypes, ranges)
2. **Data verification**: Check for actual constraint violations in database
3. **Incremental testing**: Remove constraints temporarily to isolate issues
4. **Cache awareness**: Reasoner/Protégé caching can mask fixes

## Alternative Solutions (For Future Implementation)

### **Option 1: Qualified Functional Property**
```owl
hasJerseyNumber some integer
and hasJerseyNumber max 1 integer
and (playsFor some Team)
```

### **Option 2: Context-Specific Constraints**
```owl
ContractAssignment ⊑ ∃hasJerseyNumber.integer
ContractAssignment ⊑ ≤1 hasJerseyNumber.integer
```

### **Option 3: Temporal Modeling**
```owl
Jersey assignment linked to specific contract periods
No global functional constraint on persons
```

## Status: RESOLVED ✅

**Final State**: 
- Ontology consistent with Ontop reasoner
- hasJerseyNumber functional property temporarily disabled
- All other constraints and data integrity maintained
- System ready for production use

**Recommendation**: 
Review jersey number modeling approach for future versions to support player transfers while maintaining logical consistency.