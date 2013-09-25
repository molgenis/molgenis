package org.molgenis.omx.harmonization.ontologyindexer;

import java.io.File;

import org.molgenis.framework.tupletable.TableException;

public interface OntologyIndexer
{
	void index(String ontologyName, File file) throws TableException;

	void removeOntology(String ontologyURI);

	boolean isIndexingRunning();

	boolean isCorrectOntology();

	String getOntologyUri();
}
