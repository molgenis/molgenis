package org.molgenis.data.cache.l1;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.data.EntityKey.create;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.EntityManager;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = L1CacheTest.Config.class)
class L1CacheTest extends AbstractMolgenisSpringTest {
  private L1Cache l1Cache;
  private EntityType entityType;
  private Entity entity1;
  private Entity entity2;

  private final String transactionID = "__TRANSACTION__";
  private final String repository = "TestRepository";
  private final String entityID1 = "1";
  private final String entityID2 = "2";

  @Autowired private EntityTypeFactory entityTypeFactory;

  @Autowired private AttributeFactory attributeFactory;

  @Autowired private EntityManager entityManager;

  @Autowired private EntityHydration entityHydration;

  @Mock private TransactionManager transactionManager;

  @BeforeEach
  void beforeMethod() {
    entityType = entityTypeFactory.create(repository);
    entityType.addAttribute(attributeFactory.create().setName("ID"), ROLE_ID);
    entityType.addAttribute(attributeFactory.create().setName("ATTRIBUTE_1"));

    Mockito.when(entityManager.create(entityType, NO_POPULATE))
        .thenReturn(new DynamicEntity(entityType));

    entity1 = new DynamicEntity(entityType);
    entity1.set("ID", entityID1);
    entity1.set("ATTRIBUTE_1", "test_value_1");

    entity2 = new DynamicEntity(entityType);
    entity2.set("ID", entityID2);
    entity2.set("ATTRIBUTE_1", "test_value_2");

    l1Cache = new L1Cache(transactionManager, entityHydration);
  }

  @Test
  void testWhenNotInTransaction() {
    // Without a transactionStarted() call the cache does not exist, return null
    l1Cache.put(repository, entity1);
    Optional<CacheHit<Entity>> optionalCacheHit = l1Cache.get(repository, entityID1, entityType);
    assertFalse(optionalCacheHit.isPresent());
  }

  @Test
  void testPutAndGetWhenInTransaction() {
    // Start transaction
    l1Cache.transactionStarted(transactionID);

    // Entity has not been added to cache, return no CacheHit
    Optional<CacheHit<Entity>> actualEntity = l1Cache.get(repository, entityID1, entityType);
    assertEquals(empty(), actualEntity);

    // Entity has been added to cache, return entity
    l1Cache.put(repository, entity1);
    CacheHit<Entity> result = l1Cache.get(repository, entityID1, entityType).get();
    assertTrue(EntityUtils.equals(result.getValue(), entity1));

    // Cleanup after transaction and expect the cache to be cleared, return no CacheHit
    l1Cache.doCleanupAfterCompletion(transactionID);
    actualEntity = l1Cache.get(repository, entityID1, entityType);
    assertEquals(empty(), actualEntity);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  void testEvictStream() {
    // Start transaction
    l1Cache.transactionStarted(transactionID);

    // Entity has been added to cache, return entity
    l1Cache.put(repository, entity1);
    l1Cache.put(repository, entity2);
    CacheHit<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity1));
    actualEntity = l1Cache.get(repository, entityID2, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity2));

    l1Cache.evict(Stream.of(EntityKey.create(entity1), EntityKey.create(entity2)));

    Optional<CacheHit<Entity>> result = l1Cache.get(repository, entityID1, entityType);
    assertEquals(empty(), result);
    result = l1Cache.get(repository, entityID2, entityType);
    assertEquals(empty(), result);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  void testEvictStreamOfOneEntity() {
    // Start transaction
    l1Cache.transactionStarted(transactionID);

    // Entity has been added to cache, return entity
    l1Cache.put(repository, entity1);
    l1Cache.put(repository, entity2);
    CacheHit<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity1));
    actualEntity = l1Cache.get(repository, entityID2, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity2));

    l1Cache.evict(Stream.of(EntityKey.create(entity2)));

    actualEntity = l1Cache.get(repository, entityID1, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity1));
    Optional<CacheHit<Entity>> result = l1Cache.get(repository, entityID2, entityType);
    assertEquals(empty(), result);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  void testEvictAll() {
    // Start transaction
    l1Cache.transactionStarted(transactionID);

    // Entity has been added to cache, return entity
    l1Cache.put(repository, entity1);
    l1Cache.put(repository, entity2);
    CacheHit<Entity> actualEntity = l1Cache.get(repository, entityID1, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity1));
    actualEntity = l1Cache.get(repository, entityID2, entityType).get();
    assertTrue(EntityUtils.equals(actualEntity.getValue(), entity2));

    l1Cache.evictAll(entityType);

    Optional<CacheHit<Entity>> result = l1Cache.get(repository, entityID1, entityType);
    assertEquals(empty(), result);
    result = l1Cache.get(repository, entityID2, entityType);
    assertEquals(empty(), result);
  }

  @Test
  void testPutDeletionWhenInTransaction() {
    // Start transaction
    l1Cache.transactionStarted(transactionID);

    // Entity has been deleted once, return empty
    l1Cache.putDeletion(create(entity1));
    Optional<CacheHit<Entity>> actualEntity = l1Cache.get(repository, entityID1, entityType);
    assertEquals(of(CacheHit.empty()), actualEntity);

    // Cleanup transaction
    l1Cache.doCleanupAfterCompletion(transactionID);
  }

  @Test
  void testEvictAllWhenInTransaction() {
    // Start transaction
    l1Cache.transactionStarted(transactionID);

    // Evict entity, return null
    l1Cache.put(repository, entity1);
    l1Cache.evictAll(entityType);
    Optional<CacheHit<Entity>> actualEntity = l1Cache.get(repository, entityID1, entityType);
    assertEquals(empty(), actualEntity);

    // Cleanup transaction
    l1Cache.doCleanupAfterCompletion(transactionID);
  }

  @Configuration
  @Import({EntityHydration.class})
  static class Config {
    @Mock private EntityManager entityManager;

    Config() {
      org.mockito.MockitoAnnotations.initMocks(this);
    }

    @Bean
    EntityManager entityManager() {
      return entityManager;
    }
  }
}
