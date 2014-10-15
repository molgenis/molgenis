package org.molgenis.data.semantic;

import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;

/**
 * This function is used to parse the results from OntologyService
 * 
 * @author chaopang
 * 
 */
public abstract class OntologyServiceResult
{
	private String message;
	private Map<String, Object> inputData;
	protected List<Map<String, Object>> ontologyTerms;
	private long totalHitCount;

	public OntologyServiceResult(String message)
	{
		this.message = message;
	}

	public OntologyServiceResult(Map<String, Object> inputData, Iterable<Entity> ontologyTerms, long totalHitCount)
	{
		this.inputData = inputData;
		this.totalHitCount = totalHitCount;
	}

	public Map<String, Object> getInputData()
	{
		return inputData;
	}

	public long getTotalHitCount()
	{
		return totalHitCount;
	}

	public String getMessage()
	{
		return message;
	}

	public List<Map<String, Object>> getOntologyTerms()
	{
		return ontologyTerms;
	}
}