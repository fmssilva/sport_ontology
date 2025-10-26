package graph_ontology;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.awt.Desktop;

/**
 * Professional WebVOWL Ontology Visualization System
 * 
 * This class provides automated ontology visualization using WebVOWL with proper webpack build process.
 * Unlike the fallback approach, this uses the full WebVOWL development server with grunt/webpack.
 * 
 * Features:
 * - Downloads and sets up WebVOWL automatically
 * - Uses proper npm/grunt build system
 * - Starts webpack development server
 * - Copies ontology files automatically
 * - Opens browser with full WebVOWL interface
 * 
 * Usage:
 * mvn exec:java -Dexec.mainClass="graph_ontology.WebVOWLVisualization"
 * mvn exec:java -Dexec.mainClass="graph_ontology.WebVOWLVisualization" -Dexec.args="path/to/ontology.owl"
 */
public class WebVOWLVisualization {
    
    private static final String WEBVOWL_URL = "https://github.com/VisualDataWeb/WebVOWL/archive/refs/heads/master.zip";
    private static final String DEFAULT_ONTOLOGY = "src/main/resources/ontology/sport-ontology.owl";
    private static final String TOOLS_DIR = "tools";
    private static final String WEBVOWL_DIR = "tools/WebVOWL-master";
    private static final String DATA_DIR = "tools/WebVOWL-master/deploy/data";
    private static final int SERVER_PORT = 8000;
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Professional WebVOWL Ontology Visualizer ===");
            System.out.println("Automated Setup with Webpack Build Process");
            System.out.println();
            
            // Determine ontology file path
            String ontologyPath = args.length > 0 ? args[0] : DEFAULT_ONTOLOGY;
            Path ontologyFile = Paths.get(ontologyPath);
            
            if (!Files.exists(ontologyFile)) {
                System.err.println("❌ Ontology file not found: " + ontologyPath);
                System.err.println("Please check the file path or place your ontology at: " + DEFAULT_ONTOLOGY);
                return;
            }
            
            System.out.println("📁 Using ontology file: " + ontologyFile.toAbsolutePath());
            
            // Step 1: Setup WebVOWL
            setupWebVOWL();
            
            // Step 2: Install dependencies
            installDependencies();
            
            // Step 2.5: Configure WebVOWL for dynamic ontology loading
            configureWebVOWLForDynamicLoading();
            
            // Step 3: Start WebVOWL server with webpack (this will clean deploy folder)
            startWebVOWLServer();
            
            // Step 4: Convert all OWL ontologies to JSON format AFTER webpack build
            // This ensures our JSON files don't get cleaned by the build process
            System.out.println("⏳ Waiting for webpack build to complete...");
            Thread.sleep(3000); // Additional delay to ensure build is fully complete
            convertAllOntologiesToJSON();
            
            // Verify files exist after conversion
            System.out.println("🔍 Verifying JSON files persist...");
            Thread.sleep(2000);
            verifyJSONFiles();
            
            // Step 5: Server is now running in background, exit Maven process
            System.out.println();
            System.out.println("✅ Setup complete!");
            System.out.println("📊 To view your Sport Ontology:");
            System.out.println("   🔗 DIRECT ACCESS: http://localhost:" + SERVER_PORT);
            System.out.println("   📁 OR use file picker: Click 'Select ontology file' → choose any .owl file");
            System.out.println();
            System.out.println("🌐 WebVOWL is running in background with live reload");
            System.out.println("🛑 To stop the server, close the browser or kill the Node.js process");
            
            if (!isWindows()) {
                System.out.println("📄 Server logs: " + Paths.get(WEBVOWL_DIR) + "/webvowl-server.log");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Setup WebVOWL by downloading and extracting if needed
     */
    private static void setupWebVOWL() throws IOException {
        Path webvowlDir = Paths.get(WEBVOWL_DIR);
        
        if (Files.exists(webvowlDir) && Files.exists(webvowlDir.resolve("package.json"))) {
            System.out.println("✅ WebVOWL already available");
            return;
        }
        
        System.out.println("📥 Downloading WebVOWL from GitHub...");
        
        // Create tools directory
        Files.createDirectories(Paths.get(TOOLS_DIR));
        
        // Download WebVOWL
        downloadWebVOWL();
        
        System.out.println("✅ WebVOWL setup complete");
    }
    
    /**
     * Download and extract WebVOWL
     */
    private static void downloadWebVOWL() throws IOException {
        try {
            URI uri = URI.create(WEBVOWL_URL);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            
            // Set user agent to avoid blocking
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (WebVOWL Automation)");
            
            Path zipFile = Paths.get(TOOLS_DIR, "webvowl.zip");
            
            System.out.print("📦 Downloading");
            
            // Download with progress indication
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(zipFile.toFile())) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    if (totalBytes % (1024 * 1024) == 0) {
                        System.out.print(".");
                    }
                }
                System.out.println(" ✅");
            }
            
