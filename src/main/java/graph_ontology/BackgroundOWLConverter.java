package graph_ontology;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background OWL to VOWL JSON Converter  
 * Uses improved OWL API parsing for complete ontology conversion
 */
public class BackgroundOWLConverter {
    
    private static final String SOURCE_DIR = "WebVOWL_ontologies";
    private static final String TARGET_DIR = "tools/WebVOWL-master/deploy/data";
    private static final int SCAN_INTERVAL_SECONDS = 2;
    
    private static OWL2VowlConverter customConverter = new OWL2VowlConverter();
    private static OfficialOWL2VowlConverter officialConverter = new OfficialOWL2VowlConverter();
    
    public static void main(String[] args) {
        System.out.println("=== BACKGROUND OWL CONVERTER STARTING ===");
        System.out.println("📁 Source: " + SOURCE_DIR);
        System.out.println("📁 Target: " + TARGET_DIR);
        System.out.println("⏰ Scan interval: " + SCAN_INTERVAL_SECONDS + " seconds");
        System.out.println("===========================================");
        
        // Create scheduled executor for periodic scanning
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        // Run initial scan
        System.out.println("🔄 Running initial scan...");
        scanAndConvert();
        
        // Schedule periodic scans
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("⏰ [" + java.time.LocalTime.now() + "] Periodic scan triggered");
            scanAndConvert();
        }, SCAN_INTERVAL_SECONDS, SCAN_INTERVAL_SECONDS, TimeUnit.SECONDS);
        
        System.out.println("✅ Background converter started successfully");
        System.out.println("🔄 Will scan every " + SCAN_INTERVAL_SECONDS + " seconds");
        System.out.println("🛑 Press Ctrl+C to stop");
        
        // Keep the application running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 Background converter stopping...");
            scheduler.shutdown();
        }));
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("🛑 Background converter interrupted");
            scheduler.shutdown();
        }
    }
    
    private static void scanAndConvert() {
        try {
            Path sourceDir = Paths.get(SOURCE_DIR);
            Path targetDir = Paths.get(TARGET_DIR);
            
            System.out.println("📋 Scanning source directory: " + sourceDir.toAbsolutePath());
            
            // Ensure directories exist
            if (!Files.exists(sourceDir)) {
                System.out.println("⚠️  Source directory doesn't exist: " + sourceDir);
                System.out.println("📁 Creating source directory...");
                Files.createDirectories(sourceDir);
                return;
            }
            
            if (!Files.exists(targetDir)) {
                System.out.println("📁 Creating target directory: " + targetDir);
                Files.createDirectories(targetDir);
            }
            
            // Find all OWL files
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir, "*.owl")) {
                boolean foundFiles = false;
                
                for (Path owlFile : stream) {
                    foundFiles = true;
                    System.out.println("📄 Found OWL file: " + owlFile.getFileName());
                    
                    String owlFileName = owlFile.getFileName().toString();
                    String jsonFileName = owlFileName.replace(".owl", ".json");
                    
                    // Only need JSON file in deploy directory for WebVOWL
                    Path targetJsonPath = targetDir.resolve(jsonFileName);
                    
                    try {
                        // Always ensure JSON exists in deploy directory (regenerate if missing)
                        if (!Files.exists(targetJsonPath)) {
                            
                            System.out.println("🔄 Converting to JSON: " + owlFileName + " → " + jsonFileName);
                            
                            try {
                                // Use custom converter first for WebVOWL compatibility
                                String vowlJson = null;
                                try {
                                    vowlJson = customConverter.convertOwlToVowlJson(owlFile);
                                    System.out.println("✅ Used custom converter with WebVOWL-compatible format");
                                } catch (Exception customEx) {
                                    System.out.println("⚠️ Custom converter failed: " + customEx.getMessage());
                                    System.out.println("🔄 Falling back to official converter...");
                                    vowlJson = officialConverter.convertOwlToVowlJson(owlFile);
                                    System.out.println("✅ Used official OWL2VOWL converter as fallback");
                                }
                                
                                // Ensure target directory exists
                                if (!Files.exists(targetDir)) {
                                    Files.createDirectories(targetDir);
                                    System.out.println("📁 Created target directory: " + targetDir);
                                }
                                
                                // Write JSON with detailed logging
                                System.out.println("💾 Writing JSON to: " + targetJsonPath.toAbsolutePath());
                                System.out.println("📊 JSON content length: " + (vowlJson != null ? vowlJson.length() : "null"));
                                
                                if (vowlJson == null || vowlJson.trim().isEmpty()) {
                                    throw new Exception("Generated VOWL JSON is null or empty");
                                }
                                
                                Files.write(targetJsonPath, vowlJson.getBytes(StandardCharsets.UTF_8));
                                
                                // Verify file was created
                                if (Files.exists(targetJsonPath)) {
                                    System.out.println("✅ VOWL JSON created: " + jsonFileName);
                                    System.out.println("📊 JSON size: " + Files.size(targetJsonPath) + " bytes");
                                } else {
                                    throw new Exception("JSON file was not created successfully");
                                }
                                
                                // Keep the OWL file in source directory only
                                System.out.println("📂 OWL file remains in source directory: " + owlFile.toAbsolutePath());
                                
                            } catch (Exception e) {
                                System.err.println("❌ VOWL conversion failed for " + owlFileName + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("✅ JSON exists: " + jsonFileName + " (copying to deploy directory)");
                            // Copy existing JSON from source area if it exists
                            Path sourceJsonPath = owlFile.getParent().resolve(jsonFileName);
                            if (Files.exists(sourceJsonPath)) {
                                Files.copy(sourceJsonPath, targetJsonPath, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("✅ Copied existing JSON: " + jsonFileName);
                            }
                        }
                        
                    } catch (Exception e) {
                        System.err.println("❌ Error processing " + owlFileName + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                if (!foundFiles) {
                    System.out.println("📭 No OWL files found in source directory");
                }
                
            } catch (IOException e) {
                System.err.println("❌ Error scanning directory: " + e.getMessage());
                e.printStackTrace();
            }
            
            // List what's in the target directory
            System.out.println("📋 Target directory contents:");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
                for (Path file : stream) {
                    if (file.getFileName().toString().endsWith(".owl") || 
                        file.getFileName().toString().endsWith(".json")) {
                        System.out.println("  📄 " + file.getFileName() + " (" + Files.size(file) + " bytes)");
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ Error listing target directory: " + e.getMessage());
            }
            
            System.out.println("✅ Scan complete");
            System.out.println("----------------------------------------");
            
        } catch (Exception e) {
            System.err.println("❌ Scan error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}