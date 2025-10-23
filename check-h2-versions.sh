#!/bin/bash
# H2 JAR Version Synchronization Script
# Ensures both H2 JARs are the same version

echo "🔍 Checking H2 JAR versions..."

# Paths to the two H2 JARs
DATABASE_H2="database/h2-2.4.240.jar"
ONTOP_H2="tools/ontop/jdbc/h2-2.4.240.jar"

# Check if both files exist
if [[ ! -f "$DATABASE_H2" ]]; then
    echo "❌ Missing: $DATABASE_H2"
    exit 1
fi

if [[ ! -f "$ONTOP_H2" ]]; then
    echo "❌ Missing: $ONTOP_H2"
    exit 1
fi

# Get file sizes (quick version check)
DATABASE_SIZE=$(stat -f%z "$DATABASE_H2" 2>/dev/null || stat -c%s "$DATABASE_H2" 2>/dev/null)
ONTOP_SIZE=$(stat -f%z "$ONTOP_H2" 2>/dev/null || stat -c%s "$ONTOP_H2" 2>/dev/null)

echo "📊 File sizes:"
echo "  Database H2: $DATABASE_SIZE bytes"
echo "  Ontop H2:    $ONTOP_SIZE bytes"

if [[ "$DATABASE_SIZE" == "$ONTOP_SIZE" ]]; then
    echo "✅ H2 JARs appear to be the same version (same size)"
else
    echo "⚠️  H2 JARs have different sizes - they may be different versions!"
    echo ""
    echo "🔧 To fix this:"
    echo "   1. Determine which version you want to keep"
    echo "   2. Copy the correct JAR to replace the other"
    echo "   3. Update filenames in scripts if version number changed"
fi

echo ""
echo "📂 Locations:"
echo "  For Java tests: $DATABASE_H2"
echo "  For Ontop CLI:  $ONTOP_H2"