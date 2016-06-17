package org.molgenis.data.elasticsearch;

import com.google.common.collect.Sets;
import org.elasticsearch.common.primitives.Ints;
import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.elasticsearch.ElasticsearchService.IndexingMode;
import org.molgenis.data.elasticsearch.util.ElasticsearchEntityUtils;
import org.molgenis.data.support.QueryImpl;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.*;

public abstract class AbstractElasticsearchRepository implements Repository<Entity>
{
	protected final SearchService elasticSearchService;

	public AbstractElasticsearchRepository(SearchService elasticSearchService)
	{
		this.elasticSearchService = requireNonNull(elasticSearchService, "elasticSearchService is null");
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(AGGREGATEABLE, QUERYABLE, WRITABLE, INDEXABLE, MANAGABLE);
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return EnumSet.allOf(Operator.class);
	}

	@Override
	public long count()
	{
		return elasticSearchService.count(getEntityMetaData());
	}

	@Override
	public Query<Entity> query()
	{
		return new QueryImpl<>(this);
	}

	@Override
	public long count(Query<Entity> q)
	{
		return elasticSearchService.count(q, getEntityMetaData());
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return elasticSearchService.searchAsStream(q, getEntityMetaData());
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		Iterable<Entity> entities = elasticSearchService.search(q, getEntityMetaData());
		Iterator<Entity> it = entities.iterator();
		return it.hasNext() ? it.next() : null;
	}

	@Override
	public Entity findOneById(Object id)
	{
		return elasticSearchService.get(id, getEntityMetaData());
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
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
		Query<Entity> q = new QueryImpl<>();
		return elasticSearchService.searchAsStream(q, getEntityMetaData()).iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		throw new UnsupportedOperationException("Not supposed to be called.");
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
	public Integer add(Stream<Entity> entities)
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
	public void update(Entity entity)
	{
		elasticSearchService.index(entity, getEntityMetaData(), IndexingMode.UPDATE);
		elasticSearchService.refresh();
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		elasticSearchService.index(entities, getEntityMetaData(), IndexingMode.UPDATE);
		elasticSearchService.refresh();
	}

	@Override
	public void delete(Entity entity)
	{
		elasticSearchService.delete(entity, getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		elasticSearchService.delete(entities, getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	public void deleteById(Object id)
	{
		elasticSearchService.deleteById(ElasticsearchEntityUtils.toElasticsearchId(id), getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		elasticSearchService.deleteById(ElasticsearchEntityUtils.toElasticsearchIds(ids), getEntityMetaData());
		elasticSearchService.refresh();
	}

	@Override
	public void deleteAll()
	{
		elasticSearchService.delete(getEntityMetaData().getName());
		createMappings();
		elasticSearchService.refresh();
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
