package org.molgenis.ontology.request;

import org.molgenis.data.rest.EntityPager;

public class OntologyServiceRequest
{
	private final String entityName;
	private final String ontologyIri;
	private final String filterQuery;
	private final boolean matched;
	private final EntityPager entityPager;

	public OntologyServiceRequest(String entityName, String ontologyIri, String filterQuery, boolean matched,
			EntityPager entityPager)
	{
		this.entityName = entityName;
		this.ontologyIri = ontologyIri;
		this.matched = matched;
		this.entityPager = entityPager;
		this.filterQuery = filterQuery;
	}

	public String getEntityName()
	{
		return entityName;
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