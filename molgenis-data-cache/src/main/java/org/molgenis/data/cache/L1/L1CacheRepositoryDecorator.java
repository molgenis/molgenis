package org.molgenis.data.cache.L1;

import org.molgenis.data.*;
import org.molgenis.data.meta.model.EntityMetaData;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCollectionCapability.CACHEABLE;
import static org.slf4j.LoggerFactory.getLogger;

public class L1CacheRepositoryDecorator implements Repository<Entity>
{
	private static final Logger LOG = getLogger(L1CacheRepositoryDecorator.class);

	private final Repository<Entity> decoratedRepository;
	private final L1Cache l1Cache;

	private final boolean cacheable = getCapabilities().contains(CACHEABLE);

	public L1CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L1Cache l1Cache)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l1Cache = requireNonNull(l1Cache);
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
			List<Object> missingIds = newArrayList();
			List<Entity> entitiesFromCache = newArrayList();
			String entityName = getName();
			ids = ids.filter(id -> {
				Entity cacheEntity = l1Cache.get(entityName, id, getEntityMetaData());

				if (cacheEntity != null) entitiesFromCache.add(cacheEntity);
				else missingIds.add(id);

				return true;
			});

			if (!entitiesFromCache.isEmpty())
			{
				// If there are missing IDs, retrieve them from the decorated repository and concat
				// them together with the enitities found in the stream
				if (!missingIds.isEmpty())
					return Stream.concat(decoratedRepository.findAll(missingIds.stream()), entitiesFromCache.stream());
				return entitiesFromCache.stream();
			}
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
	public Query query()
	{
		return decoratedRepository.query();
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
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

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepository.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepository.removeEntityListener(entityListener);
	}
}
