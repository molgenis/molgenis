package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.Iterator;

import org.elasticsearch.common.primitives.Ints;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.IndexedRepository;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.ElasticSearchService.IndexingMode;
import org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

public abstract class AbstractElasticsearchRepository implements IndexedRepository
{
	protected final SearchService elasticSearchService;

	public AbstractElasticsearchRepository(SearchService elasticSearchService)
	{
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	public abstract EntityMetaData getEntityMetaData();

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
	public void add(Entity entity)
	{
		elasticSearchService.index(entity, getEntityMetaData(), IndexingMode.ADD);
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public Integer add(Iterable<? extends Entity> entities)
	{
		long nrIndexedEntities = elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.ADD);
		elasticSearchService.refresh();
		return Ints.checkedCast(nrIndexedEntities);
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
		elasticSearchService.deleteById(ElasticsearchEntityUtils.toElasticsearchId(id), getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void deleteById(Iterable<Object> ids)
	{
		elasticSearchService.deleteById(ElasticsearchEntityUtils.toElasticsearchIds(ids), getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		elasticSearchService.delete(getEntityMetaData().getName());
		elasticSearchService.refresh();
	}

	@Override
	public void create()
	{
		elasticSearchService.createMappings(getEntityMetaData());

	}

	@Override
	public void drop()
	{
		elasticSearchService.delete(getEntityMetaData().getName());
	}
}
