package org.molgenis.omx.harmonization.plugin;

public interface OntologyAnnotator
{
	void annotate(Integer protocolId);

	float finishedPercentage();
}
