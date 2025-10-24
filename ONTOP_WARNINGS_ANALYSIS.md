# 🚨 ONTOP OWL 2 QL WARNINGS ANALYSIS & FIXES

## 📋 **EXECUTIVE SUMMARY**

Analysis of **27 Ontop warnings** when using the ontology with OBDA/SPARQL queries. These warnings indicate constructs that exceed OWL 2 QL expressiveness but are essential for demonstrating advanced DL reasoning capabilities.

**Recommendation**: Fix **7 easy datatype issues**, keep **20 complex DL constructor warnings** as they demonstrate valuable reasoning patterns beyond OWL 2 QL.

---

## ✅ **COMPLETE FIX APPLIED: DATATYPE CONSISTENCY RESOLVED**

### **Problem Solved**: Ontology Inconsistency Error
```
ERROR: Cannot do reasoning with inconsistent ontologies!
Reason for inconsistency: Literal value "1.8E8"^^float does not belong to datatype decimal.
```

### **Root Cause**: Mixed datatype declarations
When we changed `xsd:float` → `xsd:decimal` in property ranges, we had leftover literal values still using `^^float` datatypes, causing inconsistency.

### **Complete Solution Applied**:

#### **1. Fixed Property Range Declarations** (9 properties)
```xml
<!-- ALL FIXED -->
<DataPropertyRange>
    <DataProperty IRI="#hasMarketValue"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>
```

#### **2. Fixed Complex Class Definition Datatypes**
```xml
<!-- TopPlayer definition - FIXED -->
<DatatypeRestriction>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
    <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#decimal">100000000</Literal>
</DatatypeRestriction>

<!-- TopTeam definition - FIXED -->
<DatatypeRestriction>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
    <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#decimal">50000000</Literal>
</DatatypeRestriction>
```

#### **3. Fixed All ABox Individual Literals** (8 players)
```xml
<!-- ALL PLAYER MARKET VALUES FIXED -->
<DataPropertyAssertion>
    <DataProperty IRI="#hasMarketValue"/>
    <NamedIndividual abbreviatedIRI="abox:ABox_Erling_Haaland"/>
    <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#decimal">180000000</Literal>
</DataPropertyAssertion>

<!-- Converted from: -->
<!-- <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#float">1.8E8</Literal> -->
```

#### **4. Fixed Range Restrictions**
```xml
<!-- hasWinPercentage range - FIXED -->
<DatatypeRestriction>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
    <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#decimal">0</Literal>
    <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#decimal">100</Literal>
</DatatypeRestriction>
```

### **Verification**: Complete Consistency
```bash
# No more float references found
grep -r "xsd:float" src/main/resources/ontology/
# (no results)

grep -r "XMLSchema#float" src/main/resources/ontology/
# (no results)
```

**Status**: ✅ **Ontology now fully consistent** - All datatype references unified to `xsd:decimal`

### **Additional Fix**: OBDA Mapping Consistency
**Problem**: After fixing ontology datatypes, OBDA mapping file still had `xsd:float` references:
```
ERROR: hasMarketValue is declared as xsd:decimal in the ontology, 
but is used as xsd:float in the triplesMap
```

**Solution Applied**:
```turtle
# FIXED in sport-ontology-mapping.ttl
rr:predicate :hasMarketValue;
rr:objectMap [ rr:column "MARKET_VALUE"; rr:datatype xsd:decimal ]  # was xsd:float

rr:predicate :hasSalary;
rr:objectMap [ rr:column "SALARY"; rr:datatype xsd:decimal ]  # was xsd:float
```

**Final Verification**:
```bash
# No float references in any files
grep -r "xsd:float" src/main/resources/ontology/
grep -r "xsd:float" deliverables/ontology/
# (no results - fully consistent)
```

**Status**: ✅ **OBDA Mapping and Ontology fully synchronized** - Ontop reasoner works without errors

### **Problem**: `xsd:float` not supported in OWL 2 QL

**Affected Properties** (7 warnings):
- `hasAnnualSalary Range: float`
- `hasBonusClause Range: float` 
- `hasHeight Range: float`
- `hasTransferFee Range: float`
- `hasSalary Range: float`
- `hasWeeklySalary Range: float`
- `hasWeight Range: float`
- `hasReleaseClause Range: float`
- `hasMarketValue Range: float`

**Solution**: Change `xsd:float` → `xsd:decimal` for OWL 2 QL compatibility

**Why this helps**: 
- `xsd:decimal` is supported in OWL 2 QL
- `xsd:float` is not supported in OWL 2 QL
- Semantically equivalent for our use cases (monetary values, measurements)
- **9 warnings eliminated** with this simple change

### **Implementation Fix**:
```xml
<!-- BEFORE (causes warnings) -->
<DataPropertyRange>
    <DataProperty IRI="#hasMarketValue"/>
    <Datatype abbreviatedIRI="xsd:float"/>
</DataPropertyRange>

<!-- AFTER (OWL 2 QL compatible) -->
<DataPropertyRange>
    <DataProperty IRI="#hasMarketValue"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>
</DataPropertyRange>
```

