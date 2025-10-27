package engines;

import config.AppConfig;
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
 * HermiT Reasoning Engine - Performs OWL reasoning with ontology and data
 * Uses centralized configuration from AppConfig for cross-platform compatibility
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

        System.out.println("Setting up HermiT Reasoning Engine");

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

        // Use centralized path configuration (absolute path for file operations)
        String ontologyPath = AppConfig.getOntologyAbsolutePath();
        File ontologyFile = new File(ontologyPath);
        if (!ontologyFile.exists()) {
            throw new RuntimeException("Ontology file not found: " + ontologyPath);
        }

        ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        System.out.println("Loaded ontology with " + ontology.getAxiomCount() + " axioms");

        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(ontology);

        boolean isConsistent = reasoner.isConsistent();
        System.out.println("Ontology consistency: " + (isConsistent ? "CONSISTENT" : "INCONSISTENT"));

        if (!isConsistent) {
            throw new RuntimeException("Ontology is inconsistent - cannot perform reasoning");
        }

        isSetup = true;
        System.out.println("HermiT Reasoning Engine setup completed");
    }

    /**
     * Load ABox data (individuals) defined in the ontology
     */
    public void addABoxData() throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }

        System.out.println("Loading ABox data for reasoning");

        reasoner.flush();
        reasoner.precomputeInferences();
        System.out.println("Reasoning precomputation completed");

        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        System.out.println("Total individuals in ontology: " + individuals.size());

        int aboxCount = 0;
        for (OWLNamedIndividual ind : individuals) {
            String iriString = ind.getIRI().toString();
            if (iriString.contains("abox#")) {
                aboxCount++;
                System.out.println("ABox: " + ind.getIRI().getShortForm());
            }
        }
        System.out.println("ABox individuals found: " + aboxCount);
    }

    /**
     * Count individuals that belong to both classes
     */
    public int countIndividualsOfBothClasses(String className1, String className2) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }

        Set<OWLNamedIndividual> class1Individuals = getIndividualsOfClass(className1);
        Set<OWLNamedIndividual> class2Individuals = getIndividualsOfClass(className2);

        Set<OWLNamedIndividual> intersection = new HashSet<>(class1Individuals);
        intersection.retainAll(class2Individuals);

        System.out.println(className1 + " individuals: " + class1Individuals.size());
        System.out.println(className2 + " individuals: " + class2Individuals.size());
        System.out.println("Intersection: " + intersection.size());

        return intersection.size();
    }

    /**
     * Helper: Get all individuals of a specific class
     */
    private Set<OWLNamedIndividual> getIndividualsOfClass(String className) throws Exception {
        IRI classIRI = IRI.create(AppConfig.createOntologyIRI(className));
        OWLClass owlClass = dataFactory.getOWLClass(classIRI);

        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(owlClass, false);
        Set<OWLNamedIndividual> result = new HashSet<>();

        for (Node<OWLNamedIndividual> node : individuals) {
            result.addAll(node.getEntities());
        }

        return result;
    }

    /**
     * Count individuals of a specific class
     */
    public int countIndividualsOfClass(String className) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }

        IRI classIRI = IRI.create(AppConfig.createOntologyIRI(className));
        OWLClass owlClass = dataFactory.getOWLClass(classIRI);

        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(owlClass, false);

        Set<OWLNamedIndividual> uniqueIndividuals = new HashSet<>();
        for (Node<OWLNamedIndividual> node : individuals) {
            uniqueIndividuals.addAll(node.getEntities());
        }

        return uniqueIndividuals.size();
    }

    /**
     * Check if an individual belongs to a class
     */
    public boolean isIndividualOfClass(String individualName, String className) throws Exception {
        if (!isSetup) {
            throw new IllegalStateException("Reasoning engine not setup");
        }

        IRI individualIRI = IRI.create(AppConfig.createABoxIRI(individualName));
        IRI classIRI = IRI.create(AppConfig.createOntologyIRI(className));

        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);
        OWLClass owlClass = dataFactory.getOWLClass(classIRI);

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

        IRI individualIRI = IRI.create(AppConfig.createABoxIRI(individualName));
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(individualIRI);

        NodeSet<OWLClass> types = reasoner.getTypes(individual, false);
        Set<String> classNames = new HashSet<>();

        for (Node<OWLClass> node : types) {
            for (OWLClass owlClass : node.getEntities()) {
                classNames.add(owlClass.getIRI().getShortForm());
            }
        }

        return classNames;
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
