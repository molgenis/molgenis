package org.molgenis.omx.biobankconnect.ontologyannotator;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UpdateIndexRequest
{
	private Integer dataSetId;
	private String documentType;
	private List<String> documentIds;
	private String updateScript;

	public UpdateIndexRequest(Integer dataSetId, String documentType, List<String> documentIds,
			String updateScript)
	{
		this.dataSetId = dataSetId;
		this.documentType = documentType;
		this.documentIds = documentIds;
		this.updateScript = updateScript;
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
}