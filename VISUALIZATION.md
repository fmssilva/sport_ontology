# 🎨 Ontology Graph Visualization

## Quick Start

### Automatic Visualization (Recommended)

```bash
# Windows
.\visualize-ontology.bat

# Or directly with Maven
mvn exec:java@visualize-ontology
```

### Custom Ontology File

```bash
# Visualize a specific ontology file
mvn exec:java@visualize-ontology -Dexec.args="path/to/your/ontology.owl"
```

## What It Does

The `GraphViewOfOntology` class automatically:

1. **📥 Downloads WebVOWL** if not already present in `tools/` folder
2. **📋 Copies your ontology** to WebVOWL's data folder
3. **🚀 Starts HTTP server** on port 8080
4. **🌐 Opens browser** to visualize your ontology

## Default Ontology

By default, it uses: `src/main/resources/ontology/sport-ontology.owl`

## WebVOWL Features

Once opened, you can:

- **🔍 Explore** class hierarchies visually
- **🔗 View relationships** between entities
- **📊 Filter** by class types
- **🎨 Customize** layout and appearance
- **💾 Export** diagrams as SVG/PNG
- **🔎 Search** for specific concepts

## Troubleshooting

### Port Already in Use
If port 8080 is busy, the application will show an error. Stop any existing servers and try again.

### Python Not Found
The visualization requires Python to serve WebVOWL files. Make sure Python is installed and in your PATH.

### Maven Not Found
Make sure Maven is installed and configured in your PATH.

## Manual Setup (Alternative)

If you prefer manual setup:

1. Download WebVOWL from: https://github.com/VisualDataWeb/WebVOWL
2. Extract to `tools/WebVOWL-master/`
3. Copy your `.owl` file to `tools/WebVOWL-master/deploy/data/`
4. Start HTTP server in the deploy folder:
   ```bash
   cd tools/WebVOWL-master/deploy
   python -m http.server 8080
   ```
5. Open: http://localhost:8080

## Advanced Usage

### Custom Port
Modify the `SERVER_PORT` constant in `GraphViewOfOntology.java`

### Different WebVOWL Version
Update the `WEBVOWL_URL` constant to use a different version

### Development Mode
For development, you can run the class directly from your IDE with custom arguments.