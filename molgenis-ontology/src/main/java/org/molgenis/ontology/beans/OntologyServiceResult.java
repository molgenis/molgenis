package org.molgenis.ontology.beans;

import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.ontology.utils.OntologyServiceUtil;

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
	private List<Map<String, Object>> ontologyTerms;
	private long totalHitCount;

	public OntologyServiceResult(String message)
	{
		this.message = message;
	}

	public OntologyServiceResult(Map<String, Object> inputData, Iterable<? extends Entity> ontologyTerms,
			long totalHitCount)
	{
		this(inputData, OntologyServiceUtil.getEntityAsMap(ontologyTerms), totalHitCount);
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