package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * Caches {@link org.molgenis.data.Entity}s from different repositories in dehydrated form in a single combined
 * Guava {@link Cache}.
 */
public class CombinedEntityCache
{
	private final EntityHydration entityHydration;
	private final Cache<EntityKey, Optional<Map<String, Object>>> cache;

	/**
	 * Creates a new {@link CombinedEntityCache}
	 *
	 * @param entityHydration {@link EntityHydration} used to hydrate and dehydrate the entities and generate cache keys
	 * @param cache           the {@link Cache} to store the entities in
	 */
	public CombinedEntityCache(EntityHydration entityHydration, Cache<EntityKey, Optional<Map<String, Object>>> cache)
	{
		this.entityHydration = requireNonNull(entityHydration);
		this.cache = requireNonNull(cache);
	}

	/**
	 * Caches the deletion of an entity instance.
	 * @param entityKey the {@link EntityKey} of the deleted entity instance
	 */
	public void putDeletion(EntityKey entityKey)
	{
		cache.put(entityKey, empty());
	}

	/**
	 * Evicts all entries from the cache that belong to a certain entityName.
	 *
	 * @param entityName the name of the entity whose entries are to be evicted
	 */
	public void evictAll(String entityName)
	{
		cache.asMap().keySet().stream().filter(e -> e.getEntityName().equals(entityName)).forEach(cache::invalidate);
	}

	/**
	 * Retrieves an entity from the cache if present.
	 *
	 * @param entityMetaData EntityMetaData of the entity to retrieve
	 * @param id             id value of the entity to retrieve
	 * @return Optional {@link Entity} with the result from the cache,
	 * or null if no record of the entity is present in the cache
	 */
	public Optional<Entity> getIfPresent(EntityMetaData entityMetaData, Object id)
	{
		Optional<Map<String, Object>> optionalDehydratedEntity = cache
				.getIfPresent(EntityKey.create(entityMetaData, id));
		if (optionalDehydratedEntity == null)
		{
			// no information present in cache
			return null;
		}
		return optionalDehydratedEntity
				.map(dehydratedEntity -> entityHydration.hydrate(dehydratedEntity, entityMetaData));
	}

	/**
	 * Inserts an entity into the cache.
	 *
	 * @param entity the entity to insert into the cache
	 */
	public void put(Entity entity)
	{
		String entityName = entity.getEntityMetaData().getName();
		cache.put(EntityKey.create(entityName, entity.getIdValue()), Optional.of(entityHydration.dehydrate(entity)));
	}

}
