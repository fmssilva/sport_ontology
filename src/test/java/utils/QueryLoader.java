package utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for loading SQL and SPARQL queries from external files
 * Supports both individual files and named queries within consolidated files
 */
public class QueryLoader {
    private static final String QUERIES_BASE_PATH = "/queries";
    
    /**
     * Load a SQL query from file
     * @param domain The domain folder (assumptions, reasoning, integrity)  
     * @param queryName The descriptive query name (without .sql extension)
     * @return The SQL query as a string
     */
    public static String loadSQL(String domain, String queryName) {
        // Try consolidated file first
        String consolidatedQuery = loadNamedQuery(domain, queryName, "sql");
        if (consolidatedQuery != null) {
            return consolidatedQuery;
        }
        
        // Fallback to individual file
        return loadQuery(domain, queryName + ".sql");
    }
    
    /**
     * Load a SPARQL query from file  
     * @param domain The domain folder (assumptions, reasoning, integrity)
     * @param queryName The descriptive query name (without .sparql extension)
     * @return The SPARQL query as a string
     */
    public static String loadSPARQL(String domain, String queryName) {
        // Try consolidated file first
        String consolidatedQuery = loadNamedQuery(domain, queryName, "sparql");
        if (consolidatedQuery != null) {
            return consolidatedQuery;
        }
        
        // Fallback to individual file
        return loadQuery(domain, queryName + ".sparql");
    }
    
    /**
     * Load a named query from a consolidated file
     */
    private static String loadNamedQuery(String domain, String queryName, String fileType) {
        String consolidatedFile = domain + "_queries." + fileType;
        String resourcePath = QUERIES_BASE_PATH + "/" + consolidatedFile;
        
        try (InputStream inputStream = QueryLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null; // Consolidated file doesn't exist, try individual files
            }
            
            String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            // Extract named query using comment markers
            String queryMarker = queryName.toUpperCase();
            
            // Try /* */ style comments first (for backward compatibility)
            Pattern pattern1 = Pattern.compile(
                "/\\*\\s*" + Pattern.quote(queryMarker) + "\\s*\\*/\\s*[\\r\\n]+(.*?)(?=/\\*|####|--\\s*Query:|$)", 
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            );
            
            Matcher matcher = pattern1.matcher(fileContent);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
            // Try #### style comments (Protégé-friendly)
            Pattern pattern2 = Pattern.compile(
                "####\\s*" + Pattern.quote(queryMarker) + "\\s*####\\s*[\\r\\n]+(.*?)(?=####|/\\*|#\\s*Query:|$)", 
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            );
            
            matcher = pattern2.matcher(fileContent);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
            return null; // Named query not found in consolidated file
            
        } catch (IOException e) {
            return null; // Fall back to individual files
        }
    }
    
    /**
     * Internal method to load query from classpath
     */
    private static String loadQuery(String domain, String filename) {
        String resourcePath = QUERIES_BASE_PATH + "/" + domain + "/" + filename;
        
        try (InputStream inputStream = QueryLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Query file not found: " + resourcePath + 
                    "\\nMake sure the file exists in src/test/resources" + resourcePath);
            }
            
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load query file: " + resourcePath, e);
        }
    }
    
    /**
     * Convenience method to load both SQL and SPARQL queries for a test case
     * @param domain The domain folder
     * @param queryName The descriptive query name  
     * @return Array with [sqlQuery, sparqlQuery]
     */
    public static String[] loadQueryPair(String domain, String queryName) {
        return new String[] {
            loadSQL(domain, queryName),
            loadSPARQL(domain, queryName)
        };
    }
}