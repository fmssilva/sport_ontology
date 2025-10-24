package engines;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.Node;

import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * HermiT Reasoning Engine - Performs OWL reasoning with full ontology + ABox
 * Simulates the complete reasoning stack like Protege with ontology + data
 */
public class ReasoningEngine {
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLDataFactory dataFactory;
    private boolean isSetup = false;
    
    /**
     * Setup the reasoning engine with ontology
     */
    public void setup() throws Exception {
        if (isSetup) {
            System.out.println("Reasoning engine already setup");
            return;
        }
        
        System.out.println("Setting up HermiT Reasoning Engine...");
        
        // Initialize OWL API
        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        
        // Load ontology
        String ontologyPath = "src/main/resources/ontology/sport-ontology.owl";
        File ontologyFile = new File(ontologyPath);
        if (!ontologyFile.exists()) {
            throw new RuntimeException("Ontology file not found: " + ontologyPath);
        }
        
        ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        System.out.println("   Loaded ontology with " + ontology.getAxiomCount() + " axioms");
        
        // Create HermiT reasoner
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);
        
        // Check consistency
        boolean isConsistent = reasoner.isConsistent();
        System.out.println("   Ontology consistency: " + (isConsistent ? "CONSISTENT" : "INCONSISTENT"));
        
        if (!isConsistent) {
            throw new RuntimeException("Ontology is inconsistent - cannot perform reasoning");
        }
        
