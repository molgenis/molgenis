package org.molgenis.omx.biobankconnect.ontologyindexer;

import java.io.File;

public interface OntologyIndexer
{
	void index(String ontologyName, File file);

	void removeOntology(String ontologyURI);

	boolean isIndexingRunning();

	boolean isCorrectOntology();

	String getOntologyUri();
}
