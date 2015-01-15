package org.molgenis.data.elasticsearch;

import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchId;
import static org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils.toElasticsearchIds;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedCrudRepository;
import org.molgenis.data.Manageable;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

public abstract class AbstractElasticsearchRepository implements IndexedCrudRepository, Aggregateable, Manageable
{
	public static final String BASE_URL = "elasticsearch://";

	protected final SearchService elasticSearchService;

	public AbstractElasticsearchRepository(SearchService elasticSearchService)
	{
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	public abstract EntityMetaData getEntityMetaData();

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		@SuppressWarnings("resource")
		final AbstractElasticsearchRepository self = this;
		return new ConvertingIterable<E>(clazz, new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return self.iterator();
			}
		});
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + getName() + '/';
	}

	@Override
	public long count()
	{
		return elasticSearchService.count(getEntityMetaData());
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		return elasticSearchService.count(q, getEntityMetaData());
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return elasticSearchService.search(q, getEntityMetaData());
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(q));
	}

	@Override
	public Entity findOne(Query q)
	{
		Iterable<Entity> entities = elasticSearchService.search(q, getEntityMetaData());
		return !Iterables.isEmpty(entities) ? entities.iterator().next() : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		return elasticSearchService.get(id, getEntityMetaData());
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return elasticSearchService.get(ids, getEntityMetaData());
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, findAll(ids));
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		Entity entity = findOne(id);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity)).iterator().next() : null;
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		Entity entity = findOne(q);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity)).iterator().next() : null;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public void close() throws IOException
	{
		// noop
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return elasticSearchService.aggregate(aggregateQuery, getEntityMetaData());
	}

	@Override
	@Transactional
	public void add(Entity entity)
	{
		elasticSearchService.index(entity, getEntityMetaData(), IndexingMode.ADD);
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.ADD);
		elasticSearchService.refresh();
		return Iterables.size(entities); // TODO solve possible performance bottleneck
	}

	@Override
	public void flush()
	{
		elasticSearchService.flush();
	}

	@Override
	public void clearCache()
	{
		// noop
	}

	@Override
	@Transactional
	public void update(Entity entity)
	{
		elasticSearchService.index(entity, getEntityMetaData(), IndexingMode.UPDATE);
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void update(Iterable<? extends Entity> entities)
	{
		elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.UPDATE);
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		elasticSearchService.delete(entity, getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void delete(Iterable<? extends Entity> entities)
	{
		elasticSearchService.delete(entities, getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		elasticSearchService.deleteById(toElasticsearchId(id), getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void deleteById(Iterable<Object> ids)
	{
		elasticSearchService.deleteById(toElasticsearchIds(ids), getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		elasticSearchService.deleteDocumentsByType(getEntityMetaData().getName());
		elasticSearchService.refresh();
	}

	@Override
	public void drop()
	{
		elasticSearchService.delete(getEntityMetaData().getName());
	}
}
