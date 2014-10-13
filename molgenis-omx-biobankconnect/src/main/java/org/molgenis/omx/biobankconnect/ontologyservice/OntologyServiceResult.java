package org.molgenis.omx.biobankconnect.ontologyservice;

import java.util.List;
import java.util.Map;

import org.molgenis.data.elasticsearch.util.Hit;

public class OntologyServiceResult
{
	private Map<String, Object> inputData;
	private List<Hit> searchHits;
	private long totalHitCount;
	private String errorMessage = null;

	public OntologyServiceResult(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public OntologyServiceResult(Map<String, Object> inputData, List<Hit> searchHits, long totalHitCount)
	{
		this.inputData = inputData;
		this.searchHits = searchHits;
		this.totalHitCount = totalHitCount;
	}

	public Map<String, Object> getInputData()
	{
		return inputData;
	}

	public List<Hit> getSearchHits()
	{
		return searchHits;
	}

	public long getTotalHitCount()
	{
		return totalHitCount;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
}
