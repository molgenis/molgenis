package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;

public class OntologyMatcherRequest
{
	private final Integer sourceDataSetId;
	private final Integer featureId;
	private final List<Integer> selectedDataSetIds;

	public OntologyMatcherRequest(Integer sourceDataSetId, Integer featureId, List<Integer> selectedDataSetIds)
	{
		this.featureId = featureId;
		this.sourceDataSetId = sourceDataSetId;
		this.selectedDataSetIds = selectedDataSetIds;
	}

	public Integer getSourceDataSetId()
	{
		return sourceDataSetId;
	}

	public List<Integer> getSelectedDataSetIds()
	{
		return selectedDataSetIds;
	}

	public Integer getFeatureId()
	{
		return featureId;
	}
}