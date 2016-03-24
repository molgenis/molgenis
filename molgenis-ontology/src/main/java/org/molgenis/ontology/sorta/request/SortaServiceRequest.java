package org.molgenis.ontology.sorta.request;

import org.molgenis.data.rest.EntityPager;

public class SortaServiceRequest
{
	private final String sortaJobExecutionId;
	private final String ontologyIri;
	private final String filterQuery;
	private final boolean matched;
	private final EntityPager entityPager;

	public SortaServiceRequest(String sortaJobExecutionId, String ontologyIri, String filterQuery, boolean matched,
			EntityPager entityPager)
	{
		this.sortaJobExecutionId = sortaJobExecutionId;
		this.ontologyIri = ontologyIri;
		this.matched = matched;
		this.entityPager = entityPager;
		this.filterQuery = filterQuery;
	}

	public String getSortaJobExecutionId()
	{
		return sortaJobExecutionId;
	}

	public boolean isMatched()
	{
		return matched;
	}

	public EntityPager getEntityPager()
	{
		return entityPager;
	}

	public String getOntologyIri()
	{
		return ontologyIri;
	}

	public String getFilterQuery()
	{
		return filterQuery;
	}
}