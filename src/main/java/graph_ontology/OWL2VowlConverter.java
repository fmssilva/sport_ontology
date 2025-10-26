package graph_ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.nio.file.*;
import java.util.*;

/**
 * OWL to VOWL JSON Converter
 * Converts any OWL ontology to VOWL JSON format that WebVOWL can understand
 * Uses OWL API for proper ontology parsing
 */
public class OWL2VowlConverter {
    
    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    
    public OWL2VowlConverter() {
        this.manager = OWLManager.createOWLOntologyManager();
        this.dataFactory = manager.getOWLDataFactory();
    }
    
    /**
     * Convert OWL file to VOWL JSON format
     */
    public String convertOwlToVowlJson(Path owlFile) throws Exception {
        System.out.println("🔄 Converting OWL to VOWL JSON: " + owlFile.getFileName());
        
        // Create a fresh manager for each conversion to avoid ontology conflicts
        OWLOntologyManager freshManager = OWLManager.createOWLOntologyManager();
        
        // Load the ontology with better error handling
        OWLOntology ontology = null;
        try {
            ontology = freshManager.loadOntologyFromOntologyDocument(owlFile.toFile());
            System.out.println("✅ OWL ontology loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to load OWL file: " + e.getMessage());
            throw new Exception("Could not load OWL file: " + owlFile.getFileName(), e);
        }
        
        // Extract ontology information
        OntologyInfo info = null;
        try {
            info = extractOntologyInfo(ontology, freshManager);
            System.out.println("✅ Ontology information extracted: " + info.classes.size() + " classes, " + info.properties.size() + " properties");
        } catch (Exception e) {
            System.err.println("❌ Failed to extract ontology information: " + e.getMessage());
            throw new Exception("Could not extract ontology information", e);
        }
        
        // Build VOWL JSON
        String vowlJson = null;
        try {
            vowlJson = buildVowlJson(info);
            System.out.println("✅ VOWL JSON built successfully (" + vowlJson.length() + " characters)");
        } catch (Exception e) {
            System.err.println("❌ Failed to build VOWL JSON: " + e.getMessage());
            throw new Exception("Could not build VOWL JSON", e);
        } finally {
            // Clean up ontology from manager
            try {
                if (ontology != null) {
                    freshManager.removeOntology(ontology);
                    System.out.println("🧹 Cleaned up ontology from manager");
                }
            } catch (Exception cleanup) {
                System.out.println("⚠️ Warning: Could not clean up ontology: " + cleanup.getMessage());
            }
        }
        
        System.out.println("✅ VOWL JSON conversion completed");
        return vowlJson;
    }
    
    /**
     * Extract relevant information from OWL ontology
     */
    private OntologyInfo extractOntologyInfo(OWLOntology ontology, OWLOntologyManager ontologyManager) {
        OntologyInfo info = new OntologyInfo();
        
        // Basic ontology metadata
        com.google.common.base.Optional<IRI> ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        info.iri = ontologyIRI.isPresent() ? ontologyIRI.get().toString() : "http://example.org/ontology";
        info.title = extractTitle(ontology);
        info.description = extractDescription(ontology);
        
        // Extract entities
        Set<OWLClass> classes = ontology.getClassesInSignature();
        Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature();
        Set<OWLDataProperty> dataProperties = ontology.getDataPropertiesInSignature();
        
        // Process classes
        int classId = 0;
        for (OWLClass owlClass : classes) {
            if (!owlClass.isOWLThing() && !owlClass.isOWLNothing()) {
                ClassInfo classInfo = new ClassInfo();
                classInfo.id = String.valueOf(classId++);
                classInfo.iri = owlClass.getIRI().toString();
                classInfo.label = extractLabel(owlClass, ontology);
                classInfo.comment = extractComment(owlClass, ontology);
                info.classes.add(classInfo);
            }
        }
        
        // Process object properties
        int propId = 0;
        for (OWLObjectProperty property : objectProperties) {
            PropertyInfo propInfo = new PropertyInfo();
            propInfo.id = String.valueOf(propId++);
            propInfo.iri = property.getIRI().toString();
            propInfo.label = extractLabel(property, ontology);
            propInfo.comment = extractComment(property, ontology);
            propInfo.type = "object";
            
            // Extract domain and range
            propInfo.domain = extractDomain(property, ontology);
            propInfo.range = extractRange(property, ontology);
            
            info.properties.add(propInfo);
        }
        
        // Process data properties
        for (OWLDataProperty property : dataProperties) {
            PropertyInfo propInfo = new PropertyInfo();
            propInfo.id = String.valueOf(propId++);
            propInfo.iri = property.getIRI().toString();
            propInfo.label = extractLabel(property, ontology);
            propInfo.comment = extractComment(property, ontology);
            propInfo.type = "datatype";
            
            // Extract domain and range
            propInfo.domain = extractDomain(property, ontology);
            propInfo.range = extractDataRange(property, ontology);
            
            info.properties.add(propInfo);
        }
        
        System.out.println("📊 Extracted: " + info.classes.size() + " classes, " + info.properties.size() + " properties");
        return info;
    }
    
