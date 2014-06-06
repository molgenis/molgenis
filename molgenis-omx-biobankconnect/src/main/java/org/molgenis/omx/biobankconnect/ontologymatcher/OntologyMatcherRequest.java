package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;

public class OntologyMatcherRequest
{
	private final Integer sourceDataSetId;
	private final Integer featureId;
	private final List<Integer> selectedDataSetIds;
	private final List<Integer> mappedFeatureIds;
	private final String algorithmScript;

	public OntologyMatcherRequest(Integer sourceDataSetId, Integer featureId, List<Integer> selectedDataSetIds,
			List<Integer> mappedFeatureIds, String algorithmScript)
	{
		this.featureId = featureId;
		this.sourceDataSetId = sourceDataSetId;
		this.selectedDataSetIds = selectedDataSetIds;
		this.mappedFeatureIds = mappedFeatureIds;
		this.algorithmScript = algorithmScript;
	}

	public Integer getTargetDataSetId()
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

	public String getAlgorithmScript()
	{
		return algorithmScript;
	}

	public List<Integer> getMappedFeatureIds()
	{
		return mappedFeatureIds;
	}
}