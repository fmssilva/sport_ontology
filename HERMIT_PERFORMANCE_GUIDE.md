# HermiT Performance Optimization Guide for Protégé

## Problem Analysis
Your ontology contains complex axioms that cause HermiT to get stuck during "Building the class hierarchy" phase in Protégé GUI. The main performance bottlenecks are:

1. **EliteTeam** axiom with `ObjectMinCardinality cardinality="5"` referencing `StarPlayer`
2. **ProfessionalLeague** with `ObjectMinCardinality cardinality="16"` and nested intersections
3. Multiple complex `DataSomeValuesFrom` with `DatatypeRestriction` combinations

## Solutions (Choose One)

### Option 1: Use Simplified Ontology for Protégé
Use the simplified version: `sport-ontology-protege-optimized.owl`
- Removes complex cardinality constraints
- Simplifies datatype restrictions  
- Maintains logical structure but reduces reasoning complexity
- **Pros**: Fast reasoning, suitable for GUI exploration
- **Cons**: Less expressive than full ontology

### Option 2: Optimize Protégé Settings
1. **Increase Memory**:
   - Edit Protégé's `Protege.exe.vmoptions` file
   - Add: `-Xmx8g -Xms2g`

2. **Disable Auto-Reasoning**:
   - Go to Reasoner menu → Stop reasoner
   - Work with ontology first, then run reasoner manually
   - Use "Synchronize reasoner" instead of auto-sync

3. **Use Incremental Classification**:
   - Reasoner → Configure → Enable "Incremental classification"

### Option 3: Alternative Reasoner Strategy
1. **Start with Pellet**: Faster for initial consistency checking
2. **Use HermiT only for final validation**: Switch after structure is stable
3. **Use FaCT++**: Often faster than HermiT for complex axioms

### Option 4: Staged Reasoning Approach
1. **Load core ontology** (classes + properties only)
2. **Add simple axioms** (SubClassOf relationships)
3. **Add complex axioms gradually** (one EquivalentClass at a time)
4. **Test after each addition**

## Performance Monitoring
- Expected reasoning time: 2-5 seconds (with optimizations)
- If it takes >30 seconds, stop and use simplified version
- Monitor memory usage in Task Manager

## Quick Fix Commands
```bash
# Use the optimized version in Protégé
cp sport-ontology-protege-optimized.owl sport-ontology.owl

# Restore full version for testing
git checkout sport-ontology.owl
```

## Validation
- Your automated tests use the full ontology and work perfectly (134ms reasoning time)
- The simplified version maintains logical correctness for GUI exploration
- Both versions support the same OBDA mappings

## Recommendation
**Use the simplified version (`sport-ontology-protege-optimized.owl`) in Protégé for ontology development and visualization, while keeping the full version for automated testing and production OBDA queries.**