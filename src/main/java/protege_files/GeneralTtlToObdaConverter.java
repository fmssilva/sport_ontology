package protege_files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General converter from R2RML TTL mappings to OBDA format.
 * Works with any R2RML TTL file without hardcoded mappings.
 * 
 * Usage:
 * GeneralTtlToObdaConverter converter = new GeneralTtlToObdaConverter(ttlFilePath);
 * String obdaMappings = converter.convertToObda();
 */
public class GeneralTtlToObdaConverter {
    
    private final String ttlContent;
    private final Map<String, String> prefixes;
    private final List<TriplesMapInfo> triplesMaps;
    
    // Configuration - can be set via constructor or made configurable
    private String ontologyNamespace = "http://www.semanticweb.org/sports/ontology#";
    private String dataNamespace = "http://www.semanticweb.org/sports/data#";
    private String aboxNamespace = "http://www.semanticweb.org/sports/abox#";
    
    public GeneralTtlToObdaConverter(Path ttlFilePath) throws IOException {
        this.ttlContent = Files.readString(ttlFilePath);
        this.prefixes = new HashMap<>();
        this.triplesMaps = new ArrayList<>();
        parseContent();
    }
    
    public GeneralTtlToObdaConverter(Path ttlFilePath, String ontologyNamespace, String dataNamespace) throws IOException {
        this(ttlFilePath);
        this.ontologyNamespace = ontologyNamespace;
        this.dataNamespace = dataNamespace;
    }
    
    /**
     * Converts the parsed R2RML mappings to OBDA format
     */
    public String convertToObda() {
        StringBuilder obda = new StringBuilder();
        
        for (TriplesMapInfo triplesMap : triplesMaps) {
            String mappingId = extractMappingId(triplesMap.uri);
            obda.append("mappingId\t").append(mappingId).append("\n");
            
            // Build target triples
            String target = buildTargetTriples(triplesMap);
            obda.append("target\t\t").append(target).append("\n");
            
            // Extract source SQL
            String source = triplesMap.sqlQuery;
            obda.append("source\t\t").append(source).append("\n");
            obda.append("\n");
        }
        
        return obda.toString();
    }
    
    /**
     * Parses the TTL content to extract prefixes and triples maps
     */
    private void parseContent() {
        parsePrefixes();
        parseTriplesMaps();
    }
    
    /**
     * Extracts prefix declarations from TTL content
     */
    private void parsePrefixes() {
        Pattern prefixPattern = Pattern.compile("@prefix\\s+(\\w*):?\\s*<([^>]+)>\\s*\\.");
        Matcher matcher = prefixPattern.matcher(ttlContent);
        
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String namespace = matcher.group(2);
            prefixes.put(prefix, namespace);
        }
        
