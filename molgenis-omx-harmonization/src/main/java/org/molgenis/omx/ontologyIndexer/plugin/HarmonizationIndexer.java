package org.molgenis.omx.ontologyIndexer.plugin;

import java.io.File;

import org.molgenis.framework.tupletable.TableException;

public interface HarmonizationIndexer
{
	void index(File file) throws TableException;

	boolean isIndexingRunning();

	boolean isCorrectOntology();

	String getOntologyUri();
}
