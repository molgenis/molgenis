package org.molgenis.ontology.repository;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOntologyQueryRepository extends AbstractOntologyRepository
{
	@Autowired
	public AbstractOntologyQueryRepository(String entityName, SearchService searchService)
	{
		super(entityName, searchService);
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
