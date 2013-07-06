package org.molgenis.omx.harmonization.plugin;

import org.molgenis.framework.db.DatabaseException;

public interface OntologyAnnotator
{
	void annotate(Integer protocolId) throws DatabaseException;

	float finishedPercentage();
}
