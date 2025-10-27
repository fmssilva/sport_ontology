package utils;

import config.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts OBDA files to R2RML TTL format.
 * This is the reverse operation of GeneralTtlToObdaConverter.
 * 
 * Usage:
 * ObdaToTtlConverter converter = new ObdaToTtlConverter(obdaFilePath);
 * String ttlContent = converter.convertToTtl();
 * Files.write(ttlFilePath, ttlContent.getBytes());
 */
public class ObdaToTtlConverter {
    
    private final String obdaContent;
    private final Map<String, String> prefixes;
    private final List<ObdaMapping> mappings;
    
    // Default namespaces (can be overridden)
    private String ontologyNamespace = "http://www.semanticweb.org/sports/ontology#";
    private String dataNamespace = "http://www.semanticweb.org/sports/data#";
    
    public ObdaToTtlConverter(Path obdaFilePath) throws IOException {
        this.obdaContent = Files.readString(obdaFilePath);
        this.prefixes = new HashMap<>();
        this.mappings = new ArrayList<>();
        parseObdaContent();
    }
    
    public ObdaToTtlConverter(Path obdaFilePath, String ontologyNamespace, String dataNamespace) throws IOException {
        this(obdaFilePath);
        this.ontologyNamespace = ontologyNamespace;
        this.dataNamespace = dataNamespace;
    }
    
    /**
     * Converts the parsed OBDA mappings to R2RML TTL format
     */
    public String convertToTtl() {
        StringBuilder ttl = new StringBuilder();
        
        // Add TTL header with prefixes
        ttl.append(generateTtlHeader());
        ttl.append("\\n");
        
        // Convert each mapping to R2RML
        for (ObdaMapping mapping : mappings) {
            ttl.append(convertMappingToR2RML(mapping));
            ttl.append("\\n");
        }
        
        return ttl.toString();
    }
    
    /**
     * Parses OBDA content to extract prefixes and mappings
     */
    private void parseObdaContent() {
        extractPrefixes();
        extractMappings();
        System.out.println("   ✓ Parsed " + prefixes.size() + " prefixes and " + mappings.size() + " mappings from OBDA");
    }
    
    /**
     * Extract prefix declarations from OBDA content
     */
    private void extractPrefixes() {
        Pattern prefixPattern = Pattern.compile("([^\\s:]+):\\s*([^\\s]+)");
        String[] lines = obdaContent.split("\\n");
        
        boolean inPrefixSection = false;
        for (String line : lines) {
            line = line.trim();
            
            if (line.equals("[PrefixDeclaration]")) {
                inPrefixSection = true;
                continue;
            } else if (line.startsWith("[")) {
                inPrefixSection = false;
                continue;
            }
            
            if (inPrefixSection && !line.isEmpty()) {
                Matcher matcher = prefixPattern.matcher(line);
                if (matcher.find()) {
                    String prefix = matcher.group(1);
                    String namespace = matcher.group(2);
                    prefixes.put(prefix, namespace);
                }
            }
        }
    }
    
    /**
     * Extract mappings from OBDA content
     */
    private void extractMappings() {
        Pattern mappingPattern = Pattern.compile(
            "mappingId\\s+([^\\n]+)\\n" +
            "target\\s+([^\\n]+)\\n" +
            "source\\s+([^\\n]+)",
            Pattern.MULTILINE
        );
        
        Matcher matcher = mappingPattern.matcher(obdaContent);
        
        while (matcher.find()) {
            ObdaMapping mapping = new ObdaMapping();
            mapping.id = matcher.group(1).trim();
            mapping.target = matcher.group(2).trim();
            mapping.source = matcher.group(3).trim();
            
            parseTargetTriple(mapping);
            mappings.add(mapping);
        }
    }
    
    /**
     * Parse target triple to extract subject, class, and properties
     */
    private void parseTargetTriple(ObdaMapping mapping) {
        String target = mapping.target;
        
        // Extract subject template
        Pattern subjectPattern = Pattern.compile("^([^\\s]+)");
        Matcher subjectMatcher = subjectPattern.matcher(target);
        if (subjectMatcher.find()) {
            mapping.subjectTemplate = convertObdaToTemplate(subjectMatcher.group(1));
        }
        
        // Check for class assertion (a :ClassName)
        Pattern classPattern = Pattern.compile("a\\s+([^\\s;]+)");
        Matcher classMatcher = classPattern.matcher(target);
        if (classMatcher.find()) {
            mapping.rdfClass = expandShortPrefix(classMatcher.group(1));
        }
        
        // Extract properties
        mapping.properties = new ArrayList<>();
        
        // Split by ';' to get individual properties, but be careful with class assertion
        String[] parts = target.split("\\s*;\\s*");
        for (String part : parts) {
            part = part.trim();
            
            // Skip subject and class assertion parts
            if (part.contains(" a ") || part.endsWith(" .")) {
                continue;
            }
            
            // Parse property: predicate object
            Pattern propPattern = Pattern.compile("([^\\s]+)\\s+(.+?)(?:\\s*\\.|$)");
            Matcher propMatcher = propPattern.matcher(part);
            if (propMatcher.find()) {
                PropertyMapping prop = new PropertyMapping();
                prop.predicate = expandShortPrefix(propMatcher.group(1));
                
                String objectPart = propMatcher.group(2).trim();
                if (objectPart.startsWith("{") && objectPart.contains("}")) {
                    // Data property
                    Pattern dataPattern = Pattern.compile("\\{([^}]+)\\}(?:\\^\\^(.+))?");
                    Matcher dataMatcher = dataPattern.matcher(objectPart);
                    if (dataMatcher.find()) {
                        prop.column = dataMatcher.group(1);
                        if (dataMatcher.group(2) != null) {
                            prop.datatype = expandShortPrefix(dataMatcher.group(2));
                        }
                    }
                } else {
                    // Object property
                    prop.objectTemplate = convertObdaToTemplate(objectPart);
                }
                
                mapping.properties.add(prop);
            }
        }
    }
    
