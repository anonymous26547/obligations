package wu.ac.at.owl;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;


public class InputManager {

    private final OWLOntologyManager manager;
    private final OWLDataFactory dataFactory;
	private OWLOntology tboxOntology;
    private OWLOntology aboxOntology;
    private OWLOntology knowledgeBase;
    private DefaultPrefixManager prefixManager;
    private Optional<String> elapse;
    

    public InputManager(String inputElapse) {
        this.manager = OWLManager.createOWLOntologyManager();
        this.dataFactory = manager.getOWLDataFactory();
        this.elapse = resolveInputElapse(inputElapse); // may be null  
    }
    
    /* =========================
       Load TBox
       ========================= */
    public OWLOntology loadTbox(String tboxPath) throws OWLOntologyCreationException {
        this.tboxOntology =
                manager.loadOntologyFromOntologyDocument(new File(tboxPath));
        
        // Extract default + all prefixes from TBox
        this.prefixManager = extractPrefixesFromTBox();
        // System.out.println("loaded tbox");

        return tboxOntology;
    }

    public OWLOntology loadTboxFromClasspath(String resourcePath)
            throws OWLOntologyCreationException {

        InputStream is =
            InputManager.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IllegalArgumentException(
                "TBox resource not found on classpath: " + resourcePath);
        }

        this.tboxOntology =
            manager.loadOntologyFromOntologyDocument(is);

        this.prefixManager = extractPrefixesFromTBox();
        
        //System.out.println("loaded tbox");

        return tboxOntology;
    }

    /* =========================
       Load ABox (Policy)
       ========================= */
    public OWLOntology loadAbox(String aboxPath) throws OWLOntologyCreationException {
        this.aboxOntology =
                manager.loadOntologyFromOntologyDocument(new File(aboxPath));
        
        
        // Apply TBox prefixes to ABox document format
        if (prefixManager != null) {
            OWLDocumentFormat format = manager.getOntologyFormat(aboxOntology);
            if (format != null && format.isPrefixOWLOntologyFormat()) {
                format.asPrefixOWLOntologyFormat()
                      .copyPrefixesFrom(prefixManager);
            }
        }
      
        //System.out.println("loaded abox");
        return aboxOntology;
    }


    

    
    /* =========================
       Import TBox into ABox
       ========================= */
    public void importTboxIntoAbox() {

        if (tboxOntology == null || aboxOntology == null) {
            throw new IllegalStateException(
                    "Both TBox and ABox must be loaded before importing");
        }

        // TBox ontology IRI (used in owl:imports)
        com.google.common.base.Optional<IRI>  tboxIRIOptional = tboxOntology.getOntologyID().getOntologyIRI();
        if (!tboxIRIOptional.isPresent()) {
            throw new IllegalArgumentException("TBox ontology must have an ontology IRI");
        }   
        IRI tboxIRI = tboxIRIOptional.get();
        //  document IRI 
        IRI tboxDocumentIRI =
                manager.getOntologyDocumentIRI(tboxOntology);

        // Teach the manager how to resolve the import
        manager.getIRIMappers().add(
                new SimpleIRIMapper(tboxIRI, tboxDocumentIRI)
        );

        // Add import axiom to ABox
        OWLImportsDeclaration importDecl =
                dataFactory.getOWLImportsDeclaration(tboxIRI);

        manager.applyChange(new AddImport(aboxOntology, importDecl));
 
        // ---- Debug  ----
       // System.out.println("Added import: " + tboxIRI);
       // System.out.println("Imports closure size: "
         //       + aboxOntology.getImportsClosure().size());
    }

    private DefaultPrefixManager extractPrefixesFromTBox() {

        DefaultPrefixManager pm = new DefaultPrefixManager();

        // 1) Copy prefixes from TBox document format (if Turtle / prefix-aware)
        OWLDocumentFormat format = manager.getOntologyFormat(tboxOntology);
        if (format != null && format.isPrefixOWLOntologyFormat()) {
            PrefixDocumentFormat prefixFormat = format.asPrefixOWLOntologyFormat();
            prefixFormat.getPrefixName2PrefixMap()
                        .forEach(pm::setPrefix);
        }

        // 2) Ensure default prefix exists (fallback)
        com.google.common.base.Optional<IRI> iriOpt =
                tboxOntology.getOntologyID().getOntologyIRI();

        if (iriOpt.isPresent() && pm.getDefaultPrefix() == null) {
            String ns = iriOpt.get().toString();
            if (!ns.endsWith("#") && !ns.endsWith("/")) {
                ns = ns + "#";
            }
            pm.setDefaultPrefix(ns);
        }

        return pm;
    }




    public Optional<String> resolveInputElapse(String inputElapse) {

        if (inputElapse == null || inputElapse.isEmpty() || inputElapse.equals("")) {
            return Optional.empty();   // CASE 1 → Not provided
        }

        // Pattern expected if user provides a literal datetime
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        try {
            LocalDateTime dateTime;

            if (inputElapse.equalsIgnoreCase("now()")) {
                dateTime = LocalDateTime.now();   // CASE 2 → now()
                //System.out.println("local datetime now" +String.valueOf(dateTime));

            } else {
                dateTime = LocalDateTime.parse(inputElapse, formatter);  // CASE 3 → provided
            }

            // Convert to epoch millis
            long epochMillis = dateTime.atZone(ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli();

            //System.out.println("elapse time" +String.valueOf(epochMillis));
            return Optional.of(String.valueOf(epochMillis));

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid inputElapse format. Expected:\n" +
                    " - yyyy-MM-dd'T'HH:mm:ss\n" +
                    " - now()", e);
        }
    }



	public void saveInferredABox(String outputPath) {
	    try {
	        TurtleDocumentFormat format = new TurtleDocumentFormat();

	        if (prefixManager != null) {
	            format.copyPrefixesFrom(prefixManager);
	        }

	        manager.saveOntology(
	            aboxOntology,
	            format,
	            IRI.create(new File(outputPath).toURI())
	        );

	        System.out.println("ABox saved to: " + outputPath);

	    } catch (OWLOntologyStorageException e) {
	        throw new RuntimeException("Failed to save ABox ontology", e);
	    }
	}

    /* =========================
       Accessors
       ========================= */
    public OWLOntology getTBox() {
        return tboxOntology;
    }

    public OWLOntology getABox() {
        return aboxOntology;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

	public OWLOntology getTboxOntology() {
		return tboxOntology;
	}

	public void setTboxOntology(OWLOntology tboxOntology) {
		this.tboxOntology = tboxOntology;
	}

	public OWLOntology getAboxOntology() {
		return aboxOntology;
	}

	public void setAboxOntology(OWLOntology aboxOntology) {
		this.aboxOntology = aboxOntology;
	}

	public OWLOntology getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(OWLOntology knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public OWLDataFactory getDataFactory() {
		return dataFactory;
	}

	public Optional<String> getElapse() {
		return elapse;
	}

	public void setElapse(Optional<String> elapse) {
		this.elapse = elapse;
	}

	public DefaultPrefixManager getPrefixManager() {
		return prefixManager;
	}

	public void setPrefixManager(DefaultPrefixManager prefixManager) {
		this.prefixManager = prefixManager;
	}
    

}
