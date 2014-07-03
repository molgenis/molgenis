package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;

/**
 * Repository that wraps an existing repository and retrieves count/aggregate information from a Elasticsearch index
 */
public class ElasticSearchRepository implements CrudRepository, Aggregateable
{
	public static final String BASE_URL = "elasticsearch://";

	private final ElasticSearchService elasticSearchService;
	private final Repository repository;

	public ElasticSearchRepository(ElasticSearchService elasticSearchService, Repository repository)
	{
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		if (repository == null) throw new IllegalArgumentException("repository is null");
		this.elasticSearchService = elasticSearchService;
		this.repository = repository;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return repository.getEntityMetaData();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return repository.iterator(clazz);
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + getName() + '/';
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findAll(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findAll(q, clazz);
	}

	@Override
	public Entity findOne(Query q)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findAll(ids);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		return ((Queryable) repository).findOne(q, clazz);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return repository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		// noop
	}

	@Override
	public String getName()
	{
		return repository.getName();
	}

	@Override
	public AggregateResult aggregate(AttributeMetaData xAttr, AttributeMetaData yAttr, Query q)
	{
		SearchRequest searchRequest = new SearchRequest(getName(), q, Collections.<String> emptyList(), xAttr, yAttr);
		SearchResult searchResults = elasticSearchService.search(searchRequest);
		return searchResults.getAggregate();
	}

	@Override
	public void add(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).add(entity);

		// TODO update index
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		Integer count = ((Updateable) repository).add(entities);

		// TODO update index
		return count;
	}

	@Override
	public void flush()
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).flush();
	}

	@Override
	public void clearCache()
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).clearCache();
	}

	@Override
	public void update(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entity);

		// TODO update index
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(records);

		// TODO update index
	}

	@Override
	public void delete(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).delete(entity);

		// TODO update index
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).delete(entities);

		// TODO update index
	}

	@Override
	public void deleteById(Object id)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteById(id);

		// TODO update index
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteById(ids);

		// TODO update index
	}

	@Override
	public void deleteAll()
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteAll();

		// TODO update index
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entities, dbAction, keyName);

		// TODO update index
	}

	public Repository getRepository()
	{
		return repository;
	}
}
