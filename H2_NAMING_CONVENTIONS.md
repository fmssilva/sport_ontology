# h2 Database Naming Conventions - Clarification

## Why Different Cases for "H2" vs "h2"?

You noticed inconsistency in naming. Here's the **correct and consistent** usage now:

### ✅ **Correct Naming Convention**

| Context | Correct Format | Example |
|---------|---------------|---------|
| **JAR filename** | `h2-2.4.240.jar` | Lowercase "h2" |
| **Java class name** | `org.h2.Driver` | Lowercase "h2" |
| **JDBC URL protocol** | `jdbc:h2:` | Lowercase "h2" |
| **Documentation references** | "h2 database", "h2 driver" | Lowercase "h2" |

### ❌ **Inconsistent Usage (Fixed)**

Previously, documentation mixed:
- "H2 Database Engine" (uppercase)
- "H2 JDBC Driver" (uppercase)  
- "h2-2.4.240.jar" (lowercase)
- "org.h2.Driver" (lowercase)

## **In Protégé Setup:**

### JDBC Drivers Tab
```
Description: Sport_H2_DB
Class name: org.h2.Driver
Driver file: C:\...\deliverables\database\h2-2.4.240.jar
```

### Connection Parameters Tab  
```
Driver: org.h2.Driver
URL: jdbc:h2:C:/Users/franc/.../sports-deliverable-db;...
Username: sa
Password: (empty)
```

## **Why This Matters:**

### 🎯 **Consistency**
- All lowercase "h2" throughout documentation
- Matches actual Java package naming (`org.h2`)
- Matches JAR file naming (`h2-2.4.240.jar`)

### 🚀 **Less Confusion**
- No mixing of "H2" and "h2" 
- Clear, consistent instructions
- Easier to follow step-by-step guide

### 📋 **Technical Accuracy**
- Java package names are lowercase: `org.h2.*`
- JAR files follow lowercase convention
- JDBC URLs use lowercase protocol names

## **Updated in BuildDeliverables.java:**

All references now consistently use **lowercase "h2"**:
- Step names: "Install h2 JDBC Driver"
- File descriptions: "h2 JDBC driver"  
- Error messages: "h2 JAR not in plugins folder"
- Documentation: "h2 database", "h2 driver"

This eliminates the confusion between "H2" and "h2" you noticed in the Protégé interface!