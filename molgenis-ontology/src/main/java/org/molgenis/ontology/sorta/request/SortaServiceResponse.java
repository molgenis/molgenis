package org.molgenis.ontology.sorta.request;

import org.molgenis.data.Entity;
import org.molgenis.ontology.utils.SortaServiceUtil;

import java.util.List;
import java.util.Map;

/**
 * This function is used to parse the results from OntologyService
 *
 * @author chaopang
 */
public class SortaServiceResponse
{
	private String message;
	private Map<String, Object> inputData;
	private List<Map<String, Object>> ontologyTerms;
	private long totalHitCount;

	public SortaServiceResponse(String message)
	{
		this.message = message;
	}

	public SortaServiceResponse(Entity inputData, Iterable<? extends Entity> ontologyTerms)
	{
		this(SortaServiceUtil.getEntityAsMap(inputData), SortaServiceUtil.getEntityAsMap(ontologyTerms));
	}

	public SortaServiceResponse(Map<String, Object> inputData, List<Map<String, Object>> ontologyTerms)
	{
		this.inputData = inputData;
		this.ontologyTerms = ontologyTerms;
		this.totalHitCount = ontologyTerms.size();
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