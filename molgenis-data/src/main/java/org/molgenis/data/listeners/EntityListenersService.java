package org.molgenis.data.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Service
public final class EntityListenersService
{
	private final Logger LOG = LoggerFactory.getLogger(EntityListenersService.class);
	private static final ConcurrentHashMap<String, SetMultimap<Object, EntityListener>> entityListeners = new ConcurrentHashMap<>();

	/**
	 * Register a repository to the entity listeners service once
	 *
	 * @param repoFullName
	 * @return
	 */
	void register(String repoFullName)
	{
		if (!entityListeners.containsKey(requireNonNull(repoFullName))){
			entityListeners.put(repoFullName, HashMultimap.create());
		}
	}

	Stream<Entity> updateEntities(String repoFullName, Stream<Entity> entities)
	{
		verifyRepoRegistered(repoFullName);

		SetMultimap<Object, EntityListener> entityListeners = this.getEntityListeners(repoFullName);

		synchronized(entityListeners){
			return entities.filter(entity -> {
				Set<EntityListener> entityEntityListeners = entityListeners.get(entity.getIdValue());
				entityEntityListeners.forEach(entityListener -> {
					entityListener.postUpdate(entity);
				});
				return true;
			});
		}
	}

	void updateEntity(String repoFullName, Entity entity)
	{
		verifyRepoRegistered(repoFullName);
		SetMultimap<Object, EntityListener> entityListeners = this.getEntityListeners(repoFullName);
		Set<EntityListener> entityEntityListeners = entityListeners.get(entity.getIdValue());
		entityEntityListeners.forEach(entityListener -> {
			entityListener.postUpdate(entity);
		});
	}

	void addEntityListener(String repoFullName, EntityListener entityListener){
		verifyRepoRegistered(repoFullName);
		SetMultimap<Object, EntityListener> entityListeners = this.getEntityListeners(repoFullName);
		synchronized(entityListeners)
		{
			entityListeners.put(entityListener.getEntityId(), entityListener);
		}
	}

	void removeEntityListener(String repoFullName, EntityListener entityListener){
		verifyRepoRegistered(repoFullName);
		SetMultimap<Object, EntityListener> entityListeners = this.getEntityListeners(repoFullName);
		synchronized(entityListeners)
		{
			entityListeners.remove(entityListener.getEntityId(), entityListener);
		}
	}

	boolean isEmpty(String repoFullName){
		verifyRepoRegistered(repoFullName);
		return this.getEntityListeners(repoFullName).isEmpty();
	}

	protected void verifyRepoRegistered(String repoFullName){
		if (!entityListeners.containsKey(requireNonNull(repoFullName)))
		{
			LOG.error("Repository [" + repoFullName + "] is not registered in the entity listeners service");
			throw new MolgenisDataException(
					"Cannot find entity [" + repoFullName + "], please contact your administrator");
		}
	}

	/**
	 * returns a SetMultimap<Object, EntityListener> where the object is the id of the entity and the EntityListener is listener interface
	 *
	 * @param repoFullName
	 * @return SetMultimap<Object, EntityListener>
	 */
	private SetMultimap<Object, EntityListener> getEntityListeners(String repoFullName)
	{
		return entityListeners.get(repoFullName);
	}
}

