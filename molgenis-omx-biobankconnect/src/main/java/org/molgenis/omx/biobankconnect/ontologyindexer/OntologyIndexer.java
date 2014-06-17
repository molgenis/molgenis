package org.molgenis.omx.biobankconnect.ontologyindexer;

import org.molgenis.omx.biobankconnect.utils.OntologyLoader;

public interface OntologyIndexer
{
	void index(OntologyLoader model);

	void removeOntology(String ontologyURI);

	boolean isIndexingRunning();

	boolean isCorrectOntology();

	String getOntologyUri();
}
