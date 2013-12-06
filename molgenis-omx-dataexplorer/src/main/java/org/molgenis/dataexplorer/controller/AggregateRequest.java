package org.molgenis.dataexplorer.controller;

import org.molgenis.search.SearchRequest;

public class AggregateRequest
{

	private final String documentType;
	private final Integer featureId;
	private final SearchRequest searchRequest;
	private final String dataType;

	public AggregateRequest(String documentType, Integer featureId, SearchRequest searchRequest, String dataType)
	{
		this.documentType = documentType;
		this.featureId = featureId;
		this.searchRequest = searchRequest;
		this.dataType = dataType;
	}

	public String getDocumentType()
	{
		return documentType;
	}

	public Integer getFeatureId()
	{
		return featureId;
	}

	public SearchRequest getSearchRequest()
	{
		return searchRequest;
	}

	public String getDataType()
	{
		return dataType;
	}
}
