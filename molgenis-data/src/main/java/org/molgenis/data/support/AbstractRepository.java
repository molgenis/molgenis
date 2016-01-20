package org.molgenis.data.support;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.uniqueIndex;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * Base class for repositories. Subclasses can override supported methods
 */
public abstract class AbstractRepository implements Repository
{
	private static final int FIND_ALL_BATCH_SIZE = 1000;

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private String name;

	@Override
	public String getName()
	{
		if (name == null) name = getEntityMetaData().getName();
		return name;
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public void close() throws IOException
	{
	}

	@Override
	public long count(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(Query q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(Object id)
	{
		return findOne(id, null);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	@Transactional(readOnly = true)
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		return findAll(ids, null);
	}

	@Override
	@Transactional(readOnly = true)
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		Iterator<List<Object>> batches = Iterators.partition(ids.iterator(), FIND_ALL_BATCH_SIZE);
		Iterable<List<Object>> iterable = () -> batches;
		return StreamSupport.stream(iterable.spliterator(), false).flatMap(batch -> {
			return StreamSupport.stream(findAllBatched(batch, fetch).spliterator(), false);
		});
	}

	private Iterable<Entity> findAllBatched(List<Object> ids, Fetch fetch)
	{
		Query inQuery = new QueryImpl().in(getEntityMetaData().getIdAttribute().getName(), Sets.newHashSet(ids))
				.fetch(fetch);
		Map<Object, Entity> indexedEntities = uniqueIndex(findAll(inQuery).iterator(), Entity::getIdValue);
		return filter(transform(ids, id -> lookup(indexedEntities, id)), notNull());
	}

	private Entity lookup(Map<Object, Entity> index, Object id)
	{
		Entity result = index.get(id);
		if (result == null)
		{
			LOG.warn("Lookup: Couldn't find {} for id {}.", getName(), id);
		}
		return result;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(Stream<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(Stream<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(Stream<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer add(Stream<? extends Entity> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void clearCache()
	{
	}

	// Implement in child class if repository has capability MANAGABLE
	@Override
	public void create()
	{
		throw new UnsupportedOperationException();
	}

	// Implement in child class if repository has capability MANAGABLE
	@Override
	public void drop()
	{
		throw new UnsupportedOperationException();
	}

	// Implement in child class if repository has capability INDEXABLE
	@Override
	public void rebuildIndex()
	{
		throw new UnsupportedOperationException();
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
}
