package org.molgenis.data.elasticsearch;

import static org.molgenis.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchIds;
import static org.molgenis.elasticsearch.util.ElasticsearchEntityUtils.toEntityIds;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Countable;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

/**
 * Repository that wraps an existing repository and retrieves count/aggregate information from a Elasticsearch index
 */
public class ElasticsearchRepositoryDecorator implements CrudRepository, Aggregateable
{
	public static final String BASE_URL = "elasticsearch://";

	private final Repository repository;
	private final ElasticSearchService elasticSearchService;

	public ElasticsearchRepositoryDecorator(Repository repository, ElasticSearchService elasticSearchService)
	{
		if (repository == null) throw new IllegalArgumentException("repository is null");
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.repository = repository;
		this.elasticSearchService = elasticSearchService;

		// persist entity meta data if entity meta data does not exist in elasticsearch index
		if (!elasticSearchService.hasMapping(repository))
		{
			try
			{
				elasticSearchService.createMappings(repository, false);
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(e);
			}
		}
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
		if (!(repository instanceof Countable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Countable");
		}
		return ((Countable) repository).count();
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
		return elasticSearchService.count(q, getEntityMetaData());
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}

		Iterable<String> ids = elasticSearchService.search(q, getEntityMetaData());
		System.out.println(Lists.newArrayList(ids));
		return !Iterables.isEmpty(ids) ? ((Queryable) repository).findAll(toEntityIds(ids)) : Collections
				.<Entity> emptyList();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		Iterable<String> ids = elasticSearchService.search(q, getEntityMetaData());
		return !Iterables.isEmpty(ids) ? ((Queryable) repository).findAll(toEntityIds(ids), clazz) : Collections
				.<E> emptyList();
	}

	@Override
	public Entity findOne(Query q)
	{
		if (!(repository instanceof Queryable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Queryable");
		}
		Iterable<String> ids = elasticSearchService.search(q, getEntityMetaData());
		return !Iterables.isEmpty(ids) ? ((Queryable) repository).findOne(toEntityIds(ids).iterator().next()) : null;
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
		Iterable<String> ids = elasticSearchService.search(q, getEntityMetaData());
		return !Iterables.isEmpty(ids) ? ((Queryable) repository).findOne(toEntityIds(ids).iterator().next(), clazz) : null;
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
	@Transactional
	public void add(Entity entity)
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Writable");
		}
		((Writable) repository).add(entity);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.index(entity, entityMetaData, IndexingMode.ADD);
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Writable");
		}
		Integer count = ((Writable) repository).add(entities);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.index(entities, entityMetaData, IndexingMode.ADD);
		return count;
	}

	@Override
	public void flush()
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Writable) repository).flush();
	}

	@Override
	public void clearCache()
	{
		if (!(repository instanceof Writable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Writable) repository).clearCache();
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entity);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.index(entity, entityMetaData, IndexingMode.UPDATE);
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entities);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.index(entities, entityMetaData, IndexingMode.UPDATE);
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).delete(entity);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.delete(entity, entityMetaData);
	}

	@Override
	@Transactional
	public void delete(Iterable<? extends Entity> entities)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).delete(entities);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.delete(entities, entityMetaData);
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteById(id);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.deleteById(toElasticsearchId(id), entityMetaData);
	}

	@Override
	@Transactional
	public void deleteById(Iterable<Object> ids)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteById(ids);

		EntityMetaData entityMetaData = getEntityMetaData();
		elasticSearchService.deleteById(toElasticsearchIds(ids), entityMetaData);
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).deleteAll();

		elasticSearchService.delete(getEntityMetaData());
	}

	@Override
	@Transactional
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		if (!(repository instanceof Updateable))
		{
			throw new MolgenisDataAccessException("Repository '" + repository.getName() + "' is not Updateable");
		}
		((Updateable) repository).update(entities, dbAction, keyName);

		EntityMetaData entityMetaData = getEntityMetaData();
		switch (dbAction)
		{
			case ADD:
			case ADD_IGNORE_EXISTING:
			case ADD_UPDATE_EXISTING:
				elasticSearchService.index(entities, entityMetaData, IndexingMode.ADD);
				break;
			case UPDATE:
			case UPDATE_IGNORE_MISSING:
				elasticSearchService.index(entities, entityMetaData, IndexingMode.UPDATE);
				break;
			case REMOVE:
			case REMOVE_IGNORE_MISSING:
				elasticSearchService.delete(entities, entityMetaData);
				break;
			default:
				throw new RuntimeException("Unknown DatabaseAction [" + dbAction + "]");
		}
	}

	public Repository getRepository()
	{
		return repository;
	}
}
