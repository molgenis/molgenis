package org.molgenis.omx.biobankconnect.ontologyservice;

import org.molgenis.search.SearchResult;

public class OntologyServiceBatchResponse
{
	private final String term;
	private final SearchResult searchResult;

	public OntologyServiceBatchResponse(String term, SearchResult searchResult)
	{
		this.term = term;
		this.searchResult = searchResult;
	}

	public String getTerm()
	{
		return term;
	}

	public SearchResult getSearchResult()
	{
		return searchResult;
	}
}