    /**
     * Build VOWL JSON structure
     */
    private String buildVowlJson(OntologyInfo info) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"_comment\": \"Created with OWL2VowlConverter using OWL API\",\n");
        
        // Header section
        json.append("  \"header\": {\n");
        json.append("    \"languages\": [\"undefined\"],\n");
        json.append("    \"baseIris\": [\"").append(escapeJson(info.iri)).append("\"],\n");
        json.append("    \"title\": {\n");
        json.append("      \"undefined\": \"").append(escapeJson(info.title)).append("\"\n");
        json.append("    },\n");
        json.append("    \"iri\": \"").append(escapeJson(info.iri)).append("\",\n");
        json.append("    \"description\": {\n");
        json.append("      \"undefined\": \"").append(escapeJson(info.description)).append("\"\n");
        json.append("    }\n");
        json.append("  },\n");
        
        // Namespace (simplified)
        json.append("  \"namespace\": [],\n");
        
        // Classes
        json.append("  \"class\": [\n");
        for (int i = 0; i < info.classes.size(); i++) {
            ClassInfo classInfo = info.classes.get(i);
            if (i > 0) json.append(",\n");
            json.append("    {\n");
            json.append("      \"id\": \"").append(classInfo.id).append("\",\n");
            json.append("      \"type\": \"owl:Class\"\n");
            json.append("    }");
        }
        json.append("\n  ],\n");
        
        // Class attributes
        json.append("  \"classAttribute\": [\n");
        for (int i = 0; i < info.classes.size(); i++) {
            ClassInfo classInfo = info.classes.get(i);
            if (i > 0) json.append(",\n");
            json.append("    {\n");
            json.append("      \"id\": \"").append(classInfo.id).append("\",\n");
            json.append("      \"label\": {\n");
            json.append("        \"undefined\": \"").append(escapeJson(classInfo.label)).append("\"\n");
            json.append("      },\n");
            json.append("      \"iri\": \"").append(escapeJson(classInfo.iri)).append("\"");
            if (classInfo.comment != null && !classInfo.comment.isEmpty()) {
                json.append(",\n      \"comment\": {\n");
                json.append("        \"undefined\": \"").append(escapeJson(classInfo.comment)).append("\"\n");
                json.append("      }");
            }
            json.append(",\n      \"attributes\": [\"external\"]\n");
            json.append("    }");
        }
        json.append("\n  ],\n");
        
        // Properties
        json.append("  \"property\": [\n");
        for (int i = 0; i < info.properties.size(); i++) {
            PropertyInfo propInfo = info.properties.get(i);
            if (i > 0) json.append(",\n");
            json.append("    {\n");
            json.append("      \"id\": \"").append(propInfo.id).append("\"\n");
            json.append("    }");
        }
        json.append("\n  ],\n");
        
        // Property attributes
        json.append("  \"propertyAttribute\": [\n");
        for (int i = 0; i < info.properties.size(); i++) {
            PropertyInfo propInfo = info.properties.get(i);
            if (i > 0) json.append(",\n");
            json.append("    {\n");
            json.append("      \"id\": \"").append(propInfo.id).append("\",\n");
            json.append("      \"label\": {\n");
            json.append("        \"undefined\": \"").append(escapeJson(propInfo.label)).append("\"\n");
            json.append("      },\n");
            json.append("      \"iri\": \"").append(escapeJson(propInfo.iri)).append("\"");
            
            if (propInfo.domain != null) {
                json.append(",\n      \"domain\": \"").append(propInfo.domain).append("\"");
            }
            if (propInfo.range != null) {
                json.append(",\n      \"range\": \"").append(propInfo.range).append("\"");
            }
            if (propInfo.comment != null && !propInfo.comment.isEmpty()) {
                json.append(",\n      \"comment\": {\n");
                json.append("        \"undefined\": \"").append(escapeJson(propInfo.comment)).append("\"\n");
                json.append("      }");
            }
            
            // WebVOWL expects property types in attributes array, not as separate "type" field
            json.append(",\n      \"attributes\": [\"external\", \"").append(propInfo.type).append("\"]\n");
            json.append("    }");
        }
        json.append("\n  ]\n");
        
        json.append("}\n");
        return json.toString();
    }
    
    // Helper methods for extracting ontology information
    private String extractTitle(OWLOntology ontology) {
        // Try to extract title from annotations
        for (OWLAnnotation annotation : ontology.getAnnotations()) {
            OWLAnnotationProperty prop = annotation.getProperty();
            if (prop.getIRI().getFragment() != null && 
                (prop.getIRI().getFragment().equals("title") || 
                 prop.getIRI().getFragment().equals("label"))) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    return ((OWLLiteral) annotation.getValue()).getLiteral();
                }
            }
        }
        
        // Fallback: use ontology IRI
        com.google.common.base.Optional<IRI> iri = ontology.getOntologyID().getOntologyIRI();
        if (iri.isPresent()) {
            String fragment = iri.get().getFragment();
            if (fragment != null) return fragment;
            
            String path = iri.get().toString();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0) return path.substring(lastSlash + 1);
        }
        
        return "Ontology";
    }
    
    private String extractDescription(OWLOntology ontology) {
        // Try to extract description from annotations
        for (OWLAnnotation annotation : ontology.getAnnotations()) {
            OWLAnnotationProperty prop = annotation.getProperty();
            if (prop.getIRI().getFragment() != null && 
                (prop.getIRI().getFragment().equals("description") || 
                 prop.getIRI().getFragment().equals("comment"))) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    return ((OWLLiteral) annotation.getValue()).getLiteral();
                }
            }
        }
        return "Ontology converted from OWL to VOWL format";
    }
    
    private String extractLabel(OWLEntity entity, OWLOntology ontology) {
        // Try to get rdfs:label
        for (OWLAnnotationAssertionAxiom axiom : ontology.getAnnotationAssertionAxioms(entity.getIRI())) {
            OWLAnnotationProperty prop = axiom.getProperty();
            if (prop.isLabel()) {
                OWLAnnotationValue value = axiom.getValue();
                if (value instanceof OWLLiteral) {
                    return ((OWLLiteral) value).getLiteral();
                }
            }
        }
        
        // Fallback: use fragment or local name
        String fragment = entity.getIRI().getFragment();
        if (fragment != null) return fragment;
        
        String iri = entity.getIRI().toString();
        int lastHash = iri.lastIndexOf('#');
        int lastSlash = iri.lastIndexOf('/');
        int lastSeparator = Math.max(lastHash, lastSlash);
        
        return lastSeparator >= 0 ? iri.substring(lastSeparator + 1) : iri;
    }
    
    private String extractComment(OWLEntity entity, OWLOntology ontology) {
        // Try to get rdfs:comment
        for (OWLAnnotationAssertionAxiom axiom : ontology.getAnnotationAssertionAxioms(entity.getIRI())) {
            OWLAnnotationProperty prop = axiom.getProperty();
            if (prop.isComment()) {
                OWLAnnotationValue value = axiom.getValue();
                if (value instanceof OWLLiteral) {
                    return ((OWLLiteral) value).getLiteral();
                }
            }
        }
        return null;
    }
    
    private String extractDomain(OWLObjectProperty property, OWLOntology ontology) {
        Set<OWLObjectPropertyDomainAxiom> domainAxioms = ontology.getObjectPropertyDomainAxioms(property);
        for (OWLObjectPropertyDomainAxiom axiom : domainAxioms) {
            OWLClassExpression domain = axiom.getDomain();
            if (domain instanceof OWLClass) {
                return findClassId((OWLClass) domain);
            }
        }
        return null;
    }
    
    private String extractDomain(OWLDataProperty property, OWLOntology ontology) {
        Set<OWLDataPropertyDomainAxiom> domainAxioms = ontology.getDataPropertyDomainAxioms(property);
        for (OWLDataPropertyDomainAxiom axiom : domainAxioms) {
            OWLClassExpression domain = axiom.getDomain();
            if (domain instanceof OWLClass) {
                return findClassId((OWLClass) domain);
            }
        }
        return null;
    }
    
    private String extractRange(OWLObjectProperty property, OWLOntology ontology) {
        Set<OWLObjectPropertyRangeAxiom> rangeAxioms = ontology.getObjectPropertyRangeAxioms(property);
        for (OWLObjectPropertyRangeAxiom axiom : rangeAxioms) {
            OWLClassExpression range = axiom.getRange();
            if (range instanceof OWLClass) {
                return findClassId((OWLClass) range);
            }
        }
        return null;
    }
    
    private String extractDataRange(OWLDataProperty property, OWLOntology ontology) {
        Set<OWLDataPropertyRangeAxiom> rangeAxioms = ontology.getDataPropertyRangeAxioms(property);
        for (OWLDataPropertyRangeAxiom axiom : rangeAxioms) {
            OWLDataRange range = axiom.getRange();
            if (range instanceof OWLDatatype) {
                return ((OWLDatatype) range).getIRI().toString();
            }
        }
        return "http://www.w3.org/2001/XMLSchema#string"; // Default
    }
    
    private String findClassId(OWLClass owlClass) {
        // This is simplified - in a full implementation, you'd maintain a map
        // For now, return the class IRI
        return owlClass.getIRI().toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\\", "\\\\")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Data classes
    private static class OntologyInfo {
        String iri = "";
        String title = "";
        String description = "";
        List<ClassInfo> classes = new ArrayList<>();
        List<PropertyInfo> properties = new ArrayList<>();
    }
    
    private static class ClassInfo {
        String id;
        String iri;
        String label;
        String comment;
    }
    
    private static class PropertyInfo {
        String id;
        String iri;
        String label;
        String comment;
        String type; // "object" or "datatype"
        String domain;
        String range;
    }
}