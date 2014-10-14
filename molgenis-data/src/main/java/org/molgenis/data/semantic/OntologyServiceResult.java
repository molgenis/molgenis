package org.molgenis.data.semantic;

import java.util.Map;

/**
 * This function is used to parse the results from OntologyService
 * 
 * @author chaopang
 * 
 */
public class OntologyServiceResult
{
	private String message;
	private Map<String, Object> inputData;
	private Iterable<OntologyTerm> ontologyTerms;
	private long totalHitCount;

	public OntologyServiceResult(String message)
	{
		this.message = message;
	}

	public OntologyServiceResult(Map<String, Object> inputData, Iterable<OntologyTerm> ontologyTerms, long totalHitCount)
	{
		this.inputData = inputData;
		this.ontologyTerms = ontologyTerms;
		this.totalHitCount = totalHitCount;
	}

	public Map<String, Object> getInputData()
	{
		return inputData;
	}

	public Iterable<OntologyTerm> getOntologyTerms()
	{
		return ontologyTerms;
	}

	public long getTotalHitCount()
	{
		return totalHitCount;
	}

	public String getMessage()
	{
		return message;
	}
}