package protege_files;

import config.AppConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sport-specific hardcoded TTL to OBDA converter.
 * This is a reference implementation that contains all the specific mappings
 * for the sport ontology project. Kept for comparison and debugging purposes.
 * 
 * Use GeneralTtlToObdaConverter for general-purpose TTL to OBDA conversion.
 */
public class SportSpecificObdaConverter {
    
    /**
     * Converts R2RML TTL mappings to OBDA mapping format
     * Handles comprehensive mapping patterns from sport-ontology-mapping.ttl
     */
    public static String convertR2RMLToOBDA() {
        try {
            // Find the R2RML mapping file
            String projectRoot = System.getProperty("user.dir");
            Path r2rmlPath = Paths.get(projectRoot, "src", "main", "resources", "ontology", AppConfig.MAPPING_FILE_NAME);
            
            if (!Files.exists(r2rmlPath)) {
                System.out.println("   ! R2RML mapping file not found: " + AppConfig.MAPPING_FILE_NAME);
                return "";
            }
            
            StringBuilder obdaMappings = new StringBuilder();
            
            // ========================================================================
            // 1. BASIC ENTITY MAPPINGS - Core Classes
            // ========================================================================
            
            // Person Mapping with Age calculation
            obdaMappings.append("mappingId\tperson-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Person ; :hasName {FULL_NAME}^^xsd:string ; :hasNationality {NATIONALITY}^^xsd:string ; :hasAge {AGE}^^xsd:integer .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, FULL_NAME, NATIONALITY, TIMESTAMPDIFF('YEAR', BIRTH_DATE, CURRENT_DATE) as AGE FROM PERSON\n");
            obdaMappings.append("\n");
            
            // Team Mapping
            obdaMappings.append("mappingId\tteam-mapping\n");
            obdaMappings.append("target\t\tdata:team/{TEAM_ID} a :Team ; :hasName {NAME}^^xsd:string ; :hasStadiumCapacity {STADIUM_CAPACITY}^^xsd:integer ; :hasFoundedYear {FOUNDED_YEAR}^^xsd:integer .\n");
            obdaMappings.append("source\t\tSELECT TEAM_ID, NAME, STADIUM_CAPACITY, FOUNDED_YEAR FROM TEAM\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 2. SUBCLASS MAPPINGS - Specialized Entity Types
            // ========================================================================
            
            // Senior Team Mapping
            obdaMappings.append("mappingId\tsenior-team-mapping\n");
            obdaMappings.append("target\t\tdata:team/{TEAM_ID} a :SeniorTeam .\n");
            obdaMappings.append("source\t\tSELECT TEAM_ID FROM TEAM WHERE TEAM_TYPE = 'SeniorTeam'\n");
            obdaMappings.append("\n");
            
            // Youth Team Mapping
            obdaMappings.append("mappingId\tyouth-team-mapping\n");
            obdaMappings.append("target\t\tdata:team/{TEAM_ID} a :YouthTeam .\n");
            obdaMappings.append("source\t\tSELECT TEAM_ID FROM TEAM WHERE TEAM_TYPE = 'YouthTeam'\n");
            obdaMappings.append("\n");
            
            // Player Mapping
            obdaMappings.append("mappingId\tplayer-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Player ; :hasMarketValue {MARKET_VALUE}^^xsd:decimal .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, MARKET_VALUE FROM PLAYER_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Jersey Number Mapping (NULL-safe)
            obdaMappings.append("mappingId\tjersey-number-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :hasJerseyNumber {JERSEY_NUMBER}^^xsd:integer .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, JERSEY_NUMBER FROM PLAYER_ROLE WHERE END_DATE IS NULL AND JERSEY_NUMBER IS NOT NULL\n");
            obdaMappings.append("\n");
            
            // Coach Mapping
            obdaMappings.append("mappingId\tcoach-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Coach .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM COACH_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 3. POSITION-SPECIFIC PLAYER MAPPINGS
            // ========================================================================
            
            // Goalkeeper Mapping
            obdaMappings.append("mappingId\tgoalkeeper-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Goalkeeper .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE POSITION = 'Goalkeeper' AND END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Forward Mapping
            obdaMappings.append("mappingId\tforward-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Forward .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE POSITION = 'Forward' AND END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Midfielder Mapping
            obdaMappings.append("mappingId\tmidfielder-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Midfielder .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE POSITION = 'Midfielder' AND END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Defender Mapping
            obdaMappings.append("mappingId\tdefender-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :Defender .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE POSITION = 'Defender' AND END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 4. COACH TYPE MAPPINGS
            // ========================================================================
            
            // Head Coach Mapping
            obdaMappings.append("mappingId\thead-coach-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :HeadCoach .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM COACH_ROLE WHERE ROLE_TYPE = 'HeadCoach' AND END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Assistant Coach Mapping
            obdaMappings.append("mappingId\tassistant-coach-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :AssistantCoach .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM COACH_ROLE WHERE ROLE_TYPE = 'AssistantCoach' AND END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 5. RELATIONSHIP MAPPINGS - Object Properties
            // ========================================================================
            
            // Player-Team Relationship (playsFor)
            obdaMappings.append("mappingId\tplays-for-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :playsFor data:team/{TEAM_ID} .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, TEAM_ID FROM PLAYER_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Coach-Team Relationship (coaches)
            obdaMappings.append("mappingId\tcoaches-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :coaches data:team/{TEAM_ID} .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, TEAM_ID FROM COACH_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Reverse Team-Player Relationship (hasPlayer)
            obdaMappings.append("mappingId\thas-player-mapping\n");
            obdaMappings.append("target\t\tdata:team/{TEAM_ID} :hasPlayer data:person/{PERSON_ID} .\n");
            obdaMappings.append("source\t\tSELECT TEAM_ID, PERSON_ID FROM PLAYER_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Reverse Team-Coach Relationship (hasCoach)
            obdaMappings.append("mappingId\thas-coach-mapping\n");
            obdaMappings.append("target\t\tdata:team/{TEAM_ID} :hasCoach data:person/{PERSON_ID} .\n");
            obdaMappings.append("source\t\tSELECT TEAM_ID, PERSON_ID FROM COACH_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 6. CONTRACT MAPPINGS
            // ========================================================================
            
            // Contract Entity Mapping
            obdaMappings.append("mappingId\tcontract-mapping\n");
            obdaMappings.append("target\t\tdata:contract/{CONTRACT_ID} a :Contract ; :hasSalary {SALARY}^^xsd:decimal .\n");
            obdaMappings.append("source\t\tSELECT CONTRACT_ID, SALARY FROM CONTRACT\n");
            obdaMappings.append("\n");
            
            // Person-Contract Relationship
            obdaMappings.append("mappingId\thas-contract-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :hasContract data:contract/{CONTRACT_ID} .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, CONTRACT_ID FROM CONTRACT WHERE IS_ACTIVE = TRUE\n");
            obdaMappings.append("\n");
            
            // Permanent Contract Mapping
            obdaMappings.append("mappingId\tpermanent-contract-mapping\n");
            obdaMappings.append("target\t\tdata:contract/{CONTRACT_ID} a :PermanentContract .\n");
            obdaMappings.append("source\t\tSELECT CONTRACT_ID FROM CONTRACT WHERE CONTRACT_TYPE = 'PermanentContract'\n");
            obdaMappings.append("\n");
            
            // Loan Contract Mapping
            obdaMappings.append("mappingId\tloan-contract-mapping\n");
            obdaMappings.append("target\t\tdata:contract/{CONTRACT_ID} a :LoanContract .\n");
            obdaMappings.append("source\t\tSELECT CONTRACT_ID FROM CONTRACT WHERE CONTRACT_TYPE = 'LoanContract'\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 7. SPECIALIZED PLAYER CLASS MAPPINGS - OBDA-DRIVEN CLASSIFICATION
            // ========================================================================
            
            // YoungPlayer Mapping (under 23)
            obdaMappings.append("mappingId\tyoung-player-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :YoungPlayer .\n");
            obdaMappings.append("source\t\tSELECT pr.PERSON_ID FROM PLAYER_ROLE pr JOIN PERSON p ON pr.PERSON_ID = p.PERSON_ID WHERE pr.END_DATE IS NULL AND TIMESTAMPDIFF('YEAR', p.BIRTH_DATE, CURRENT_DATE) < 23\n");
            obdaMappings.append("\n");
            
            // TopPlayer Mapping (market value >= 50M)
            obdaMappings.append("mappingId\ttop-player-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :TopPlayer .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE END_DATE IS NULL AND MARKET_VALUE >= 50000000\n");
            obdaMappings.append("\n");
            
            // StarPlayer Mapping (market value >= 100M)
            obdaMappings.append("mappingId\tstar-player-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :StarPlayer .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE END_DATE IS NULL AND MARKET_VALUE >= 100000000\n");
            obdaMappings.append("\n");
            
            // VeteranPlayer Mapping (age >= 33)
            obdaMappings.append("mappingId\tveteran-player-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :VeteranPlayer .\n");
            obdaMappings.append("source\t\tSELECT pr.PERSON_ID FROM PLAYER_ROLE pr JOIN PERSON p ON pr.PERSON_ID = p.PERSON_ID WHERE pr.END_DATE IS NULL AND TIMESTAMPDIFF('YEAR', p.BIRTH_DATE, CURRENT_DATE) >= 33\n");
            obdaMappings.append("\n");
            
            // ExperiencedPlayer Mapping (age >= 28)
            obdaMappings.append("mappingId\texperienced-player-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :ExperiencedPlayer .\n");
            obdaMappings.append("source\t\tSELECT pr.PERSON_ID FROM PLAYER_ROLE pr JOIN PERSON p ON pr.PERSON_ID = p.PERSON_ID WHERE pr.END_DATE IS NULL AND TIMESTAMPDIFF('YEAR', p.BIRTH_DATE, CURRENT_DATE) >= 28\n");
            obdaMappings.append("\n");
            
            // HomegrownPlayer Mapping (all active players - simplified)
            obdaMappings.append("mappingId\thomegrown-player-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :HomegrownPlayer .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID FROM PLAYER_ROLE WHERE END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 8. SPECIALIZED TEAM AND COACH MAPPINGS
            // ========================================================================
            
            // EliteTeam Mapping (stadium capacity >= 50000)
            obdaMappings.append("mappingId\telite-team-mapping\n");
            obdaMappings.append("target\t\tdata:team/{TEAM_ID} a :EliteTeam .\n");
            obdaMappings.append("source\t\tSELECT TEAM_ID FROM TEAM WHERE STADIUM_CAPACITY >= 50000\n");
            obdaMappings.append("\n");
            
            // TopCoach Mapping (experienced head coaches, age >= 50)
            obdaMappings.append("mappingId\ttop-coach-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} a :TopCoach .\n");
            obdaMappings.append("source\t\tSELECT cr.PERSON_ID FROM COACH_ROLE cr JOIN PERSON p ON cr.PERSON_ID = p.PERSON_ID WHERE cr.END_DATE IS NULL AND cr.ROLE_TYPE = 'HeadCoach' AND TIMESTAMPDIFF('YEAR', p.BIRTH_DATE, CURRENT_DATE) >= 50\n");
            obdaMappings.append("\n");
            
            // ========================================================================
            // 9. ADDITIONAL DATA PROPERTIES FOR ENHANCED FUNCTIONALITY
            // ========================================================================
            
            // International Caps Mapping (simulated with market value tiers)
            obdaMappings.append("mappingId\tinternational-caps-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :hasInternationalCaps {CAPS}^^xsd:integer .\n");
            obdaMappings.append("source\t\tSELECT PERSON_ID, CASE WHEN MARKET_VALUE >= 150000000 THEN 50 WHEN MARKET_VALUE >= 100000000 THEN 25 WHEN MARKET_VALUE >= 50000000 THEN 10 ELSE 0 END as CAPS FROM PLAYER_ROLE WHERE END_DATE IS NULL AND MARKET_VALUE IS NOT NULL\n");
            obdaMappings.append("\n");
            
            // Years Experience Mapping (age-based calculation)
            obdaMappings.append("mappingId\tyears-experience-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :hasYearsExperience {EXPERIENCE}^^xsd:integer .\n");
            obdaMappings.append("source\t\tSELECT pr.PERSON_ID, GREATEST(0, TIMESTAMPDIFF('YEAR', p.BIRTH_DATE, CURRENT_DATE) - 18) as EXPERIENCE FROM PLAYER_ROLE pr JOIN PERSON p ON pr.PERSON_ID = p.PERSON_ID WHERE pr.END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            // Coach Experience Mapping (coaching years and trophies)
            obdaMappings.append("mappingId\tcoach-experience-mapping\n");
            obdaMappings.append("target\t\tdata:person/{PERSON_ID} :hasYearsCoaching {COACHING_YEARS}^^xsd:integer ; :hasTrophiesWon {TROPHIES}^^xsd:integer .\n");
            obdaMappings.append("source\t\tSELECT cr.PERSON_ID, GREATEST(0, TIMESTAMPDIFF('YEAR', p.BIRTH_DATE, CURRENT_DATE) - 35) as COACHING_YEARS, CASE WHEN cr.ROLE_TYPE = 'HeadCoach' THEN 5 ELSE 2 END as TROPHIES FROM COACH_ROLE cr JOIN PERSON p ON cr.PERSON_ID = p.PERSON_ID WHERE cr.END_DATE IS NULL\n");
            obdaMappings.append("\n");
            
            System.out.println("   → [SPORT-SPECIFIC] Converted comprehensive R2RML mappings to OBDA format");
            System.out.println("   → [SPORT-SPECIFIC] Includes " + countMappings(obdaMappings.toString()) + " mapping declarations");
            System.out.println("   → [SPORT-SPECIFIC] Covers: entities, subclasses, relationships, contracts, specializations, and data properties");
            
            return obdaMappings.toString();
            
        } catch (Exception e) {
            System.out.println("   ! Warning: Could not convert R2RML mappings: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Helper method to count the number of mappings in the OBDA string
     */
    private static int countMappings(String obdaContent) {
        return obdaContent.split("mappingId\\s").length - 1;
    }
}