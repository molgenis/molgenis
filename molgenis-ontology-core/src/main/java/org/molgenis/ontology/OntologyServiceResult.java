package org.molgenis.ontology;

import java.util.List;
import java.util.Map;

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
	private List<Map<String, Object>> ontologyTerms;
	private long totalHitCount;

	public OntologyServiceResult(String message)
	{
		this.message = message;
	}

	public OntologyServiceResult(Map<String, Object> inputData, List<Map<String, Object>> ontologyTerms,
			long totalHitCount)
	{
		this.inputData = inputData;
		this.totalHitCount = totalHitCount;
		this.ontologyTerms = ontologyTerms;
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