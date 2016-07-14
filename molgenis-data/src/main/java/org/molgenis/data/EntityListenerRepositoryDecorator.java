package org.molgenis.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.EntityMetaData;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class EntityListenerRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decoratedRepository;
	private SetMultimap<Object, EntityListener> entityListeners;

	public EntityListenerRepositoryDecorator(Repository<Entity> decoratedRepository)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		decoratedRepository.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepository.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
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
	public Entity findOneById(Object id)
	{
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
		return decoratedRepository.findAll(ids);
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
		decoratedRepository.update(entity);

		if (entityListeners != null)
		{
			Set<EntityListener> entityEntityListeners = entityListeners.get(entity.getIdValue());
			entityEntityListeners.forEach(entityListener -> {
				entityListener.postUpdate(entity);
			});
		}
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		if (entityListeners != null)
		{
			entities = entities.filter(entity -> {
				Set<EntityListener> entityEntityListeners = entityListeners.get(entity.getIdValue());
				entityEntityListeners.forEach(entityListener -> {
					entityListener.postUpdate(entity);
				});
				return true;
			});
		}
		decoratedRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		decoratedRepository.deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		return decoratedRepository.add(entities);
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
		if (entityListeners == null)
		{
			entityListeners = HashMultimap.<Object, EntityListener> create();
		}
		entityListeners.put(entityListener.getEntityId(), entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		if (entityListeners != null)
		{
			entityListeners.remove(entityListener.getEntityId(), entityListener);
		}
	}
}
