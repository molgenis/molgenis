package org.molgenis.data.cache.l1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.test.AbstractMockitoTest;

class L1CacheTest extends AbstractMockitoTest {
  private static final String TRANSACTION_ID = "MyTransactionId";

  @Mock TransactionManager transactionManager;
  @Mock EntityHydration entityHydration;
  private L1Cache l1Cache;

  @BeforeEach
  void setUpBeforeMethod() {
    l1Cache = new L1Cache(transactionManager, entityHydration);
    l1Cache.transactionStarted(TRANSACTION_ID);
  }

  @AfterEach
  void tearDownAfterEach() {
    l1Cache.doCleanupAfterCompletion(TRANSACTION_ID);
  }

  @Test
  void putAndGet() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Object entityId = mock(Object.class);
    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(entityId).getMock();
    when(entity.getEntityType()).thenReturn(entityType);

    Map<String, Object> hydratedEntity = Collections.singletonMap("id", entityId);
    when(entityHydration.dehydrate(entity)).thenReturn(hydratedEntity);
    when(entityHydration.hydrate(hydratedEntity, entityType, null)).thenReturn(entity);

    l1Cache.put(entity);
    assertEquals(Optional.of(CacheHit.of(entity)), l1Cache.get(entityType, entityId));
  }

  @Test
  void putAndGetFetch() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Object entityId = mock(Object.class);
    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(entityId).getMock();
    when(entity.getEntityType()).thenReturn(entityType);

    Map<String, Object> hydratedEntity = Collections.singletonMap("id", entityId);
    Fetch fetch = mock(Fetch.class);
    when(entityHydration.dehydrate(entity)).thenReturn(hydratedEntity);
    when(entityHydration.hydrate(hydratedEntity, entityType, fetch)).thenReturn(entity);

    l1Cache.put(entity);
    assertEquals(Optional.of(CacheHit.of(entity)), l1Cache.get(entityType, entityId, fetch));
  }

  @Test
  void putDeletionEntity() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Object entityId = mock(Object.class);
    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(entityId).getMock();
    when(entity.getEntityType()).thenReturn(entityType);

    l1Cache.putDeletion(entity);
    assertEquals(Optional.of(CacheHit.empty()), l1Cache.get(entityType, entityId));
  }

  @Test
  void putDeletionEntityTypeObject() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Object entityId = mock(Object.class);

    l1Cache.putDeletion(entityType, entityId);
    assertEquals(Optional.of(CacheHit.empty()), l1Cache.get(entityType, entityId));
  }

  @Test
  void evict() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Object entityId = mock(Object.class);
    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(entityId).getMock();
    when(entity.getEntityType()).thenReturn(entityType);

    l1Cache.put(entity);
    l1Cache.evict(entity);

    assertEquals(Optional.empty(), l1Cache.get(entityType, entityId));
  }

  @Test
  void evictAll() {
    String entityTypeId = "MyEntityTypeId";
    EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(entityTypeId).getMock();
    Object entityId = mock(Object.class);
    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn(entityId).getMock();
    when(entity.getEntityType()).thenReturn(entityType);

    l1Cache.putDeletion(entity);
    l1Cache.evictAll(entityType);

    assertEquals(Optional.empty(), l1Cache.get(entityType, entityId));
  }
}