            // Extract
            System.out.println("📦 Extracting WebVOWL...");
            extractZip(zipFile, Paths.get(TOOLS_DIR));
            
            // Clean up
            Files.deleteIfExists(zipFile);
            
        } catch (Exception e) {
            throw new IOException("Failed to download WebVOWL: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract ZIP file
     */
    private static void extractZip(Path zipFile, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = destDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
    
    /**
     * Install npm dependencies
     */
    private static void installDependencies() throws IOException {
        Path webvowlDir = Paths.get(WEBVOWL_DIR);
        Path nodeModules = webvowlDir.resolve("node_modules");
        
        if (Files.exists(nodeModules)) {
            System.out.println("✅ Dependencies already installed");
            return;
        }
        
        System.out.println("📦 Installing npm dependencies...");
        
        try {
            // Build npm command for Windows
            String[] npmCommand = isWindows() ? 
                new String[]{"cmd", "/c", "npm", "--version"} : 
                new String[]{"npm", "--version"};
            
            // Check if npm is available
            ProcessBuilder npmCheck = new ProcessBuilder(npmCommand);
            npmCheck.directory(webvowlDir.toFile());
            Process npmProcess = npmCheck.start();
            npmProcess.waitFor();
            
            if (npmProcess.exitValue() != 0) {
                throw new IOException("npm is not installed or not in PATH. Please install Node.js and npm to use WebVOWL.");
            }
            
            // Install dependencies
            String[] installCommand = isWindows() ? 
                new String[]{"cmd", "/c", "npm", "install"} : 
                new String[]{"npm", "install"};
                
            ProcessBuilder npmInstall = new ProcessBuilder(installCommand);
            npmInstall.directory(webvowlDir.toFile());
            npmInstall.inheritIO();
            Process installProcess = npmInstall.start();
            installProcess.waitFor();
            
            if (installProcess.exitValue() != 0) {
                throw new IOException("npm install failed. Please check Node.js and npm installation.");
            }
            
            System.out.println("✅ Dependencies installed successfully");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Installation process interrupted", e);
        }
    }
    
    /**
     * Verify that JSON files are persisting in the deploy/data folder
     */
    private static void verifyJSONFiles() {
        try {
            Path dataDir = Paths.get(WEBVOWL_DIR, "deploy", "data");
            if (Files.exists(dataDir)) {
                long jsonCount = Files.list(dataDir)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .filter(path -> path.getFileName().toString().startsWith("sport-ontology"))
                    .count();
                
                System.out.println("📄 Found " + jsonCount + " sport-ontology JSON files in deploy/data");
                
                if (jsonCount > 0) {
                    System.out.println("✅ JSON files are persisting correctly!");
                } else {
                    System.out.println("⚠️  No sport-ontology JSON files found - they may have been cleaned");
                }
                
                // List all JSON files for verification
                Files.list(dataDir)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .forEach(path -> System.out.println("  📝 " + path.getFileName()));
                    
            } else {
                System.out.println("❌ Deploy/data directory not found!");
            }
        } catch (IOException e) {
            System.err.println("❌ Error verifying JSON files: " + e.getMessage());
        }
    }
    
    /**
     * Copy all OWL ontologies from WebVOWL_ontologies folder and set up JavaScript conversion
     */
    private static void convertAllOntologiesToJSON() throws IOException {
        System.out.println("🔄 Setting up OWL ontologies for JavaScript conversion...");
        
        Path ontologiesDir = Paths.get("WebVOWL_ontologies");
        
        if (!Files.exists(ontologiesDir)) {
            System.out.println("⚠️  WebVOWL_ontologies folder not found, creating it...");
            Files.createDirectories(ontologiesDir);
            System.out.println("📁 Created WebVOWL_ontologies folder. Please add your .owl files there.");
            return;
        }
        
        // Ensure output directory exists
        Path outputDir = Paths.get(WEBVOWL_DIR, "deploy", "data");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            System.out.println("📁 Created output directory: " + outputDir);
        }
        
        // Copy all .owl files to the data directory for JavaScript access
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(ontologiesDir, "*.owl")) {
            int copiedCount = 0;
            
            for (Path owlFile : stream) {
                System.out.println("\n📂 Processing: " + owlFile.getFileName());
                
                try {
                    String owlFileName = owlFile.getFileName().toString();
                    Path targetPath = outputDir.resolve(owlFileName);
                    
                    System.out.println("📁 Copying to: " + targetPath);
                    
                    // Copy OWL file to data directory
                    Files.copy(owlFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    if (Files.exists(targetPath)) {
                        System.out.println("✅ OWL file copied: " + owlFileName);
                        copiedCount++;
                    } else {
                        System.out.println("⚠️  Expected OWL file not found: " + owlFileName);
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Error copying " + owlFile.getFileName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            if (copiedCount > 0) {
                System.out.println("\n✅ Successfully copied " + copiedCount + " ontology files");
                System.out.println("📝 JavaScript will handle OWL to JSON conversion in the browser");
            } else {
                System.out.println("\n⚠️  No .owl files found in WebVOWL_ontologies folder");
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error reading WebVOWL_ontologies directory: " + e.getMessage());
        }
    }
    
    /**
     * Configure WebVOWL for dynamic ontology loading
     */
    private static void configureWebVOWLForDynamicLoading() throws IOException {
        System.out.println("🔧 Configuring WebVOWL for dynamic ontology loading...");
        
        // Create the dynamic ontology loader JavaScript file
        createDynamicOntologyLoader();
        
        // Update index.html to include our script
        updateIndexHtmlForDynamicLoading();
        
        System.out.println("✅ WebVOWL configured for dynamic ontology loading");
    }
    
    /**
     * Create dynamic-ontology-loader.js for handling OWL to JSON conversion and menu updates
     */
    private static void createDynamicOntologyLoader() throws IOException {
        Path jsDir = Paths.get(WEBVOWL_DIR, "deploy", "js");
        Files.createDirectories(jsDir);
        
        Path loaderScript = jsDir.resolve("dynamic-ontology-loader.js");
        
        String loaderContent = 
            "// Dynamic Ontology Loader for WebVOWL\n" +
            "// Handles OWL file detection, conversion, and menu updates\n" +
            "\n" +
            "(function() {\n" +
            "    'use strict';\n" +
            "    \n" +
            "    var SCAN_INTERVAL = 2000; // 2 seconds\n" +
            "    var CONVERSION_TIMEOUT = 30000; // 30 seconds\n" +
            "    var isProcessing = false;\n" +
            "    \n" +
            "    // List of ontology files to check for\n" +
            "    var potentialOntologies = [\n" +
            "        'sport-ontology',\n" +
            "        'sport-ontology_2',\n" +
            "        'sport-ontology_3',\n" +
            "        'foaf',\n" +
            "        'goodrelations',\n" +
            "        'muto',\n" +
            "        'ontovibe',\n" +
            "        'personasonto',\n" +
            "        'sioc',\n" +
            "        'benchmark'\n" +
            "    ];\n" +
            "    \n" +
            "    // Display names for ontologies\n" +
            "    var displayNames = {\n" +
            "        'sport-ontology': 'Sport Ontology - Football/Soccer Domain',\n" +
            "        'sport-ontology_2': 'Sport Ontology v2 - Extended Domain',\n" +
            "        'sport-ontology_3': 'Sport Ontology v3 - Advanced Domain',\n" +
            "        'foaf': 'Friend of a Friend (FOAF) vocabulary',\n" +
            "        'goodrelations': 'GoodRelations Vocabulary for E-Commerce',\n" +
            "        'muto': 'Modular and Unified Tagging Ontology (MUTO)',\n" +
            "        'ontovibe': 'Ontology Visualization Benchmark (OntoViBe)',\n" +
            "        'personasonto': 'Personas Ontology (PersonasOnto)',\n" +
            "        'sioc': 'SIOC (Semantically-Interlinked Online Communities) Core Ontology',\n" +
            "        'benchmark': 'Benchmark Graph for VOWL'\n" +
            "    };\n" +
            "    \n" +
            "    function log(message) {\n" +
            "        console.log('[Dynamic Loader] ' + message);\n" +
            "    }\n" +
            "    \n" +
            "    function convertOWLToJSON(owlName, callback) {\n" +
            "        var owlPath = './data/' + owlName + '.owl';\n" +
            "        var jsonPath = './data/' + owlName + '.json';\n" +
            "        \n" +
            "        log('Checking for OWL file: ' + owlPath);\n" +
            "        \n" +
            "        // First check if JSON already exists\n" +
            "        d3.xhr(jsonPath, function(jsonError, jsonRequest) {\n" +
            "            if (!jsonError && jsonRequest.responseText) {\n" +
            "                log('JSON already exists: ' + owlName + '.json');\n" +
            "                callback(null, true);\n" +
            "                return;\n" +
            "            }\n" +
            "            \n" +
            "            // JSON doesn't exist, check for OWL\n" +
            "            d3.xhr(owlPath, function(owlError, owlRequest) {\n" +
            "                if (owlError || !owlRequest.responseText) {\n" +
            "                    // OWL file doesn't exist\n" +
            "                    callback(null, false);\n" +
            "                    return;\n" +
            "                }\n" +
            "                \n" +
            "                log('Converting OWL to JSON: ' + owlName);\n" +
            "                \n" +
            "                // Use WebVOWL's conversion mechanism\n" +
            "                try {\n" +
            "                    // Wait for WebVOWL to be fully loaded\n" +
            "                    if (typeof webvowl !== 'undefined' && webvowl.graph) {\n" +
            "                        // Use WebVOWL's graph to parse OWL\n" +
            "                        var converter = webvowl.graph().parser();\n" +
            "                        \n" +
            "                        converter.parse(owlRequest.responseText, function(error, graph) {\n" +
            "                            if (!error && graph) {\n" +
            "                                log('✅ Successfully converted: ' + owlName);\n" +
            "                                callback(null, true);\n" +
            "                            } else {\n" +
            "                                log('❌ Conversion failed: ' + owlName + ' - ' + error);\n" +
            "                                callback(error, false);\n" +
            "                            }\n" +
            "                        });\n" +
            "                    } else {\n" +
            "                        // WebVOWL not ready yet, will try again later\n" +
            "                        log('⏳ WebVOWL not ready, will retry: ' + owlName);\n" +
            "                        callback(new Error('WebVOWL not ready'), false);\n" +
            "                    }\n" +
            "                } catch (e) {\n" +
            "                    log('❌ Conversion exception: ' + owlName + ' - ' + e.message);\n" +
            "                    callback(e, false);\n" +
            "                }\n" +
            "            });\n" +
            "        });\n" +
            "    }\n" +
            "    \n" +
            "    function scanForOntologies(callback) {\n" +
            "        var availableOntologies = [];\n" +
            "        var checkCount = 0;\n" +
            "        var totalChecks = potentialOntologies.length;\n" +
            "        \n" +
            "        function checkComplete() {\n" +
            "            checkCount++;\n" +
            "            if (checkCount >= totalChecks) {\n" +
            "                log('Scan complete. Found: [' + availableOntologies.join(', ') + ']');\n" +
            "                callback(availableOntologies);\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        potentialOntologies.forEach(function(ontologyName) {\n" +
            "            convertOWLToJSON(ontologyName, function(error, isAvailable) {\n" +
            "                if (isAvailable) {\n" +
            "                    availableOntologies.push(ontologyName);\n" +
            "                }\n" +
            "                checkComplete();\n" +
            "            });\n" +
            "        });\n" +
            "    }\n" +
            "    \n" +
            "    function updateOntologyMenu(ontologies) {\n" +
            "        var menuContainer = d3.select('#m_select');\n" +
            "        \n" +
            "        if (menuContainer.empty()) {\n" +
            "            log('Menu container not found, will retry later');\n" +
            "            return;\n" +
            "        }\n" +
            "        \n" +
            "        // Remove existing dynamic entries\n" +
            "        menuContainer.selectAll('li.dynamic-ontology').remove();\n" +
            "        \n" +
            "        // Add available ontologies\n" +
            "        ontologies.forEach(function(ontologyName) {\n" +
            "            var displayName = displayNames[ontologyName] || \n" +
            "                ontologyName.replace(/[-_]/g, ' ')\n" +
            "                           .replace(/\\b\\w/g, function(l) { return l.toUpperCase(); });\n" +
            "            \n" +
            "            var listItem = menuContainer.insert('li', '.option')\n" +
            "                                      .attr('class', 'dynamic-ontology');\n" +
            "            \n" +
            "            listItem.append('a')\n" +
            "                   .attr('href', '#' + ontologyName)\n" +
            "                   .attr('id', ontologyName)\n" +
            "                   .text(displayName)\n" +
            "                   .on('click', function() {\n" +
            "                       log('Loading ontology: ' + ontologyName);\n" +
            "                       // Trigger ontology loading\n" +
            "                       if (typeof loadOntologyFromText === 'function') {\n" +
            "                           d3.xhr('./data/' + ontologyName + '.json', function(error, request) {\n" +
            "                               if (!error && request.responseText) {\n" +
            "                                   loadOntologyFromText(request.responseText, {\n" +
            "                                       filename: ontologyName + '.json'\n" +
            "                                   });\n" +
            "                               }\n" +
            "                           });\n" +
            "                       }\n" +
            "                   });\n" +
            "        });\n" +
            "        \n" +
            "        log('Updated menu with ' + ontologies.length + ' ontologies');\n" +
            "    }\n" +
            "    \n" +
            "    function performScan() {\n" +
            "        if (isProcessing) {\n" +
            "            return;\n" +
            "        }\n" +
            "        \n" +
            "        isProcessing = true;\n" +
            "        log('Scanning for ontologies...');\n" +
            "        \n" +
            "        scanForOntologies(function(ontologies) {\n" +
            "            updateOntologyMenu(ontologies);\n" +
            "            isProcessing = false;\n" +
            "        });\n" +
            "    }\n" +
            "    \n" +
            "    // Initialize when document is ready\n" +
            "    function initialize() {\n" +
            "        log('Dynamic Ontology Loader initialized');\n" +
            "        \n" +
            "        // Initial scan\n" +
            "        setTimeout(performScan, 1000);\n" +
            "        \n" +
            "        // Set up periodic scanning\n" +
            "        setInterval(performScan, SCAN_INTERVAL);\n" +
            "        \n" +
            "        log('Scanning every ' + (SCAN_INTERVAL / 1000) + ' seconds');\n" +
            "    }\n" +
            "    \n" +
            "    // Start when page loads\n" +
            "    if (document.readyState === 'loading') {\n" +
            "        document.addEventListener('DOMContentLoaded', initialize);\n" +
            "    } else {\n" +
            "        initialize();\n" +
            "    }\n" +
            "    \n" +
            "})();";
        
        Files.write(loaderScript, loaderContent.getBytes());
        System.out.println("✅ Created dynamic-ontology-loader.js");
    }
    
    /**
     * Update index.html to include our dynamic loader script
     */
    private static void updateIndexHtmlForDynamicLoading() throws IOException {
        Path indexHtmlPath = Paths.get(WEBVOWL_DIR, "deploy", "index.html");
        
        if (!Files.exists(indexHtmlPath)) {
            // Try the src version
            indexHtmlPath = Paths.get(WEBVOWL_DIR, "src", "index.html");
        }
        
        if (Files.exists(indexHtmlPath)) {
            String htmlContent = new String(Files.readAllBytes(indexHtmlPath));
            
            // Check if our script is already included
            if (!htmlContent.contains("dynamic-ontology-loader.js")) {
                // Add our script before the closing body tag
                String scriptTag = "    <script src=\"js/dynamic-ontology-loader.js\"></script>\n</body>";
                htmlContent = htmlContent.replace("</body>", scriptTag);
                
                Files.write(indexHtmlPath, htmlContent.getBytes());
                System.out.println("✅ Updated index.html to include dynamic loader script");
            } else {
                System.out.println("✅ index.html already configured");
            }
        } else {
            System.out.println("⚠️  index.html not found, script will be included after build");
        }
    }
    
    /**
     * Copy ontology file and convert for WebVOWL usage
     */
    private static void copyAndConvertOntology(Path ontologyFile) throws IOException {
        Path dataDir = Paths.get(DATA_DIR);
        Files.createDirectories(dataDir);
        
        String ontologyFileName = ontologyFile.getFileName().toString();
        Path targetFile = dataDir.resolve(ontologyFileName);
        
        // Copy the OWL file
        Files.copy(ontologyFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("📋 Copied ontology to: " + targetFile);
        
        // Add to WebVOWL menu by converting via WebVOWL's own converter
        addOntologyToMenu(ontologyFileName);
    }
    
    /**
     * Add ontology to WebVOWL menu by creating URL for automatic loading
     */
    private static void addOntologyToMenu(String ontologyFileName) throws IOException {
        System.out.println("� Configuring ontology for WebVOWL menu...");
        
        // Create a custom URL that WebVOWL can use to load the ontology
        String ontologyUrl = "data/" + ontologyFileName;
        
        // Create a simple HTML file that auto-loads the ontology
        Path webvowlDir = Paths.get(WEBVOWL_DIR);
        Path deployDir = webvowlDir.resolve("deploy");
        Path autoLoadFile = deployDir.resolve("sport-ontology.html");
        
        String autoLoadContent = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>Sport Ontology - WebVOWL</title>\n" +
            "    <script>\n" +
            "        // Auto-redirect to WebVOWL with sport ontology loaded\n" +
            "        window.location.href = 'index.html#opts=url=%s';\n" +
            "    </script>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <p>Loading Sport Ontology visualization...</p>\n" +
            "</body>\n" +
            "</html>", ontologyUrl);
        
        Files.write(autoLoadFile, autoLoadContent.getBytes());
        
        System.out.println("✅ Sport ontology configured for WebVOWL");
        System.out.println("🌐 Direct link: http://localhost:" + SERVER_PORT + "/sport-ontology.html");
        System.out.println("📊 Or use the file picker in WebVOWL to select: " + ontologyFileName);
    }
    
    /**
     * Start WebVOWL server using npm run webserver (webpack + grunt)
     */
    private static void startWebVOWLServer() throws IOException {
        System.out.println("🚀 Starting WebVOWL server with webpack...");
        System.out.println("📝 Real-time build output will be shown below:");
        System.out.println("=" .repeat(60));
        
        Path webvowlDir = Paths.get(WEBVOWL_DIR);
        
        try {
            // Use cross-platform npm command with real-time output
            String[] webserverCommand;
            if (isWindows()) {
                // On Windows, run npm directly to see output, then detach
                webserverCommand = new String[]{"cmd", "/c", "npm", "run", "webserver"};
            } else {
                webserverCommand = new String[]{"npm", "run", "webserver"};
            }
            
            ProcessBuilder webserverCmd = new ProcessBuilder(webserverCommand);
            webserverCmd.directory(webvowlDir.toFile());
            webserverCmd.inheritIO(); // Show real-time webpack output
            
            System.out.println("🛠️  Running: " + String.join(" ", webserverCommand));
            Process webserverProcess = webserverCmd.start();
            
            // Give time for initial build and server startup
            System.out.println("⏳ Waiting for webpack build to complete...");
            Thread.sleep(10000); // Longer wait for full build completion
            
            // Check if server started successfully
            if (webserverProcess.isAlive()) {
                System.out.println("=" .repeat(60));
                System.out.println("✅ WebVOWL server started successfully!");
                System.out.println("🌐 Server running at: http://localhost:" + SERVER_PORT);
                System.out.println("🌐 Browser should open automatically");
            } else {
                throw new IOException("WebVOWL server failed to start");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Server start process interrupted", e);
        }
    }
    
    /**
     * Open browser to WebVOWL interface
     */
    private static void openBrowser() {
        try {
            String url = "http://localhost:" + SERVER_PORT;
            
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url));
                    System.out.println("🌐 Opening WebVOWL in browser...");
                    return;
                }
            }
            
            // Fallback for different OS
            if (isWindows()) {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else if (isMac()) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
            
            System.out.println("🌐 Browser opened");
            
        } catch (Exception e) {
            System.out.println("⚠️  Could not auto-open browser. Please navigate to:");
            System.out.println("   http://localhost:" + SERVER_PORT);
        }
    }
    
    /**
     * Keep the application running
     */
    private static void keepServerRunning() {
        System.out.println();
        System.out.println("✅ Setup complete!");
        System.out.println("📊 To view your Sport Ontology:");
        System.out.println("   🎯 DIRECT ACCESS: http://localhost:" + SERVER_PORT + "/sport-ontology.html");
        System.out.println("   📂 OR use file picker: Click 'Select ontology file' → choose 'sport-ontology.owl'");
        System.out.println();
        System.out.println("🔄 WebVOWL is running with live reload");
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println();
        
        try {
            // Keep main thread alive
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("🛑 Server stopped");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Check if running on Windows
     */
    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    
    /**
     * Check if running on Mac
     */
    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}