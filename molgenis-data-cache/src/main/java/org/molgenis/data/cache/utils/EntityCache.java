package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * Caches {@link org.molgenis.data.Entity}s from different repositories in dehydrated form in a Guava {@link Cache}.
 */
public class EntityCache
{
	private final EntityHydration cachingUtils;
	private final Cache<String, Map<String, Object>> cache;

	/**
	 * Creates a new {@link EntityCache}
	 *
	 * @param cachingUtils {@link EntityHydration} used to hydrate and dehydrate the entities and generate cache keys
	 * @param cache        the {@link Cache} to store the entities in
	 */
	public EntityCache(EntityHydration cachingUtils, Cache<String, Map<String, Object>> cache)
	{
		this.cachingUtils = requireNonNull(cachingUtils);
		this.cache = requireNonNull(cache);
	}

	/**
	 * Evicts an entity from cache based on entity name and entity id combination
	 *
	 * @param entityName the name of the entity to evict
	 * @param entityId   the id value of the entity to evict
	 */
	public void evict(String entityName, Object entityId)
	{
		String key = generateCacheKey(entityName, entityId);
		cache.invalidate(key);
	}

	/**
	 * Evicts all entries from the cache that belong to a certain entityName.
	 *
	 * @param entityName the name of the entity whose entries are to be evicted
	 */
	public void evictAll(String entityName)
	{
		cache.asMap().keySet().stream().filter(getKeyFilter(entityName)).forEach(cache::invalidate);
	}

	/**
	 * Retrieves an entity from the cache if present.
	 *
	 * @param entityMetaData EntityMetaData of the entity to retrieve
	 * @param id             id value of the entity to retrieve
	 * @return Optional {@link Entity} with the result, or an empty optional if the entity was not found
	 */
	public Optional<Entity> getIfPresent(EntityMetaData entityMetaData, Object id)
	{
		String entityName = entityMetaData.getName();
		String key = generateCacheKey(entityName, id);
		return Optional.ofNullable(cache.getIfPresent(key))
				.map(dehydratedEntity -> cachingUtils.hydrate(dehydratedEntity, entityMetaData));
	}

	/**
	 * Inserts an entity into the cache.
	 *
	 * @param entity the entity to insert into the cache
	 */
	public void put(Entity entity)
	{
		String entityName = entity.getEntityMetaData().getName();
		String key = generateCacheKey(entityName, entity.getIdValue());
		cache.put(key, cachingUtils.dehydrate(entity));
	}

	String generateCacheKey(String entityName, Object id)
	{
		return entityName + "__" + id.toString();
	}

	Predicate<String> getKeyFilter(String entityName)
	{
		String prefix = entityName + "__";
		return key -> key.startsWith(prefix);
	}

}