        System.out.println("   ✓ Extracted " + prefixes.size() + " prefix declarations");
    }
    
    /**
     * Extracts all triples maps from TTL content using a simpler approach
     */
    private void parseTriplesMaps() {
        // Split content by triples map declarations
        String[] blocks = ttlContent.split("(?=<[^>]+>\\s+a\\s+rr:TriplesMap)");
        
        for (String block : blocks) {
            if (block.trim().isEmpty() || !block.contains("rr:TriplesMap")) {
                continue;
            }
            
            TriplesMapInfo triplesMap = new TriplesMapInfo();
            
            // Extract URI
            Pattern uriPattern = Pattern.compile("<([^>]+)>\\s+a\\s+rr:TriplesMap");
            Matcher uriMatcher = uriPattern.matcher(block);
            if (uriMatcher.find()) {
                triplesMap.uri = uriMatcher.group(1);
            }
            
            triplesMap.content = block;
            
            // Parse components with simpler patterns
            parseLogicalTableSimple(triplesMap, block);
            parseSubjectMapSimple(triplesMap, block);
            parsePredicateObjectMapsSimple(triplesMap, block);
            
            if (triplesMap.sqlQuery != null && !triplesMap.sqlQuery.isEmpty()) {
                triplesMaps.add(triplesMap);
            }
        }
        
        System.out.println("   ✓ Extracted " + triplesMaps.size() + " triples maps");
    }
    
    /**
     * Simple SQL query extraction
     */
    private void parseLogicalTableSimple(TriplesMapInfo triplesMap, String block) {
        Pattern sqlPattern = Pattern.compile("rr:sqlQuery\\s+\"([^\"]+)\"", Pattern.DOTALL);
        Matcher matcher = sqlPattern.matcher(block);
        
        if (matcher.find()) {
            triplesMap.sqlQuery = matcher.group(1).trim();
        }
    }
    
    /**
     * Simple subject map extraction
     */
    private void parseSubjectMapSimple(TriplesMapInfo triplesMap, String block) {
        // Extract template
        Pattern templatePattern = Pattern.compile("rr:template\\s+\"([^\"]+)\"");
        Matcher templateMatcher = templatePattern.matcher(block);
        if (templateMatcher.find()) {
            triplesMap.subjectTemplate = templateMatcher.group(1);
        }
        
        // Extract class
        Pattern classPattern = Pattern.compile("rr:class\\s+(\\S+)");
        Matcher classMatcher = classPattern.matcher(block);
        if (classMatcher.find()) {
            triplesMap.rdfClass = expandPrefix(classMatcher.group(1));
        }
    }
    
    /**
     * Simple predicate-object map extraction
     */
    private void parsePredicateObjectMapsSimple(TriplesMapInfo triplesMap, String block) {
        triplesMap.predicateObjectMaps = new ArrayList<>();
        
        // Find all predicate-object patterns
        Pattern pomPattern = Pattern.compile(
            "rr:predicate\\s+(\\S+);\\s*rr:objectMap\\s*\\[([^\\]]+)\\]",
            Pattern.DOTALL
        );
        
        Matcher matcher = pomPattern.matcher(block);
        
        while (matcher.find()) {
            String predicateUri = matcher.group(1);
            String objectMapContent = matcher.group(2);
            
            PredicateObjectMap pom = new PredicateObjectMap();
            pom.predicate = expandPrefix(predicateUri);
            
            // Extract column (for data properties)
            Pattern columnPattern = Pattern.compile("rr:column\\s+\"([^\"]+)\"");
            Matcher columnMatcher = columnPattern.matcher(objectMapContent);
            if (columnMatcher.find()) {
                pom.column = columnMatcher.group(1);
            }
            
            // Extract template (for object properties)
            Pattern templatePattern = Pattern.compile("rr:template\\s+\"([^\"]+)\"");
            Matcher templateMatcher = templatePattern.matcher(objectMapContent);
            if (templateMatcher.find()) {
                pom.objectTemplate = templateMatcher.group(1);
            }
            
            // Extract datatype
            Pattern datatypePattern = Pattern.compile("rr:datatype\\s+(\\S+)");
            Matcher datatypeMatcher = datatypePattern.matcher(objectMapContent);
            if (datatypeMatcher.find()) {
                pom.datatype = expandPrefix(datatypeMatcher.group(1));
            }
            
            triplesMap.predicateObjectMaps.add(pom);
        }
    }
    
    /**
     * Expands prefixed terms to full URIs
     */
    private String expandPrefix(String term) {
        if (term.contains(":")) {
            String[] parts = term.split(":", 2);
            String prefix = parts[0];
            String localName = parts[1];
            
            String namespace = prefixes.get(prefix);
            if (namespace != null) {
                return namespace + localName;
            }
        }
        return term;
    }
    
    /**
     * Builds target triples from triples map info
     */
    private String buildTargetTriples(TriplesMapInfo triplesMap) {
        StringBuilder target = new StringBuilder();
        
        // Convert subject template to OBDA format
        String subject = convertTemplateToObda(triplesMap.subjectTemplate);
        target.append(subject);
        
        // Add class assertion if this is an entity mapping
        boolean hasClassAssertion = false;
        if (triplesMap.rdfClass != null) {
            String className = convertUriToShort(triplesMap.rdfClass);
            target.append(" a ").append(className);
            hasClassAssertion = true;
        }
        
        // Add property assertions
        boolean isFirstProperty = true;
        for (PredicateObjectMap pom : triplesMap.predicateObjectMaps) {
            if (pom.predicate == null || (pom.column == null && pom.objectTemplate == null)) {
                continue; // Skip invalid mappings
            }
            
            String predicate = convertUriToShort(pom.predicate);
            String object;
            
            // Check if this is an object property (references another entity) or data property
            if (isObjectProperty(pom)) {
                // Object property - reference another entity
                object = convertTemplateToObda(pom.objectTemplate);
            } else {
                // Data property - use column value
                object = "{" + pom.column + "}";
                if (pom.datatype != null) {
                    String datatype = convertUriToShort(pom.datatype);
                    object += "^^" + datatype;
                }
            }
            
            // Add separator - use ';' between all properties (after class assertion or other properties)
            if (hasClassAssertion || !isFirstProperty) {
                target.append(" ; ");
            } else {
                target.append(" ");
            }
            
            target.append(predicate).append(" ").append(object);
            isFirstProperty = false;
        }
        
        target.append(" .");
        return target.toString();
    }
    
    /**
     * Determines if a predicate-object mapping is an object property (references another entity)
     */
    private boolean isObjectProperty(PredicateObjectMap pom) {
        // Object properties have templates but no datatypes
        return pom.objectTemplate != null && pom.datatype == null;
    }
    
    /**
     * Converts R2RML template to OBDA format
     */
    private String convertTemplateToObda(String template) {
        if (template == null) return "";
        
        // Replace template variables {COLUMN} with {COLUMN}
        // Replace namespace prefix with short form
        String result = template;
        
        if (result.startsWith(dataNamespace)) {
            result = "data:" + result.substring(dataNamespace.length());
        } else if (result.startsWith(ontologyNamespace)) {
            result = ":" + result.substring(ontologyNamespace.length());
        }
        
        return result;
    }
    
    /**
     * Converts full URI to short prefixed form
     */
    private String convertUriToShort(String uri) {
        if (uri.startsWith(ontologyNamespace)) {
            return ":" + uri.substring(ontologyNamespace.length());
        } else if (uri.startsWith(dataNamespace)) {
            return "data:" + uri.substring(dataNamespace.length());
        } else if (uri.startsWith("http://www.w3.org/2001/XMLSchema#")) {
            return "xsd:" + uri.substring("http://www.w3.org/2001/XMLSchema#".length());
        }
        return uri;
    }
    
    /**
     * Extracts mapping ID from triples map URI
     */
    private String extractMappingId(String uri) {
        if (uri.contains("#")) {
            return uri.substring(uri.lastIndexOf("#") + 1);
        } else if (uri.contains("/")) {
            return uri.substring(uri.lastIndexOf("/") + 1);
        } else if (uri.contains(":")) {
            return uri.substring(uri.lastIndexOf(":") + 1);
        }
        return uri;
    }
    
    // Data classes for storing parsed information
    private static class TriplesMapInfo {
        String uri;
        String content;
        String sqlQuery;
        String subjectTemplate;
        String rdfClass;
        List<PredicateObjectMap> predicateObjectMaps = new ArrayList<>();
    }
    
    private static class PredicateObjectMap {
        String predicate;
        String column;        // For data properties
        String objectTemplate; // For object properties
        String datatype;
    }
}