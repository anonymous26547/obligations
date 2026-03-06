package wu.ac.at.owl;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Hello world!
 */
public class App {
	  public static void main(String[] args) throws Exception {
		  
		  /*to remove logging, add this pom.xml
		   * 	   
		<!-- SLF4J API -->
		<dependency>
		  <groupId>org.slf4j</groupId>
		  <artifactId>slf4j-api</artifactId>
		  <version>1.7.36</version>
		</dependency>
		
		<!-- Bridge java.util.logging → SLF4J -->
		<dependency>
		  <groupId>org.slf4j</groupId>
		  <artifactId>jul-to-slf4j</artifactId>
		  <version>1.7.36</version>
		</dependency>
		
		<!-- SLF4J NOP backend (silences everything) -->
		<dependency>
		  <groupId>org.slf4j</groupId>
		  <artifactId>slf4j-nop</artifactId>
		  <version>1.7.36</version>
		</dependency>
		
		   * 
		   * 
		   * 
		   * 
		   * 
		   * 
		   * 
		   */
		    //System.out.println(System.getProperty("java.runtime.version"));
		  // Remove default JUL handlers
	         SLF4JBridgeHandler.removeHandlersForRootLogger();
	        // Route JUL → SLF4J
	         SLF4JBridgeHandler.install();
		   // String inputElapse="1767875400000";
		    //InputManager guconManager = new InputManager(inputElapse);
		    InputManager guconManager = new InputManager(null);
			try {
			     // TBox
				//OWLOntology tbox=guconManager.loadTbox("Z:/PhD/GUCONT-Obl/owl/v10/dev/ontology-gucon-with-swrl-v10.ttl");
				guconManager.loadTboxFromClasspath("ontology-ocm-with-swrl.ttl");

				 // ABox
				guconManager.loadAbox("Z:/PhD/GUCONT-Obl/owl/evaluation/generated/generated-obligations-9.ttl"); 
				
				// resolve input elapse
				//guconManager.resolveInputElapse();
				
				// setTime here!
				} catch (OWLOntologyCreationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			
			// change to create knwoledgeBase
			 guconManager.importTboxIntoAbox();
			// System.out.println(" default prefix "+guconManager.getPrefixManager().getDefaultPrefix());
			 //System.out.println(" gem prefix "+guconManager.getPrefixManager().getPrefixNames().toString());

			 ContextManager context = new ContextManager(guconManager);			 

			Reasoner reasoner = new Reasoner(context);

			reasoner.runInferencePipeline();

			QueryService query = new QueryService(reasoner);
			
			
			
	    	Map<String, Set<OWLNamedIndividual>>  results= query.queryObligationStates();
	    	int fulfilled = 0;
	    	int active =0;
	    	int violated=0;
	    	int expired=0;
	    	for (Map.Entry<String, Set<OWLNamedIndividual>> entry : results.entrySet()) {
	    		String classIRI = entry.getKey();
	    	    Set<OWLNamedIndividual> individuals = entry.getValue();
	    	    if (classIRI.equals("gem:Fulfilled")) {
	    	    	fulfilled=individuals.size();
	    	    }
	    	    if (classIRI=="gem:Active") {
	    	    	active=individuals.size();
	    	    }
	    	    if (classIRI=="gem:Violated") {
	    	    	violated=individuals.size();
	    	    }
	    	    if (classIRI=="gem:Expired") {
	    	    	expired=individuals.size();
	    	    }

	    	    System.out.println("Class " + classIRI);
	    	    individuals.forEach(ind ->
	    	        System.out.println("  - " + ind.getIRI())
	    	    );
	    	}
	        System.out.println(" ------- ");
	        
	        System.out.println("size of fulfilled "+fulfilled);
	        System.out.println("size of active "+active);
	        System.out.println("size of violated "+violated);
	        System.out.println("size of expired "+expired);

          
	    	/*Map<String, Set<OWLNamedIndividual>>  results1= query.queryRegulatedActionStates();
	    	for (Map.Entry<String, Set<OWLNamedIndividual>> entry : results1.entrySet()) {
	    		String classIRI = entry.getKey();
	    	    Set<OWLNamedIndividual> individuals = entry.getValue();
	    	    System.out.println("Class " + classIRI);
	    	    individuals.forEach(ind ->
	    	        System.out.println("  - " + ind.getIRI())
	    	    );
	    	}*/
	    	
	    	context.getOrCreate().dispose(); // 
			
			//guconManager.saveInferredABox("Z:/PhD/GUCONT-Obl/owl/v12y/dev/kb-inferred.ttl");
		  }
	    
}
