package org.molgenis.ontology.sorta.request;

import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.ontology.sorta.bean.SortaHit;
import org.molgenis.ontology.utils.SortaServiceUtil;

/**
 * This function is used to parse the results from OntologyService
 * 
 * @author chaopang
 * 
 */
public class SortaServiceResponse
{
	private String message;
	private Map<String, Object> inputData;
	private List<SortaHit> sortaHits;
	private long totalHitCount;

	public SortaServiceResponse(String message)
	{
		this.message = message;
	}

	public SortaServiceResponse(Entity inputData, List<SortaHit> sortaHits)
	{
		this.inputData = SortaServiceUtil.getEntityAsMap(inputData);
		this.sortaHits = sortaHits;
		this.totalHitCount = sortaHits.size();
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

	public List<SortaHit> getSortaHits()
	{
		return sortaHits;
	}
}