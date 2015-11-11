package org.molgenis.ui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;

public class EntityListenerRepositoryDecorator implements Repository
{
	private final Repository decoratedRepository;
	private Map<Object, EntityListener> entityListeners;

	public EntityListenerRepositoryDecorator(Repository decoratedRepository)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
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
	public Iterable<Entity> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepository.findOne(id);
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		return decoratedRepository.findOne(id, fetch);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids, Fetch fetch)
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
			EntityListener entityListener = entityListeners.get(entity.getIdValue());
			if (entityListener != null)
			{
				entityListener.postUpdate(entity);
			}
		}
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		decoratedRepository.update(records);

		if (entityListeners != null)
		{
			for (Entity entity : records)
			{
				EntityListener entityListener = entityListeners.get(entity.getIdValue());
				if (entityListener != null)
				{
					entityListener.postUpdate(entity);
				}
			}
		}
	}

	@Override
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		decoratedRepository.deleteById(ids);
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
	public Integer add(Iterable<? extends Entity> entities)
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
	public void create()
	{
		decoratedRepository.create();
	}

	@Override
	public void drop()
	{
		decoratedRepository.drop();
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
			entityListeners = new HashMap<>();
		}
		entityListeners.put(entityListener.getEntityId(), entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		if (entityListeners != null)
		{
			entityListeners.remove(entityListener.getEntityId());
		}
	}
}
