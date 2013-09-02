package org.molgenis.omx.ontologyAnnotator.plugin;


public interface OntologyAnnotator
{
	void annotate(Integer protocolId);

	float finishedPercentage();
}
