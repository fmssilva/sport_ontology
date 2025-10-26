package graph_ontology;

import java.io.*;
import java.nio.file.*;

/**
 * Official OWL2VOWL Converter using the VisualDataWeb library
 * Simple wrapper that calls the official OWL2VOWL converter
 */
public class OfficialOWL2VowlConverter {
    
    /**
     * Convert OWL file to VOWL JSON using official OWL2VOWL library
     */
    public String convertOwlToVowlJson(Path owlFile) throws Exception {
        System.out.println("🔄 Converting OWL to VOWL JSON using official OWL2VOWL library: " + owlFile.getFileName());
        
        // Create temporary output file
        Path tempJsonFile = Files.createTempFile("vowl_", ".json");
        
        try {
            // Try to use the OWL2VOWL library directly
            // This is a simplified approach - we'll call the library via reflection or direct instantiation
            System.out.println("⚠️ Using simplified approach - calling OWL2VOWL via command line");
            
            // For now, let's call it via the JAR file
            String owl2vowlJar = findOwl2VowlJar();
            if (owl2vowlJar == null) {
                throw new RuntimeException("OWL2VOWL JAR not found. Please run 'mvn compile' first.");
            }
            
            // Execute OWL2VOWL via command line
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", owl2vowlJar,
                "-file", owlFile.toAbsolutePath().toString(),
                "-output", tempJsonFile.toAbsolutePath().toString()
            );
            
            pb.directory(new File(System.getProperty("user.dir")));
            Process process = pb.start();
            
            // Wait for completion
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                // Read error output
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                throw new RuntimeException("OWL2VOWL conversion failed: " + errorOutput.toString());
            }
            
            // Read the generated JSON
            if (!Files.exists(tempJsonFile)) {
                throw new RuntimeException("OWL2VOWL did not generate output file");
            }
            
            String vowlJson = new String(Files.readAllBytes(tempJsonFile));
            
            System.out.println("✅ Official OWL2VOWL conversion completed");
            System.out.println("📊 Generated VOWL JSON size: " + vowlJson.length() + " characters");
            
            return vowlJson;
            
        } finally {
            // Clean up temporary file
            try {
                Files.deleteIfExists(tempJsonFile);
            } catch (Exception e) {
                System.out.println("⚠️ Could not delete temp file: " + e.getMessage());
            }
        }
    }
    
    private String findOwl2VowlJar() {
        // Look for the OWL2VOWL JAR in lib directory
        File libDir = new File("lib");
        if (libDir.exists() && libDir.isDirectory()) {
            File[] jars = libDir.listFiles((dir, name) -> name.startsWith("OWL2VOWL") && name.endsWith(".jar"));
            if (jars != null && jars.length > 0) {
                return jars[0].getAbsolutePath();
            }
        }
        
        // Look in tools/OWL2VOWL/target
        File targetDir = new File("tools/OWL2VOWL/target");
        if (targetDir.exists() && targetDir.isDirectory()) {
            File[] jars = targetDir.listFiles((dir, name) -> name.startsWith("OWL2VOWL") && name.endsWith(".jar") && !name.contains("sources") && !name.contains("javadoc"));
            if (jars != null && jars.length > 0) {
                return jars[0].getAbsolutePath();
            }
        }
        
        return null;
    }
}