---

## 🎯 **SHOULD KEEP: VALUABLE DL CONSTRUCTORS (20 warnings)**

These warnings demonstrate **advanced DL reasoning capabilities beyond OWL 2 QL** that are essential for our ontology's expressiveness:

### **1. ObjectIntersectionOf (⊓) - Complex Class Definitions**

**Warnings to KEEP**:
```
TopPlayer EquivalentTo Player and (hasMarketValue some float[>= 1.0E8f])
YoungPlayer EquivalentTo Player and (hasAge some integer[< 23])
VeteranPlayer EquivalentTo Player and (hasAge some integer) and (hasYearsExperience some integer)
StarPlayer EquivalentTo Player and (hasInternationalCaps some integer) and (hasMarketValue some float)
TopTeam EquivalentTo Team and (hasPlayer some (Player and (hasMarketValue some float[>= 5.0E7f])))
EliteTeam EquivalentTo Team and (hasPlayer some TopPlayer) and (hasStadiumCapacity some integer[>= 50000])
TopCoach EquivalentTo Coach and (hasTrophiesWon some integer) and (hasYearsCoaching some integer)
```

**Why KEEP**: 
- **Automatic classification**: Players/teams automatically inherit appropriate types
- **Complex reasoning**: Combines multiple conditions with logical AND
- **Business value**: Represents real-world classification patterns
- **Beyond ALC**: Demonstrates ObjectIntersectionOf constructor

### **2. DatatypeRestriction - Numeric Range Constraints**

**Warnings to KEEP**:
```
hasJerseyNumber Range: integer[>= 1 , <= 99]
hasWinPercentage Range: float[>= 0.0f , <= 100.0f]
TopPlayer EquivalentTo Player and (hasMarketValue some float[>= 1.0E8f])
```

**Why KEEP**:
- **Data validation**: Ensures realistic value ranges
- **Business rules**: Jersey numbers 1-99, win percentage 0-100%
- **DL expressiveness**: Shows datatype restriction capabilities
- **Reasoning advantage**: Automatic constraint checking

### **3. ObjectUnionOf (⊔) - Enumeration Types**

**Warnings to KEEP**:
```
playsPosition Range Defender or Forward or Goalkeeper or Midfielder
preferredPosition Range Defender or Forward or Goalkeeper or Midfielder
specialistIn Range Defender or Forward or Goalkeeper or Midfielder
canPlayPosition Range Defender or Forward or Goalkeeper or Midfielder
```

**Why KEEP**:
- **Controlled vocabulary**: Enforces valid position types
- **Ontological modeling**: Represents closed enumeration
- **Type safety**: Prevents invalid position assignments
- **Domain knowledge**: Captures football position structure

### **4. Property Chains (∘) - Transitive Reasoning**

**Warning to KEEP**:
```
playsFor o competesIn SubPropertyOf: participatesIn
```

**Why KEEP**:
- **Automatic inference**: Player → Team → League relationships derived automatically
- **Query simplification**: Complex JOINs become simple property paths
- **Business logic**: "Players participate in leagues through their teams"
- **Advanced reasoning**: Property chain constructor demonstration

### **5. Existential Restrictions with Complex Classes**

**Warnings to KEEP**:
```
hasContract some Contract SubClassOf Coach or Player
ProfessionalLeague EquivalentTo League and (hasTeam some Team)
HomegrownPlayer EquivalentTo Player and (playsFor some Team)
```

**Why KEEP**:
- **Domain constraints**: Only coaches and players can have contracts
- **Logical consistency**: Maintains ontological integrity
- **Complex reasoning**: Existential quantification with class restrictions
- **Business rules**: Captures real-world constraints

---

## 📊 **WARNING CATEGORIES BREAKDOWN**

| Category | Count | Action | Reasoning |
|----------|-------|--------|-----------|
| **Datatype Issues** | 9 | ✅ **FIX** | Simple xsd:float → xsd:decimal |
| **ObjectIntersectionOf** | 8 | 🎯 **KEEP** | Essential complex class definitions |
| **DatatypeRestriction** | 3 | 🎯 **KEEP** | Valuable range constraints |
| **ObjectUnionOf** | 4 | 🎯 **KEEP** | Important enumeration types |
| **Property Chains** | 1 | 🎯 **KEEP** | Advanced reasoning demonstration |
| **Complex Restrictions** | 2 | 🎯 **KEEP** | Domain constraint modeling |

**Total**: 27 warnings → **9 fixable**, **18 valuable to keep**

---

## 🔧 **RECOMMENDED FIXES**

### **Fix 1: Replace xsd:float with xsd:decimal**

**File**: `sport-ontology.owl`  
**Lines**: Multiple DataPropertyRange declarations

```xml
<!-- Fix these 9 properties -->
<DataPropertyRange>
    <DataProperty IRI="#hasAnnualSalary"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasBonusClause"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasHeight"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasMarketValue"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasReleaseClause"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasSalary"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasTransferFee"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasWeeklySalary"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>

<DataPropertyRange>
    <DataProperty IRI="#hasWeight"/>
    <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- was xsd:float -->
</DataPropertyRange>
```

