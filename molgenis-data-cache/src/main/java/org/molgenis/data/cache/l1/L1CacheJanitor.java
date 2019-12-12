package org.molgenis.data.cache.l1;

import java.util.stream.Stream;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

/** Performs {@link L1Cache} cleaning on cache adds/updates/deletes. */
public interface L1CacheJanitor {
  void cleanCacheBeforeAdd(Entity entity);

  Stream<Entity> cleanCacheBeforeAdd(EntityType entityType, Stream<Entity> entityStream);

  void cleanCacheBeforeUpdate(Entity entity);

  Stream<Entity> cleanCacheBeforeUpdate(EntityType entityType, Stream<Entity> entityStream);

  void cleanCacheBeforeDelete(Entity entity);

  Stream<Entity> cleanCacheBeforeDelete(EntityType entityType, Stream<Entity> entityStream);

  void cleanCacheBeforeDeleteById(EntityType entityType, Object entityId);

  Stream<Object> cleanCacheBeforeDeleteById(EntityType entityType, Stream<Object> entityIdStream);

  void cleanCacheBeforeDeleteAll(EntityType entityType);
}
