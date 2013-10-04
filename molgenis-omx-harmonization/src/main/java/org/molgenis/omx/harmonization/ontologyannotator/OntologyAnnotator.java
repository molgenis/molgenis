package org.molgenis.omx.harmonization.ontologyannotator;

import java.util.List;

public interface OntologyAnnotator
{
	void annotate(Integer protocolId, List<String> documentTypes);

	void removeAnnotations(Integer protocolId, List<String> documentTypes);

	float finishedPercentage();

	boolean isRunning();

	boolean isComplete();

	void initComplete();

	void updateIndex(UpdateIndexRequest request);
}