### **Fix 2: Update Complex Class Definitions (if desired)**

**Only if you want OWL 2 QL compatibility** (NOT recommended for our use case):

```xml
<!-- Current (expressive but causes warnings) -->
<EquivalentClasses>
    <Class IRI="#TopPlayer"/>
    <ObjectIntersectionOf>
        <Class IRI="#Player"/>
        <DataSomeValuesFrom>
            <DataProperty IRI="#hasMarketValue"/>
            <DatatypeRestriction>
                <Datatype abbreviatedIRI="xsd:decimal"/>  <!-- changed from float -->
                <FacetRestriction facet="http://www.w3.org/2001/XMLSchema#minInclusive">
                    <Literal datatypeIRI="http://www.w3.org/2001/XMLSchema#decimal">100000000</Literal>
                </FacetRestriction>
            </DatatypeRestriction>
        </DataSomeValuesFrom>
    </ObjectIntersectionOf>
</EquivalentClasses>

<!-- Alternative OWL 2 QL approach (less expressive) -->
<SubClassOf>
    <Class IRI="#TopPlayer"/>
    <Class IRI="#Player"/>
</SubClassOf>
<!-- Note: Loses automatic classification capability -->
```

---

## 💡 **STRATEGIC RECOMMENDATIONS**

### **Apply Only Datatype Fixes**

**Recommended Approach**:
1. ✅ **Fix 9 datatype warnings** (xsd:float → xsd:decimal)
2. 🎯 **Keep 18 DL constructor warnings** (demonstrate advanced reasoning)
3. 📝 **Document in ontology** that OWL 2 QL warnings are intentional

**Benefits**:
- **Reduces warning noise** (67% fewer warnings)
- **Maintains expressiveness** for advanced reasoning
- **Keeps valuable DL constructors** for demonstration
- **Improves OBDA compatibility** where possible

### **Don't Simplify Complex Classes**

**Why NOT to fix ObjectIntersectionOf warnings**:
- **Loses automatic classification**: `TopPlayer`, `YoungPlayer` etc. become manual
- **Reduces reasoning capabilities**: No automatic type inference
- **Breaks test cases**: Our reasoning tests depend on these constructs
- **Academic value**: Demonstrates DL expressiveness beyond OWL 2 QL

### **Documentation Strategy**

Add to ontology header:
```xml
<!-- 
ONTOP OWL 2 QL WARNINGS: Intentional advanced DL constructs
- ObjectIntersectionOf: Automatic player/team classification  
- DatatypeRestriction: Business rule validation
- Property Chains: Transitive relationship inference
- ObjectUnionOf: Position enumeration types

These warnings demonstrate DL expressiveness beyond OWL 2 QL 
and are essential for our reasoning test demonstrations.
-->
```

---

## 🎯 **IMPLEMENTATION IMPACT**

### **Before Fixes** (27 warnings):
```
WARN: hasMarketValue Range: float (unsupported datatype: XSD_FLOAT)
WARN: TopPlayer EquivalentTo Player and (hasMarketValue some float[>= 1.0E8f])
WARN: playsPosition Range Defender or Forward or Goalkeeper or Midfielder
WARN: playsFor o competesIn SubPropertyOf: participatesIn
... 23 more warnings
```

### **After Datatype Fixes** (18 warnings):
```
WARN: TopPlayer EquivalentTo Player and (hasMarketValue some decimal[>= 100000000])
WARN: playsPosition Range Defender or Forward or Goalkeeper or Midfielder  
WARN: playsFor o competesIn SubPropertyOf: participatesIn
... 15 more warnings (all valuable DL constructors)
```

**Result**: **33% reduction in warnings** while maintaining full reasoning expressiveness.

---

## 📚 **TECHNICAL JUSTIFICATION**

### **Why xsd:decimal is Better for OBDA**

| Aspect | xsd:float | xsd:decimal |
|--------|-----------|-------------|
| **OWL 2 QL Support** | ❌ No | ✅ Yes |
| **Precision** | Approximate | Exact |
| **Financial Data** | ❌ Rounding errors | ✅ Exact monetary values |
| **Database Mapping** | Complex | Direct |
| **SPARQL Queries** | ⚠️ Warnings | ✅ Clean |

### **DL Constructor Educational Value**

The 18 remaining warnings demonstrate:
- **ObjectIntersectionOf**: Complex class expressions (⊓)
- **DatatypeRestriction**: Numeric constraints with facets
- **ObjectUnionOf**: Enumeration types (⊔)  
- **Property Chains**: Transitive inference (∘)
- **Existential Restrictions**: ∃R.C constructs

**Academic Benefit**: Shows DL expressiveness **beyond ALC + OWL 2 QL**

---

**🎯 CONCLUSION**: Fix the 9 simple datatype issues (xsd:float → xsd:decimal) to reduce warning noise while preserving the 18 valuable DL constructor warnings that demonstrate advanced reasoning capabilities essential for our ontology's educational and practical value.**