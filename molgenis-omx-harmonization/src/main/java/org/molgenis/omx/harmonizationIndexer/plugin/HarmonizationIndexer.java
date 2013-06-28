package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.File;

import org.molgenis.framework.tupletable.TableException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public interface HarmonizationIndexer
{
	void index(File file) throws TableException, OWLOntologyCreationException;

	boolean isIndexingRunning();

	String getOntologyUri();
}
