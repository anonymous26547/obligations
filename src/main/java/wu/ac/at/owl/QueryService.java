package wu.ac.at.owl;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.*;

public class QueryService {

    Reasoner reasoner;
    private final DefaultPrefixManager prefixManager;
    private final OWLDataFactory dataFactory;

    private final Map<String, OWLClass> classCache = new HashMap<>();

    public QueryService(Reasoner reasoner) {
        this.reasoner = reasoner;
        this.prefixManager = reasoner.getCtx().getPrefixManager();
        this.dataFactory = reasoner.getCtx().getDataFactory();
    }

    private OWLClass getClassCached(String iri) {
        OWLClass cls = classCache.get(iri);
        if (cls == null) {
            cls = dataFactory.getOWLClass(iri, prefixManager);
            classCache.put(iri, cls);
        }
        return cls;
    }

    public static Set<String> obligationStates =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    Schema.ActiveIRI,
                    Schema.FulfilledIRI,
                    Schema.ViolatedIRI,
                    Schema.ExpiredIRI
            )));

    public static Set<String> regulatedActionStates =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    Schema.OnTimeExecutedActionIRI,
                    Schema.KOnTimeExecutedActionIRI,
                    Schema.NotKOnTimeExecutedActionIRI,
                    Schema.FailedExecutedActionIRI
            )));

    public static Set<String> temporalActionStates =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    Schema.ActiveTemporalActionIRI,
                    Schema.ExpiredTemporalActionIRI
            )));

    public static Set<String> eventStates =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    Schema.TriggeredEventIRI
            )));

    private Map<String, Set<OWLNamedIndividual>> queryClasses(Set<String> classIRIs)
            throws OWLOntologyCreationException {

        Map<String, Set<OWLNamedIndividual>> results = new HashMap<>();

        for (String classIRI : classIRIs) {
            OWLClass cls = getClassCached(classIRI);

            NodeSet<OWLNamedIndividual> instances =
                    reasoner.getCtx().getOrCreate().getInstances(cls, false);

            results.put(classIRI, instances.getFlattened());
        }

        return results;
    }

    public Map<String, Set<OWLNamedIndividual>> queryObligationStates()
            throws OWLOntologyCreationException {
        return queryClasses(obligationStates);
    }

    public Map<String, Set<OWLNamedIndividual>> queryEventStates()
            throws OWLOntologyCreationException {
        return queryClasses(eventStates);
    }

    public Map<String, Set<OWLNamedIndividual>> queryRegulatedActionStates()
            throws OWLOntologyCreationException {
        return queryClasses(regulatedActionStates);
    }

    public Map<String, Set<OWLNamedIndividual>> queryTemporalActionStates()
            throws OWLOntologyCreationException {
        return queryClasses(temporalActionStates);
    }

    public Map<String, Set<OWLNamedIndividual>> queryEntities()
            throws OWLOntologyCreationException {
        return queryClasses(Collections.singleton(Schema.EntityIRI));
    }

    public Map<String, Set<OWLNamedIndividual>> queryActions()
            throws OWLOntologyCreationException {
        return queryClasses(Collections.singleton(Schema.ActionIRI));
    }

    public Map<String, Set<OWLNamedIndividual>> queryResources()
            throws OWLOntologyCreationException {
        return queryClasses(Collections.singleton(Schema.ResourceIRI));
    }

    public Map<String, Set<OWLNamedIndividual>> queryByKeyword(String keyword)
            throws OWLOntologyCreationException {

        QueryType type = QueryType.fromKeyword(keyword);

        switch (type) {
            case OBLIGATION_STATE:
                return queryObligationStates();
            case REGULATED_ACTION_STATE:
                return queryRegulatedActionStates();
            case TEMPORAL_ACTION_STATE:
                return queryTemporalActionStates();
            case EVENT_STATE:
                return queryEventStates();
            case ENTITY:
                return queryEntities();
            case ACTION:
                return queryActions();
            case RESOURCE:
                return queryResources();
            default:
                throw new IllegalStateException("Unhandled query type: " + type);
        }
    }
}