        isSetup = true;
        System.out.println("HermiT Reasoning Engine setup completed");
    }
    
    /**
     * Load ABox data (individuals) that are already defined in the ontology
     */
    public void addABoxData() throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        System.out.println("Loading ABox data for reasoning...");
        
        // ABox individuals are already defined in the ontology file
        // Just force reasoner to recompute with existing data
        reasoner.flush();
        
        // Precompute inferences
        System.out.println("   Computing inferences...");
        reasoner.precomputeInferences();
        System.out.println("   Reasoning precomputation completed");
        
        // Debug: Show all individuals in the abox namespace
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        System.out.println("   Total individuals in ontology: " + individuals.size());
        
        int aboxCount = 0;
        for (OWLNamedIndividual ind : individuals) {
            String iriString = ind.getIRI().toString();
            if (iriString.contains("abox#")) {
                aboxCount++;
                System.out.println("     - ABox: " + ind.getIRI().getShortForm());
            }
        }
        System.out.println("   ABox individuals found: " + aboxCount);
    }
    
    /**
     * Count individuals that belong to BOTH classes (intersection)
     */
    public int countIndividualsOfBothClasses(String className1, String className2) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        // Get individuals of both classes
        Set<OWLNamedIndividual> class1Individuals = getIndividualsOfClass(className1);
        Set<OWLNamedIndividual> class2Individuals = getIndividualsOfClass(className2);
        
        // Find intersection
        Set<OWLNamedIndividual> intersection = new HashSet<>(class1Individuals);
        intersection.retainAll(class2Individuals);
        
        System.out.println("   " + className1 + " individuals: " + class1Individuals.size());
        System.out.println("   " + className2 + " individuals: " + class2Individuals.size());
        System.out.println("   Intersection: " + intersection.size());
        
        return intersection.size();
    }
    
    /**
     * Helper: Get all individuals of a specific class
     */
    private Set<OWLNamedIndividual> getIndividualsOfClass(String className) throws Exception {
        IRI classIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + className);
        OWLClass owlClass = dataFactory.getOWLClass(classIRI);
        
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(owlClass, false);
        Set<OWLNamedIndividual> result = new HashSet<>();
        
        for (Node<OWLNamedIndividual> node : individuals) {
            result.addAll(node.getEntities());
        }
        
        return result;
    }

    /**
     * Count individuals of a specific class (including inferred)
     */
    public int countIndividualsOfClass(String className) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        // Create the class
        IRI classIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + className);
        OWLClass owlClass = dataFactory.getOWLClass(classIRI);
        
        // Get all individuals of this class (including inferred)
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(owlClass, false);
        
        // Count unique individuals
        Set<OWLNamedIndividual> uniqueIndividuals = new HashSet<>();
        for (Node<OWLNamedIndividual> node : individuals) {
            uniqueIndividuals.addAll(node.getEntities());
        }
        
        return uniqueIndividuals.size();
    }
    
    /**
     * Check if an individual belongs to a class (including inferred)
     */
    public boolean isIndividualOfClass(String individualName, String className) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        // Create individual and class - individuals are in abox namespace
        IRI individualIRI = IRI.create("http://www.semanticweb.org/sports/abox#" + individualName);
        IRI classIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + className);
        
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);
        OWLClass owlClass = dataFactory.getOWLClass(classIRI);
        
        // Check if individual is instance of class (including inferred)
        NodeSet<OWLClass> types = reasoner.getTypes(individual, false);
        
        for (Node<OWLClass> node : types) {
            if (node.contains(owlClass)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all inferred classes for an individual
     */
    public Set<String> getInferredClassesForIndividual(String individualName) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        IRI individualIRI = IRI.create("http://www.semanticweb.org/sports/abox#" + individualName);
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);
        
        // First check if individual exists in ontology
        Set<OWLClassAssertionAxiom> assertions = ontology.getClassAssertionAxioms(individual);
        System.out.println("   Direct assertions for " + individualName + ": " + assertions.size());
        
        NodeSet<OWLClass> types = reasoner.getTypes(individual, false);
        Set<String> classNames = new HashSet<>();
        
        System.out.println("   Reasoning results for " + individualName + ":");
        for (Node<OWLClass> node : types) {
            for (OWLClass owlClass : node.getEntities()) {
                String shortName = owlClass.getIRI().getShortForm();
                classNames.add(shortName);
                System.out.println("     - " + shortName);
            }
        }
        
        return classNames;
    }
    
    /**
     * Check if an individual has a specific property value (for property chain testing)
     */
    public boolean hasPropertyValue(String individualName, String propertyName) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        IRI individualIRI = IRI.create("http://www.semanticweb.org/sports/abox#" + individualName);
        IRI propertyIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + propertyName);
        
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);
        OWLObjectProperty property = dataFactory.getOWLObjectProperty(propertyIRI);
        
        // Get all property assertions for this individual
        NodeSet<OWLNamedIndividual> propertyValues = reasoner.getObjectPropertyValues(individual, property);
        
        return !propertyValues.isEmpty();
    }
    
    /**
     * Check if there's a specific object property relationship between two individuals
     */
    public boolean hasObjectPropertyRelation(String subjectName, String propertyName, String objectName) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        IRI subjectIRI = IRI.create("http://www.semanticweb.org/sports/abox#" + subjectName);
        IRI objectIRI = IRI.create("http://www.semanticweb.org/sports/abox#" + objectName);
        IRI propertyIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + propertyName);
        
        OWLNamedIndividual subject = dataFactory.getOWLNamedIndividual(subjectIRI);
        OWLNamedIndividual object = dataFactory.getOWLNamedIndividual(objectIRI);
        OWLObjectProperty property = dataFactory.getOWLObjectProperty(propertyIRI);
        
        // Get all values for this property
        NodeSet<OWLNamedIndividual> propertyValues = reasoner.getObjectPropertyValues(subject, property);
        
        // Check if the specific object is among the values
        for (Node<OWLNamedIndividual> node : propertyValues) {
            if (node.contains(object)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all individuals that have a specific object property relationship
     */
    public Set<String> getIndividualsWithProperty(String propertyName) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }
        
        IRI propertyIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + propertyName);
        OWLObjectProperty property = dataFactory.getOWLObjectProperty(propertyIRI);
        
        Set<String> individualNames = new HashSet<>();
        
        // Check all individuals in the ontology
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        
        for (OWLNamedIndividual individual : individuals) {
            NodeSet<OWLNamedIndividual> propertyValues = reasoner.getObjectPropertyValues(individual, property);
            if (!propertyValues.isEmpty()) {
                individualNames.add(individual.getIRI().getShortForm());
            }
        }
        
        return individualNames;
    }
    
    /**
     * Helper: Add a player individual with properties
     */
    private void addPlayerIndividual(String name, float marketValue, int age) throws Exception {
        IRI playerIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + name);
        OWLNamedIndividual player = dataFactory.getOWLNamedIndividual(playerIRI);
        
        // Assert player is a Player
        OWLClass playerClass = dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/sports/ontology#Player"));
        OWLClassAssertionAxiom playerAssertion = dataFactory.getOWLClassAssertionAxiom(playerClass, player);
        manager.addAxiom(ontology, playerAssertion);
        
        // Add market value property
        OWLDataProperty marketValueProperty = dataFactory.getOWLDataProperty(
            IRI.create("http://www.semanticweb.org/sports/ontology#hasMarketValue"));
        OWLLiteral marketValueLiteral = dataFactory.getOWLLiteral(marketValue);
        OWLDataPropertyAssertionAxiom marketValueAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(
            marketValueProperty, player, marketValueLiteral);
        manager.addAxiom(ontology, marketValueAssertion);
        
        // Add age property
        OWLDataProperty ageProperty = dataFactory.getOWLDataProperty(
            IRI.create("http://www.semanticweb.org/sports/ontology#hasAge"));
        OWLLiteral ageLiteral = dataFactory.getOWLLiteral(age);
        OWLDataPropertyAssertionAxiom ageAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(
            ageProperty, player, ageLiteral);
        manager.addAxiom(ontology, ageAssertion);
    }
    
    /**
     * Helper: Add a team individual
     */
    private void addTeamIndividual(String name) throws Exception {
        IRI teamIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + name);
        OWLNamedIndividual team = dataFactory.getOWLNamedIndividual(teamIRI);
        
        OWLClass teamClass = dataFactory.getOWLClass(IRI.create("http://www.semanticweb.org/sports/ontology#Team"));
        OWLClassAssertionAxiom teamAssertion = dataFactory.getOWLClassAssertionAxiom(teamClass, team);
        manager.addAxiom(ontology, teamAssertion);
    }
    
    /**
     * Helper: Add playsFor relationship
     */
    private void addPlaysForRelationship(String playerName, String teamName) throws Exception {
        IRI playerIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + playerName);
        IRI teamIRI = IRI.create("http://www.semanticweb.org/sports/ontology#" + teamName);
        
        OWLNamedIndividual player = dataFactory.getOWLNamedIndividual(playerIRI);
        OWLNamedIndividual team = dataFactory.getOWLNamedIndividual(teamIRI);
        
        OWLObjectProperty playsForProperty = dataFactory.getOWLObjectProperty(
            IRI.create("http://www.semanticweb.org/sports/ontology#playsFor"));
        OWLObjectPropertyAssertionAxiom playsForAssertion = dataFactory.getOWLObjectPropertyAssertionAxiom(
            playsForProperty, player, team);
        manager.addAxiom(ontology, playsForAssertion);
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (reasoner != null) {
            reasoner.dispose();
        }
        System.out.println("HermiT Reasoning Engine cleanup completed");
    }
    
    public boolean isSetup() {
        return isSetup;
    }
}