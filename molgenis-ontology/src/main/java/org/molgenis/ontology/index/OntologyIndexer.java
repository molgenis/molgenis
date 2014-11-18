package org.molgenis.ontology.index;

import org.molgenis.ontology.utils.OntologyLoader;

public interface OntologyIndexer
{
	void index(OntologyLoader model);

	void removeOntology(String ontologyURI);

	boolean isIndexingRunning();

	boolean isCorrectOntology();

	String getOntologyUri();
}
