package org.molgenis.data.cache.l1;

import static com.google.common.collect.Iterators.partition;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.meta.model.EntityType;

/**
 * Adds, removes and retrieves entities from the {@link L1Cache} when a {@link Repository} is {@link
 * RepositoryCapability#CACHEABLE}. Delegates to the underlying repository when an action is not
 * supported by the cache or when the cache doesn't contain the needed entity.
 */
public class L1CacheRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {
  private static final int BATCH_SIZE = 1000;

  private final L1Cache l1Cache;
  private final L1CacheJanitor l1CacheJanitor;
  private final boolean cacheable;

  public L1CacheRepositoryDecorator(
      Repository<Entity> delegateRepository, L1Cache l1Cache, L1CacheJanitor l1CacheJanitor) {
    super(delegateRepository);
    this.l1Cache = requireNonNull(l1Cache);
    this.l1CacheJanitor = requireNonNull(l1CacheJanitor);
    this.cacheable = delegateRepository.getCapabilities().contains(CACHEABLE);
  }

  @Override
  public void add(Entity entity) {
    if (cacheable) {
      l1CacheJanitor.cleanCacheBeforeAdd(entity);
      putCache(entity);
    }
    delegate().add(entity);
  }

  @Override
  public Integer add(Stream<Entity> entities) {
    if (cacheable) {
      entities =
          l1CacheJanitor.cleanCacheBeforeAdd(getEntityType(), entities).filter(this::putCache);
    }
    return delegate().add(entities);
  }

  @Override
  public Entity findOneById(Object id) {
    return cacheable ? findOneByIdWithCache(id, null) : delegate().findOneById(id);
  }

  @Override
  public Entity findOneById(Object id, Fetch fetch) {
    return cacheable ? findOneByIdWithCache(id, fetch) : delegate().findOneById(id, fetch);
  }

  private Entity findOneByIdWithCache(Object id, @Nullable @CheckForNull Fetch fetch) {
    EntityType entityType = getEntityType();
    Optional<CacheHit<Entity>> cacheHit = l1Cache.get(entityType, id, fetch);
    if (cacheHit.isPresent()) {
      return cacheHit.get().getValue();
    } else {
      // TODO retrieve with fetch and cache with fetch (also see findAllWithCache)
      Entity entity = delegate().findOneById(id);
      if (entity != null) {
        l1Cache.put(entity);
      } else {
        l1Cache.putDeletion(entityType, id);
      }
      return entity;
    }
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids) {
    return cacheable ? findAllWithCache(ids, null) : delegate().findAll(ids);
  }

  @Override
  public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch) {
    return cacheable ? findAllWithCache(ids, fetch) : delegate().findAll(ids, fetch);
  }

  @SuppressWarnings("UnstableApiUsage")
  private Stream<Entity> findAllWithCache(Stream<Object> ids, @Nullable @CheckForNull Fetch fetch) {
    Iterator<List<Object>> idBatches = partition(ids.iterator(), BATCH_SIZE);
    return Streams.stream(idBatches)
        .map(idBatch -> findAllWithCache(idBatch, fetch))
        .flatMap(Collection::stream)
        .filter(Objects::nonNull);
  }

  /**
   * Looks up the Entities for a List of entity IDs. Those present in the cache are returned from
   * cache. The missing ones are retrieved from the decoratedRepository.
   *
   * @param entityIds list of entity IDs to look up
   * @param fetch containing attributes to retrieve, can be null
   * @return List of {@link Entity}s
   */
  private Collection<Entity> findAllWithCache(
      List<Object> entityIds, @Nullable @CheckForNull Fetch fetch) {
    EntityType entityType = getEntityType();

    List<Object> missingEntityIds = null;
    Map<Object, Entity> entityMap = Maps.newLinkedHashMapWithExpectedSize(entityIds.size());

    for (Object entityId : entityIds) {
      Optional<CacheHit<Entity>> optionalCacheHit = l1Cache.get(entityType, entityId, fetch);
      if (optionalCacheHit.isPresent()) {
        entityMap.put(entityId, optionalCacheHit.get().getValue());
      } else {
        // placeholder value to reserve location in linked map
        entityMap.put(entityId, null);
        if (missingEntityIds == null) {
          missingEntityIds = new ArrayList<>(entityIds.size());
        }
        missingEntityIds.add(entityId);
      }
    }

    if (missingEntityIds != null && !missingEntityIds.isEmpty()) {
      // TODO retrieve with fetch and cache with fetch (also see findOneByIdWithCache)
      Stream<Entity> entityStream = delegate().findAll(missingEntityIds.stream());

      // replace placeholder values with actual values
      entityStream.forEach(
          entity -> {
            entityMap.put(entity.getIdValue(), entity);
            // TODO cache nulls for ids for which no entity exists
            l1Cache.put(entity);
          });
    }

    return entityMap.values();
  }

  @Override
  public void update(Entity entity) {
    if (cacheable) {
      l1CacheJanitor.cleanCacheBeforeUpdate(entity);
      putCache(entity);
    }
    delegate().update(entity);
  }

  @Override
  public void update(Stream<Entity> entities) {
    if (cacheable) {
      entities =
          l1CacheJanitor.cleanCacheBeforeUpdate(getEntityType(), entities).filter(this::putCache);
    }
    delegate().update(entities);
  }

  @Override
  public void delete(Entity entity) {
    if (cacheable) {
      l1CacheJanitor.cleanCacheBeforeDelete(entity);
      delCache(entity);
    }
    delegate().delete(entity);
  }

  @Override
  public void delete(Stream<Entity> entities) {
    if (cacheable) {
      entities =
          l1CacheJanitor.cleanCacheBeforeDelete(getEntityType(), entities).filter(this::delCache);
    }
    delegate().delete(entities);
  }

  @Override
  public void deleteById(Object id) {
    if (cacheable) {
      EntityType entityType = getEntityType();
      l1CacheJanitor.cleanCacheBeforeDeleteById(entityType, id);
      delCache(entityType, id);
    }
    delegate().deleteById(id);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    if (cacheable) {
      EntityType entityType = getEntityType();
      ids =
          l1CacheJanitor
              .cleanCacheBeforeDeleteById(entityType, ids)
              .filter(id -> this.delCache(entityType, id));
    }
    delegate().deleteAll(ids);
  }

  @Override
  public void deleteAll() {
    if (cacheable) {
      EntityType entityType = getEntityType();
      l1CacheJanitor.cleanCacheBeforeDeleteAll(entityType);
      l1Cache.evictAll(entityType);
    }
    delegate().deleteAll();
  }

  /** Convenience method for use in stream filter. */
  private boolean putCache(Entity entity) {
    l1Cache.put(entity);
    return true;
  }

  /** Convenience method for use in stream filter. */
  private boolean delCache(Entity entity) {
    l1Cache.putDeletion(entity);
    return true;
  }

  /** Convenience method for use in stream filter. */
  private boolean delCache(EntityType entityType, Object entityId) {
    l1Cache.putDeletion(entityType, entityId);
    return true;
  }
}
