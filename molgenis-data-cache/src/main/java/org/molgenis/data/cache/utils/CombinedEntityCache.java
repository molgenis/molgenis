package org.molgenis.data.cache.utils;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.Cache;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityType;

/**
 * Caches {@link CacheHit}s with {@link Entity}s from different repositories in dehydrated form in a
 * single combined Guava {@link Cache}.
 */
public class CombinedEntityCache {
  private final EntityHydration entityHydration;
  private final Cache<EntityKey, CacheHit<Map<String, Object>>> cache;

  /**
   * Creates a new {@link CombinedEntityCache}
   *
   * @param entityHydration {@link EntityHydration} used to hydrate and dehydrate the entities and
   *     generate cache keys
   * @param cache the {@link Cache} to store the {@link CacheHit}s in
   */
  public CombinedEntityCache(
      EntityHydration entityHydration, Cache<EntityKey, CacheHit<Map<String, Object>>> cache) {
    this.entityHydration = requireNonNull(entityHydration);
    this.cache = requireNonNull(cache);
  }

  /**
   * Caches the deletion of an entity instance.
   *
   * @param entityKey the {@link EntityKey} of the deleted entity instance
   */
  public void putDeletion(EntityKey entityKey) {
    cache.put(entityKey, CacheHit.empty());
  }

  /**
   * Evicts all entries from the cache that belong to a certain entityType.
   *
   * @param entityType the id of the entity whose entries are to be evicted
   */
  public void evictAll(EntityType entityType) {
    cache
        .asMap()
        .keySet()
        .stream()
        .filter(e -> e.getEntityTypeId().equals(entityType.getId()))
        .forEach(cache::invalidate);
  }

  /**
   * Retrieves an entity from the cache if present.
   *
   * @param entityType EntityType of the entity to retrieve
   * @param id id value of the entity to retrieve
   * @return Optional {@link CacheHit<Entity>} with the result from the cache, empty if no record of
   *     the entity is present in the cache
   */
  public Optional<CacheHit<Entity>> getIfPresent(EntityType entityType, Object id) {
    EntityKey key = EntityKey.create(entityType, id);
    return Optional.ofNullable(cache.getIfPresent(key))
        .map(cacheHit -> hydrate(cacheHit, entityType));
  }

  /**
   * Inserts an entity into the cache.
   *
   * @param entity the entity to insert into the cache
   */
  public void put(Entity entity) {
    EntityType entityType = entity.getEntityType();
    cache.put(
        EntityKey.create(entityType, entity.getIdValue()),
        CacheHit.of(entityHydration.dehydrate(entity)));
  }

  public void evict(Stream<EntityKey> entityKeys) {
    entityKeys.forEach(cache::invalidate);
  }

  private CacheHit<Entity> hydrate(
      CacheHit<Map<String, Object>> cacheHitAsMap, EntityType entityType) {
    if (cacheHitAsMap.isEmpty()) {
      return CacheHit.empty();
    } else {
      return CacheHit.of(entityHydration.hydrate(cacheHitAsMap.getValue(), entityType));
    }
  }
}
