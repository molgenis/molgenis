package org.molgenis.data.cache;

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
import static org.slf4j.LoggerFactory.getLogger;

public class L1CacheRepositoryDecorator implements Repository<Entity>
{
	private static final Logger LOG = getLogger(L1CacheRepositoryDecorator.class);

	private final Repository<Entity> decoratedRepository;
	private final L1Cache l1Cache;

	public L1CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L1Cache l1Cache)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l1Cache = requireNonNull(l1Cache);
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
		// TODO l1Cache.getEntityMetaData(entityName)???
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
	public Entity findOneById(Object id)
	{
		// TODO CHECK IF THIS IS CORRECT
		Entity entity = l1Cache.cacheGet(getName(), id);
		if (entity == null) return decoratedRepository.findOneById(id);
		return entity;
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		// TODO CHECK IF THIS IS CORRECT
		Entity entity = l1Cache.cacheGet(getName(), id);
		if (entity == null) return decoratedRepository.findOneById(id, fetch);
		return entity;
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		// TODO SHOULD WE MAKE A l1Cache method for query???
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		// TODO SHOULD WE MAKE A l1Cache method for query???
		return decoratedRepository.findOne(q);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		List<Entity> entitiesFromCache = newArrayList();
		String entityName = getName();
		ids.forEach(id -> {
			Entity cacheEntity = l1Cache.cacheGet(entityName, id);
			if(cacheEntity != null) entitiesFromCache.add(cacheEntity);
		});
		if(entitiesFromCache.isEmpty()) return decoratedRepository.findAll(ids);
		return entitiesFromCache.stream();
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
	public void update(Entity entity)
	{
		// TODO Is this correct?
		l1Cache.cachePut(getName(), entity);
		decoratedRepository.update(entity);
	}

	@Override
	public void delete(Entity entity)
	{
		l1Cache.cacheEvict(getName(), entity.getIdValue());
		decoratedRepository.delete(entity);
	}

	@Override
	public void deleteById(Object id)
	{
		l1Cache.cacheEvict(getName(), id);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		String entityName = getName();
		ids.forEach(id -> l1Cache.cacheEvict(entityName, id));
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		l1Cache.cacheEvictAll(getName());
		decoratedRepository.deleteAll();
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		entities = entities.filter(entity -> {
			l1Cache.cacheEvict(getName(), entity.getIdValue());
			return true;
		});
		decoratedRepository.delete(entities);
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
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

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return null;
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		entities = entities.filter(entity -> {
			l1Cache.cacheEvict(getName(), entity.getIdValue());
			return true;
		});
		decoratedRepository.update(entities);
	}
}
