package org.molgenis.omx.biobankconnect.ontologyannotator;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UpdateIndexRequest
{
	private final Integer dataSetId;
	private final String documentType;
	private final List<Integer> matchedDataSetIds;
	private final List<String> documentIds;
	private final String updateScript;

	public UpdateIndexRequest(Integer dataSetId, String documentType, List<Integer> matchedDataSetIds,
			List<String> documentIds, String updateScript)
	{
		this.dataSetId = dataSetId;
		this.documentType = documentType;
		this.documentIds = documentIds;
		this.updateScript = updateScript;
		this.matchedDataSetIds = matchedDataSetIds;
	}

	public Integer getDataSetId()
	{
		return this.dataSetId;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public List<String> getDocumentIds()
	{
		return documentIds;
	}

	public String getUpdateScript()
	{
		return updateScript;
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this);
	}

	public List<Integer> getMatchedDataSetIds()
	{
		return matchedDataSetIds;
	}
}