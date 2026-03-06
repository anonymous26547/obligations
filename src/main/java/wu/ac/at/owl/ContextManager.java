package wu.ac.at.owl;

import java.util.Optional;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;





public class ContextManager {

	// from input manager
    private final OWLOntologyManager manager;
    private final OWLDataFactory dataFactory;
    private OWLOntology abox;
    private OWLOntology tbox;
    private final DefaultPrefixManager prefixManager;
    private Optional <String> elapse;
   
    // native to the context
    private OWLReasoner reasoner;
    
    public ContextManager(InputManager inputManager) {
        this.manager = inputManager.getManager();
        this.dataFactory = inputManager.getDataFactory();
        this.abox = inputManager.getABox();
        this.tbox = inputManager.getTBox();
        this.prefixManager = inputManager.getPrefixManager();
        this.elapse=inputManager.getElapse();
    }
    // ---------------- Ontology access ----------------
    public OWLOntologyManager getManager() {
        return manager;
    }

    public OWLDataFactory getDataFactory() {
        return dataFactory;
    }

    public OWLOntology getABox() {
        return abox;
    }

    public void setABox(OWLOntology abox) {
        this.abox = abox;
    }

    public OWLOntology getTBox() {
        return tbox;
    }
    
    public DefaultPrefixManager getPrefixManager() {
        return prefixManager;
    }

    public Optional<String> getElapse() {
		return elapse;
	}
	public void setInputElapse(Optional<String>  elapse) {
		this.elapse = elapse;
	}
    // ---------------- Reasoner lifecycle ----------------

	public void synchonize() throws Exception {
        if (reasoner != null) {
            reasoner.dispose();
        }

        reasoner = PelletReasonerFactory.getInstance().createReasoner(abox);

        if (!reasoner.isConsistent()) {
            throw new Exception("Ontology is inconsistent");
        }
        //System.out.println("reasoner synchronised");

    }
	
    public OWLReasoner getOrCreate() {
        if (reasoner == null) {
            reasoner = PelletReasonerFactory.getInstance()
                    .createReasoner(abox);
        }
        //System.out.println("reasoner created");
        return reasoner;
    }

    public void precomputeInferences() {
    	getOrCreate().precomputeInferences(
                InferenceType.CLASS_ASSERTIONS,
                InferenceType.CLASS_HIERARCHY

        );
    }


  
}
