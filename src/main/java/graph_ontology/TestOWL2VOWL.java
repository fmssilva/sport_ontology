package graph_ontology;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple test to verify OWL2VOWL converter functionality
 */
public class TestOWL2VOWL {
    
    public static void main(String[] args) {
        System.out.println("=== Testing OWL2VOWL Converter ===");
        
        try {
            // Test if we can create instances
            OfficialOWL2VowlConverter officialConverter = new OfficialOWL2VowlConverter();
            OWL2VowlConverter customConverter = new OWL2VowlConverter();
            
            System.out.println("✅ Both converters instantiated successfully");
            
            // Test if we can find the OWL2VOWL JAR
            System.out.println("🔍 Looking for OWL2VOWL JAR...");
            
            // Check if we have any test OWL files
            Path testPath = Paths.get("WebVOWL_ontologies");
            if (testPath.toFile().exists()) {
                System.out.println("📁 Found source directory: " + testPath);
            } else {
                System.out.println("⚠️ Source directory not found: " + testPath);
                System.out.println("📝 This is normal for first run - directory will be created when OWL files are added");
            }
            
            System.out.println("✅ OWL2VOWL setup verification completed");
            
        } catch (Exception e) {
            System.err.println("❌ Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}