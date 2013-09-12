package org.molgenis.omx.harmonization.ontologyannotator;

public interface OntologyAnnotator
{
	void annotate(Integer protocolId);

	float finishedPercentage();

	boolean isRunning();
}
