package org.molgenis.ontology.repository;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;

public abstract class AbstractOntologyQueryRepository extends AbstractOntologyRepository
{
	protected final SearchService searchService;

	public AbstractOntologyQueryRepository(String entityName, SearchService searchService)
	{
		super(entityName);
		if (searchService == null) throw new IllegalArgumentException("SearchService is null!");
		this.searchService = searchService;
	}

	public Entity findOneInternal(Query query)
	{
		for (Entity entity : searchService.search(query, getEntityMetaData()))
		{
			return entity;
		}

		return null;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

}
