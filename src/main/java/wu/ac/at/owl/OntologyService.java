package wu.ac.at.owl;

import java.util.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OntologyService {

    private final ContextManager ctx;
    private final OWLOntologyManager manager;
    private final OWLDataFactory dataFactory;
    private final OWLOntology aboxOntology;
    private final DefaultPrefixManager prefixManager;

    // optimisation
    private final Map<String, OWLClass> classCache = new HashMap<>();
    private final Map<String, OWLDataProperty> dpCache = new HashMap<>();

    private static final OWL2Datatype LONG_TYPE = OWL2Datatype.XSD_LONG;

    public OntologyService(ContextManager ctx) {
        this.ctx = ctx;
        this.manager = ctx.getManager();
        this.aboxOntology = ctx.getABox();
        this.dataFactory = ctx.getDataFactory();
        this.prefixManager = ctx.getPrefixManager();
    }

    public OWLClass createCls(String iri) {
        OWLClass cls = classCache.get(iri);
        if (cls == null) {
            if (iri.startsWith("http")) {
                cls = dataFactory.getOWLClass(IRI.create(iri));
            } else {
                cls = dataFactory.getOWLClass(iri, prefixManager);
            }
            classCache.put(iri, cls);
        }
        return cls;
    }

    public OWLDataProperty getDataProperty(String iri) {
        OWLDataProperty p = dpCache.get(iri);
        if (p == null) {
            if (iri.startsWith("http")) {
                p = dataFactory.getOWLDataProperty(IRI.create(iri));
            } else {
                p = dataFactory.getOWLDataProperty(iri, prefixManager);
            }
            dpCache.put(iri, p);
        }
        return p;
    }

    public void assertEquivalent(OWLClass Kclass, Set<OWLNamedIndividual> actions) {

        Set<OWLEquivalentClassesAxiom> oldAxioms =
                aboxOntology.getEquivalentClassesAxioms(Kclass);

        for (OWLEquivalentClassesAxiom ax : oldAxioms) {
            manager.removeAxiom(aboxOntology, ax);
        }

        if (actions == null || actions.isEmpty()) return;

        /* ✔ No HashSet copy */
        OWLObjectOneOf oneOf = dataFactory.getOWLObjectOneOf(actions);

        OWLEquivalentClassesAxiom ax =
                dataFactory.getOWLEquivalentClassesAxiom(Kclass, oneOf);

        manager.addAxiom(aboxOntology, ax);
    }

    public void assertAllDifferent(Set<OWLNamedIndividual> individuals) {

        OWLNamedIndividual[] arr =
                individuals.toArray(new OWLNamedIndividual[individuals.size()]);

        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {

                OWLDifferentIndividualsAxiom diffAx =
                        dataFactory.getOWLDifferentIndividualsAxiom(arr[i], arr[j]);

                manager.addAxiom(aboxOntology, diffAx);
            }
        }
    }

    public void assertDateTime(
            OWLNamedIndividual subject,
            String dataPropertyIRI,
            String lexicalDateTime) {

        OWLDataProperty p = getDataProperty(dataPropertyIRI);

        OWLLiteral lit = dataFactory.getOWLLiteral(lexicalDateTime, LONG_TYPE);

        OWLDataPropertyAssertionAxiom ax =
                dataFactory.getOWLDataPropertyAssertionAxiom(p, subject, lit);

        manager.addAxiom(aboxOntology, ax);
    }

    public OWLNamedIndividual getIndividual(String iri) {
        if (iri.startsWith("http")) {
            return dataFactory.getOWLNamedIndividual(IRI.create(iri));
        }
        return dataFactory.getOWLNamedIndividual(iri, prefixManager);
    }

    public void removeElapseAtTime() {

        OWLNamedIndividual elapse = getIndividual(Schema.elapseIRI);
        OWLDataProperty atTime = getDataProperty(Schema.numericAtTimeIRI);

        Set<OWLDataPropertyAssertionAxiom> oldAxioms =
                aboxOntology.getDataPropertyAssertionAxioms(elapse);

        for (OWLDataPropertyAssertionAxiom ax : oldAxioms) {
            if (ax.getProperty().asOWLDataProperty().equals(atTime)) {
                manager.removeAxiom(aboxOntology, ax);
            }
        }
    }

    public void assertElapse(String dateTime) {
        removeElapseAtTime();

        OWLNamedIndividual elapse = getIndividual(Schema.elapseIRI);

        assertDateTime(elapse, Schema.numericAtTimeIRI, dateTime);
    }

    public Set<OWLNamedIndividual> retrieveIndividuals(OWLClass cls) {
        return ctx.getOrCreate().getInstances(cls, false).getFlattened();
    }

    public void assertKOnTimeExecutedAction() {
        OWLClass onTime = createCls(Schema.OnTimeExecutedActionIRI);
        Set<OWLNamedIndividual> actions = retrieveIndividuals(onTime);

        if (!actions.isEmpty()) {
            OWLClass kClass = createCls(Schema.KOnTimeExecutedActionIRI);
            assertEquivalent(kClass, actions);
        }
    }

    public void assertRegulatedActionsDifferent() {
        OWLClass regulated = createCls(Schema.RegulatedActionIRI);
        Set<OWLNamedIndividual> actions = retrieveIndividuals(regulated);
        assertAllDifferent(actions);
    }

    public void assertNotKOnTimeExecutedAction() {
        OWLClass notK = createCls(Schema.NotKOnTimeExecutedActionIRI);
        Set<OWLNamedIndividual> actions = retrieveIndividuals(notK);

        if (!actions.isEmpty()) {
            assertEquivalent(notK, actions);
        }
    }

    public void computeCwaClosure() throws Exception {
        assertKOnTimeExecutedAction();
        assertRegulatedActionsDifferent();
        assertNotKOnTimeExecutedAction();
    }
}