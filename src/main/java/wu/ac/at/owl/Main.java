package wu.ac.at.owl;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class Main {
	  /**
     * High-level API:
     *  - loads TBox + ABox
     *  - applies time elapse
     *  - runs GUCON inference
     *  - executes keyword-based query
     */
    public static Map<String, Set<OWLNamedIndividual>> run(
           // String tboxPath,
            String aboxPath,
            String elapseTime,
            String queryKeyword
    ) throws Exception {

        /* =========================
           1. Load input ontologies
           ========================= */
        InputManager inputManager =
                new InputManager(elapseTime);

        inputManager.loadTboxFromClasspath("ontology-ocm-with-swrl.ttl");
        inputManager.loadAbox(aboxPath);
        inputManager.importTboxIntoAbox();

        /* =========================
           2. Create context
           ========================= */
        ContextManager ctx =
                new ContextManager(inputManager);

        /* =========================
           3. Run GUCON reasoning
           ========================= */
        Reasoner reasoner =
                new Reasoner(ctx);

        reasoner.runInferencePipeline();

        /* =========================
           4. Query
           ========================= */
        QueryService queryService =
                new QueryService(reasoner);

	    ctx.getOrCreate().dispose();

        return queryService.queryByKeyword(queryKeyword);
    }

    /* =====================================================
       CLI entry point (for runnable JAR)
       ===================================================== */
    public static void main(String[] args) throws Exception {

    	  if (args.length < 2) {
	            System.err.println(
	                "Usage:\n" +
	                "  java -jar gucont-query.jar <abox.ttl> [elapseTime] <QUERY_KEYWORD>\n\n" +
	                "Keywords:\n" +
	                "  OBLIGATION_STATE\n" +
	                "  REGULATED_ACTION_STATE\n" +
	                "  TEMPORAL_ACTION_STATE\n" +
	                "  EVENT_STATE"
	            );
	            System.exit(1);
	        }

		  String aboxPath;
		  String elapseTime = null;
		  String queryKeyword;

		  if (args.length == 3) {
		      // (elapseTime provided)
		      aboxPath     = args[0];
		      elapseTime   = args[1];
		      queryKeyword = args[2];

		  } else if (args.length == 2) {
		      // elapseTime omitted
		      aboxPath     = args[0];
		      queryKeyword = args[1];

		  } else {
		      System.err.println(
		          "Usage:\n" +
		          "  java -jar gucont-query.jar <abox.ttl> [elapseTime] <QUERY_KEYWORD>"
		      );
		      System.exit(1);
		      return;
		  }
        Map<String, Set<OWLNamedIndividual>> results =
                run(aboxPath, elapseTime, queryKeyword);

        /* =========================
           5. Print results
           ========================= */
        results.forEach((cls, inds) -> {
            System.out.println("\n== " + cls + " ==");
            if (inds.isEmpty()) {
                System.out.println("  (none)");
            } else {
                inds.forEach(i ->
                        System.out.println("  " + i.getIRI()));
            }
        });
    }
}
