package org.molgenis.omx.harmonization.ontologymatcher;

import java.util.List;

public class OntologyMatcherRequest
{
	private final Integer sourceDataSetId;
	private final List<Integer> selectedDataSetIds;

	public OntologyMatcherRequest(Integer sourceDataSetId, List<Integer> selectedDataSetIds)
	{
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
}