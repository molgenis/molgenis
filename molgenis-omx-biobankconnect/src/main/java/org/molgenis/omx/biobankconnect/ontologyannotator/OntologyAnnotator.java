package org.molgenis.omx.biobankconnect.ontologyannotator;

import java.util.List;

public interface OntologyAnnotator
{
	void annotate(Integer protocolId, List<String> documentTypes);

	void removeAnnotations(Integer dataSetId);

	float finishedPercentage();

	boolean isRunning();

	boolean isComplete();

	void initComplete();

	void updateIndex(UpdateIndexRequest request);
}
