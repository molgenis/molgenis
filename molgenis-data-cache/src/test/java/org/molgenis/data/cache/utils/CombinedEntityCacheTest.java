package org.molgenis.data.cache.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class CombinedEntityCacheTest extends AbstractMockitoTest {
  private CombinedEntityCache entityCache;
  @Mock private EntityHydration entityHydration;
  @Mock private Cache<EntityKey, CacheHit<Map<String, Object>>> cache;
  @Mock EntityType entityType;
  @Mock Entity entity;
  @Mock Map<String, Object> dehydratedEntity;

  @BeforeEach
  void beforeMethod() {
    when(entityType.getId()).thenReturn("TestEntity");
    entityCache = new CombinedEntityCache(entityHydration, cache);
  }

  @Test
  void getIfPresentIntegerIdEntityNotPresentInCache() {
    when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(null);
    assertEquals(entityCache.getIfPresent(entityType, 123), Optional.empty());
  }

  @Test
  void getIfPresentIntegerIdDeletionLoggedInCache() {
    when(cache.getIfPresent(EntityKey.create("TestEntity", 123))).thenReturn(CacheHit.empty());
    assertEquals(entityCache.getIfPresent(entityType, 123), Optional.of(CacheHit.empty()));
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  void getIfPresentIntegerIdEntityPresentInCache() {
    when(cache.getIfPresent(EntityKey.create("TestEntity", 123)))
        .thenReturn(CacheHit.of(dehydratedEntity));
    when(entityHydration.hydrate(dehydratedEntity, entityType)).thenReturn(entity);
    assertSame(
        entityCache.getIfPresent(entityType, 123).get().getValue(), CacheHit.of(entity).getValue());
  }
}
