package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityType;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
	 *
	 * @param entityKey the {@link EntityKey} of the deleted entity instance
	 */
	public void putDeletion(EntityKey entityKey)
	{
		cache.put(entityKey, empty());
	}

	/**
	 * Evicts all entries from the cache that belong to a certain entityType.
	 *
	 * @param entityType the id of the entity whose entries are to be evicted
	 */
	public void evictAll(EntityType entityType)
	{
		cache.asMap()
			 .keySet()
			 .stream()
			 .filter(e -> e.getEntityTypeId().equals(entityType.getId()))
			 .forEach(cache::invalidate);
	}

	/**
	 * Retrieves an entity from the cache if present.
	 *
	 * @param entityType EntityType of the entity to retrieve
	 * @param id         id value of the entity to retrieve
	 * @return Optional {@link Entity} with the result from the cache,
	 * or null if no record of the entity is present in the cache
	 */
	@SuppressFBWarnings(value = "NP_OPTIONAL_RETURN_NULL", justification = "Intentional behavior")
	public Optional<Entity> getIfPresent(EntityType entityType, Object id)
	{
		Optional<Map<String, Object>> optionalDehydratedEntity = cache.getIfPresent(EntityKey.create(entityType, id));
		if (optionalDehydratedEntity == null)
		{
			// no information present in cache
			return null;
		}
		return optionalDehydratedEntity.map(dehydratedEntity -> entityHydration.hydrate(dehydratedEntity, entityType));
	}

	/**
	 * Inserts an entity into the cache.
	 *
	 * @param entity the entity to insert into the cache
	 */
	public void put(Entity entity)
	{
		EntityType entityType = entity.getEntityType();
		cache.put(EntityKey.create(entityType, entity.getIdValue()), Optional.of(entityHydration.dehydrate(entity)));
	}

	public void evict(Stream<EntityKey> entityKeys)
	{
		entityKeys.forEach(cache::invalidate);
	}
}
