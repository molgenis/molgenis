package org.molgenis.data.cache.L1;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.collect.Iterators;
import org.molgenis.data.*;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Iterators.partition;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

/**
 * Adds, removes and retrieves entities from the {@link L1Cache} when a {@link Repository} is {@link RepositoryCapability#CACHEABLE}.
 * Delegates to the underlying repository when an action is not supported by the cache or when the cache doesn't contain
 * the needed entity.
 */
public class L1CacheRepositoryDecorator implements Repository<Entity>
{
	private static final int ID_BATCH_SIZE = 1000;

	private final Repository<Entity> decoratedRepository;
	private final L1Cache l1Cache;

	private final boolean cacheable;

	public L1CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L1Cache l1Cache)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l1Cache = requireNonNull(l1Cache);
		this.cacheable = decoratedRepository.getCapabilities().containsAll(newArrayList(CACHEABLE, WRITABLE));
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		if (cacheable)
		{
			String entityName = getName();
			entities = entities.filter(entity -> {
				l1Cache.put(entityName, entity);
				return true;
			});
		}
		return decoratedRepository.add(entities);
	}

	@Override
	public void add(Entity entity)
	{
		if (cacheable)
		{
			l1Cache.put(getName(), entity);
		}
		decoratedRepository.add(entity);
	}

	@Override
	public Entity findOneById(Object id)
	{
		if (cacheable)
		{
			Entity entity = l1Cache.get(getName(), id, getEntityMetaData());
			if (entity != null) return entity;
		}
		return decoratedRepository.findOneById(id);
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		return decoratedRepository.findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		if (cacheable)
		{
			Iterator<List<Object>> idBatches = partition(ids.iterator(), ID_BATCH_SIZE);
			Iterator<List<Entity>> entityBatches = Iterators.transform(idBatches, this::findAllBatch);
			return stream(spliteratorUnknownSize(entityBatches, SORTED | ORDERED), false).flatMap(List::stream)
					.filter(e -> e != null);
		}
		return decoratedRepository.findAll(ids);
	}

	@Override
	public void update(Entity entity)
	{
		if (cacheable) l1Cache.put(getName(), entity);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		if (cacheable)
		{
			entities = entities.filter(entity -> {
				l1Cache.put(getName(), entity);
				return true;
			});
		}
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		if (cacheable) l1Cache.evict(getName(), entity.getIdValue());
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		if (cacheable)
		{
			entities = entities.filter(entity -> {
				l1Cache.evict(getName(), entity.getIdValue());
				return true;
			});
		}
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (cacheable) l1Cache.evict(getName(), id);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll()
	{
		if (cacheable) l1Cache.evictAll(getName());
		decoratedRepository.deleteAll();
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		if (cacheable)
		{
			String entityName = getName();
			ids = ids.filter(id -> {
				l1Cache.evict(entityName, id);
				return true;
			});
		}
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public Set<QueryRule.Operator> getQueryOperators()
	{
		return decoratedRepository.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decoratedRepository.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decoratedRepository.aggregate(aggregateQuery);
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepository.rebuildIndex();
	}

	/**
	 * Looks up the Entities for a List of entity IDs.
	 * Those present in the cache are returned from cache. The missing ones are retrieved from the decoratedRepository.
	 *
	 * @param batch list of entity IDs to look up
	 * @return List of {@link Entity}s
	 */
	private List<Entity> findAllBatch(List<Object> batch)
	{
		String entityName = getName();
		List<Object> missingIds = batch.stream().filter(id -> l1Cache.get(entityName, id, getEntityMetaData()) == null)
				.collect(toList());

		Map<Object, Entity> missingEntities = decoratedRepository.findAll(missingIds.stream())
				.collect(toMap(Entity::getIdValue, e -> e));

		return Lists.transform(batch, id -> {
			Entity result = l1Cache.get(entityName, id, getEntityMetaData());
			if (result == null)
			{
				result = missingEntities.get(id);
			}
			return result;
		});
	}
}
