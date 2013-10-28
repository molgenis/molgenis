package org.molgenis.dataexplorer.controller;

import org.molgenis.search.SearchRequest;

public class AggregateRequest {

	private final String documentType;
	private final Integer featureId;
	private final SearchRequest searchRequest;

	public AggregateRequest( String documentType,Integer featureId, SearchRequest searchRequest)
	{
		this.documentType = documentType;
		this.featureId = featureId;
		this.searchRequest = searchRequest;
	}

	public String getDocumentType() {
		return documentType;
	}

	public Integer getFeatureId() {
		return featureId;
	}

	public SearchRequest getSearchRequest() {
		return searchRequest;
	}
}
