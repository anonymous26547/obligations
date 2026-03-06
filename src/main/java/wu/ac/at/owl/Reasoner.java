package wu.ac.at.owl;

import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLRule;

public class Reasoner {

    //private static  GUCONManager guconManager;
    private final ContextManager ctx;
    private final OntologyService ontology;


    public Reasoner(ContextManager ctx) {
        this.ctx = ctx;
        this.ontology = new OntologyService(ctx);
	}

	public void runInferencePipeline () throws Exception
	{
		
		//System.out.println("Inference starts");
		if (ctx.getElapse().isPresent())
		{ 
			//System.out.println("elapse");
			
			ontology.assertElapse(ctx.getElapse().get());
			//System.out.println("elapse " + ctx.getElapse().get());

		}

	    // Phase 1: initial reasoning
		ctx.getOrCreate();
		
		/*OWLOntology onto = ctx.getABox();

		Set<SWRLRule> rules = onto.getAxioms(AxiomType.SWRL_RULE);

		for (SWRLRule rule : rules) {
		    System.out.println(rule);
		}*/
		
		ctx.precomputeInferences();
		
	    // Phase 2: assert CWA Closure 
		ontology.computeCwaClosure();
		
	    // Phase 3: synchronize reasoner
		ctx.synchonize();


	}
	
	public ContextManager getCtx() {
		return ctx;
	}

	public OntologyService getOntology() {
		return ontology;
	}
	

	


		
		
		
}
