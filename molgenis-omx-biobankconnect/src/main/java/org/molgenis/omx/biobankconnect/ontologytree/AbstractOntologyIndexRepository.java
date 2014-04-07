package org.molgenis.omx.biobankconnect.ontologytree;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractOntologyIndexRepository implements Repository, Queryable
{
	protected DefaultEntityMetaData entityMetaData = null;
	protected final SearchService searchService;
	protected final Map<Integer, String> identifierMap = new HashMap<Integer, String>();

	@Autowired
	public AbstractOntologyIndexRepository(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public void close() throws IOException
	{
		identifierMap.clear();
	}

	@Override
	public Entity findOne(Query q)
	{
		for (Hit hit : searchService.search(new SearchRequest(null, q, null)).getSearchHits())
		{
			String id = hit.getId();
			int hashCode = id.hashCode();
			if (!identifierMap.containsKey(hashCode))
			{
				identifierMap.put(hashCode, id);
			}
			return new OntologyIndexEntity(hit, getEntityMetaData(), identifierMap, searchService);
		}
		return null;
	}

	@Override
	public Entity findOne(Integer id)
	{
		if (identifierMap.containsKey(id))
		{
			Hit hit = searchService.searchById(null, identifierMap.get(id));
			return new OntologyIndexEntity(hit, getEntityMetaData(), identifierMap, searchService);
		}
		return null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<? extends Entity> getEntityClass()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}
}
