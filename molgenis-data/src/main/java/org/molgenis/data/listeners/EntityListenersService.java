package org.molgenis.data.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Service
public final class EntityListenersService
{
	private final Logger LOG = LoggerFactory.getLogger(EntityListenersService.class);
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<String, SetMultimap<Object, EntityListener>> entityListeners = new HashMap<>();

	/**
	 * Register a repository to the entity listeners service once
	 */
	void register(String repoFullName)
	{
		lock.writeLock().lock();
		try
		{
			if (!entityListeners.containsKey(requireNonNull(repoFullName)))
			{
				entityListeners.put(repoFullName, HashMultimap.create());
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Update all registered listeners of the entities
	 *
	 * @return Stream<Entity>
	 */
	Stream<Entity> updateEntities(String repoFullName, Stream<Entity> entities)
	{
		lock.readLock().lock();
		try
		{
			verifyRepoRegistered(repoFullName);
			SetMultimap<Object, EntityListener> entityListeners = this.entityListeners.get(repoFullName);
			return entities.filter(entity ->
			{
				Set<EntityListener> entityEntityListeners = entityListeners.get(entity.getIdValue());
				entityEntityListeners.forEach(entityListener -> entityListener.postUpdate(entity));
				return true;
			});
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * Update all registered listeners of an entity
	 */
	void updateEntity(String repoFullName, Entity entity)
	{
		lock.readLock().lock();
		try
		{
			verifyRepoRegistered(repoFullName);
			SetMultimap<Object, EntityListener> entityListeners = this.entityListeners.get(repoFullName);
			Set<EntityListener> entityEntityListeners = entityListeners.get(entity.getIdValue());
			entityEntityListeners.forEach(entityListener -> entityListener.postUpdate(entity));
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * Adds an entity listener for a entity of the given class that listens to entity changes
	 *
	 * @param entityListener entity listener for a entity
	 */

	public void addEntityListener(String repoFullName, EntityListener entityListener)
	{
		lock.writeLock().lock();
		try
		{
			verifyRepoRegistered(repoFullName);
			SetMultimap<Object, EntityListener> entityListeners = this.entityListeners.get(repoFullName);
			entityListeners.put(entityListener.getEntityId(), entityListener);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes an entity listener for a entity of the given class
	 *
	 * @param entityListener entity listener for a entity
	 * @return boolean
	 */
	public boolean removeEntityListener(String repoFullName, EntityListener entityListener)
	{
		lock.writeLock().lock();
		try
		{
			verifyRepoRegistered(repoFullName);
			SetMultimap<Object, EntityListener> entityListeners = this.entityListeners.get(repoFullName);
			if (entityListeners.containsKey(entityListener.getEntityId()))
			{
				entityListeners.remove(entityListener.getEntityId(), entityListener);
				return true;
			}
			return false;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Check if a repository has no listeners
	 * Repository must be registered
	 *
	 * @return boolean
	 */
	boolean isEmpty(String repoFullName)
	{
		lock.readLock().lock();
		try
		{
			verifyRepoRegistered(repoFullName);
			return entityListeners.get(repoFullName).isEmpty();
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * Verify that the repository is registered
	 */
	private void verifyRepoRegistered(String repoFullName)
	{
		lock.readLock().lock();
		try
		{
			if (!entityListeners.containsKey(requireNonNull(repoFullName)))
			{
				LOG.error("Repository [" + repoFullName + "] is not registered in the entity listeners service");
				throw new MolgenisDataException(
						"Repository [" + repoFullName + "] is not registered, please contact your administrator");
			}
		}
		finally
		{
			lock.readLock().unlock();
		}
	}
}