    /**
     * Convert OBDA subject/object to R2RML template
     */
    private String convertObdaToTemplate(String obdaReference) {
        if (obdaReference.startsWith("data:")) {
            return dataNamespace + obdaReference.substring(5);
        } else if (obdaReference.startsWith(":")) {
            return ontologyNamespace + obdaReference.substring(1);
        }
        return obdaReference;
    }
    
    /**
     * Expand short prefix to full URI
     */
    private String expandShortPrefix(String shortForm) {
        if (shortForm.contains(":")) {
            String[] parts = shortForm.split(":", 2);
            String prefix = parts[0];
            String localName = parts[1];
            
            String namespace = prefixes.get(prefix);
            if (namespace != null) {
                return namespace + localName;
            }
        }
        return shortForm;
    }
    
    /**
     * Generate TTL header with prefixes
     */
    private String generateTtlHeader() {
        StringBuilder header = new StringBuilder();
        
        // Add standard R2RML header
        header.append("# ========================================================================\\n");
        header.append("# SPORTS ONTOLOGY MAPPINGS - Generated from OBDA\\n");
        header.append("# ========================================================================\\n");
        header.append("# This file was automatically generated from OBDA mappings\\n");
        header.append("# Generated by ObdaToTtlConverter\\n");
        header.append("# ========================================================================\\n\\n");
        
        // Add R2RML prefix
        header.append("@prefix rr: <http://www.w3.org/ns/r2rml#> .\\n");
        
        // Add other prefixes from OBDA
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            String prefix = entry.getKey();
            String namespace = entry.getValue();
            
            if (!prefix.equals("rr")) { // Avoid duplicate rr: prefix
                header.append("@prefix ").append(prefix).append(": <").append(namespace).append("> .\\n");
            }
        }
        
        header.append("\\n");
        return header.toString();
    }
    
    /**
     * Convert individual OBDA mapping to R2RML TTL
     */
    private String convertMappingToR2RML(ObdaMapping mapping) {
        StringBuilder r2rml = new StringBuilder();
        
        // Create triples map URI
        String triplesMapUri = "<urn:r2rml:" + mapping.id + ">";
        
        r2rml.append("# ").append(mapping.id.replace("-", " ").toUpperCase()).append("\\n");
        r2rml.append(triplesMapUri).append(" a rr:TriplesMap;\\n");
        
        // Logical table with SQL query
        r2rml.append("  rr:logicalTable [ a rr:R2RMLView;\\n");
        r2rml.append("      rr:sqlQuery \"").append(mapping.source).append("\"\\n");
        r2rml.append("    ];\\n");
        
        // Subject map
        r2rml.append("  rr:subjectMap [ a rr:TermMap, rr:SubjectMap;\\n");
        r2rml.append("      rr:template \"").append(mapping.subjectTemplate).append("\";\\n");
        r2rml.append("      rr:termType rr:IRI");
        
        if (mapping.rdfClass != null) {
            r2rml.append(";\\n      rr:class ").append(convertUriToShort(mapping.rdfClass));
        }
        r2rml.append("\\n    ]");
        
        // Predicate-object maps
        if (!mapping.properties.isEmpty()) {
            r2rml.append(";\\n");
            
            for (int i = 0; i < mapping.properties.size(); i++) {
                PropertyMapping prop = mapping.properties.get(i);
                
                r2rml.append("  rr:predicateObjectMap [\\n");
                r2rml.append("      rr:predicate ").append(convertUriToShort(prop.predicate)).append(";\\n");
                
                if (prop.column != null) {
                    // Data property
                    r2rml.append("      rr:objectMap [ rr:column \"").append(prop.column).append("\"");
                    if (prop.datatype != null) {
                        r2rml.append("; rr:datatype ").append(convertUriToShort(prop.datatype));
                    }
                    r2rml.append(" ]\\n");
                } else if (prop.objectTemplate != null) {
                    // Object property
                    r2rml.append("      rr:objectMap [ rr:template \"").append(prop.objectTemplate).append("\"; rr:termType rr:IRI ]\\n");
                }
                
                r2rml.append("    ]");
                
                if (i < mapping.properties.size() - 1) {
                    r2rml.append(", [\\n");
                } else {
                    r2rml.append(" .\\n");
                }
            }
        } else {
            r2rml.append(" .\\n");
        }
        
        return r2rml.toString();
    }
    
    /**
     * Convert full URI to short prefixed form
     */
    private String convertUriToShort(String uri) {
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            String prefix = entry.getKey();
            String namespace = entry.getValue();
            
            if (uri.startsWith(namespace)) {
                return prefix + ":" + uri.substring(namespace.length());
            }
        }
        return "<" + uri + ">"; // Fallback to full URI
    }
    
    // Data classes for storing parsed OBDA information
    private static class ObdaMapping {
        String id;
        String target;
        String source;
        String subjectTemplate;
        String rdfClass;
        List<PropertyMapping> properties = new ArrayList<>();
    }
    
    private static class PropertyMapping {
        String predicate;
        String column;           // For data properties
        String objectTemplate;   // For object properties
        String datatype;
    }
}