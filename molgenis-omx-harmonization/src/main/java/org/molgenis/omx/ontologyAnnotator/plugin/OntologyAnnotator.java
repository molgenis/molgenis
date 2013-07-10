package org.molgenis.omx.ontologyAnnotator.plugin;

import org.molgenis.framework.db.DatabaseException;

public interface OntologyAnnotator
{
	void annotate(Integer protocolId) throws DatabaseException;

	float finishedPercentage();
}
