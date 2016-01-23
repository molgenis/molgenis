package org.molgenis.data.elasticsearch;

import static org.molgenis.data.RepositoryCapability.AGGREGATEABLE;
import static org.molgenis.data.RepositoryCapability.INDEXABLE;
import static org.molgenis.data.RepositoryCapability.MANAGABLE;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.elasticsearch.common.primitives.Ints;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils;
import org.molgenis.data.support.QueryImpl;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

public abstract class AbstractElasticsearchRepository implements Repository
{
	protected final SearchService elasticSearchService;

	public AbstractElasticsearchRepository(SearchService elasticSearchService)
	{
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchService is null");
		this.elasticSearchService = elasticSearchService;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(AGGREGATEABLE, QUERYABLE, WRITABLE, INDEXABLE, MANAGABLE);
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
	public Stream<Entity> findAll(Query q)
	{
		return elasticSearchService.searchAsStream(q, getEntityMetaData());
	}

	@Override
	public Entity findOne(Query q)
	{
		Iterable<Entity> entities = elasticSearchService.search(q, getEntityMetaData());
		Iterator<Entity> it = entities.iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public Entity findOne(Object id)
	{
		return elasticSearchService.get(id, getEntityMetaData());
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return elasticSearchService.get(id, getEntityMetaData(), fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return elasticSearchService.get(ids, getEntityMetaData());
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return elasticSearchService.get(ids, getEntityMetaData(), fetch);
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
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public Integer add(Stream<? extends Entity> entities)
	{
		long nrIndexedEntities = elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.ADD);
		elasticSearchService.refresh(getEntityMetaData());
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
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public void update(Stream<? extends Entity> entities)
	{
		elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.UPDATE);
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public void delete(Entity entity)
	{
		elasticSearchService.delete(entity, getEntityMetaData());
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public void delete(Stream<? extends Entity> entities)
	{
		elasticSearchService.delete(entities, getEntityMetaData());
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public void deleteById(Object id)
	{
		elasticSearchService.deleteById(ElasticsearchEntityUtils.toElasticsearchId(id), getEntityMetaData());
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public void deleteById(Stream<Object> ids)
	{
		elasticSearchService.deleteById(ElasticsearchEntityUtils.toElasticsearchIds(ids), getEntityMetaData());
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	@Transactional
	public void deleteAll()
	{
		elasticSearchService.delete(getEntityMetaData().getName());
		createMappings();
		elasticSearchService.refresh(getEntityMetaData());
	}

	@Override
	public void create()
	{
		createMappings();
	}

	@Override
	public void drop()
	{
		elasticSearchService.delete(getEntityMetaData().getName());
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		throw new UnsupportedOperationException();
	}

	private void createMappings()
	{
		elasticSearchService.createMappings(getEntityMetaData());
	}
}
