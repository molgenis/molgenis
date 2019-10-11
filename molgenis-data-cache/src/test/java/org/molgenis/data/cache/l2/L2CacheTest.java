package org.molgenis.data.cache.l2;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;

import com.google.common.util.concurrent.UncheckedExecutionException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.molgenis.data.support.PartialEntity;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionManager;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration(classes = L2CacheTest.Config.class)
class L2CacheTest extends AbstractMolgenisSpringTest {
  private L2Cache l2Cache;

  @Autowired private EntityHydration entityHydration;

  @Autowired private EntityTestHarness entityTestHarness;

  @Autowired private EntityManager entityManager;

  @Mock private TransactionManager transactionManager;
  @Mock private Repository<Entity> repository;
  @Mock private TransactionInformation transactionInformation;
  @Captor private ArgumentCaptor<Stream<Object>> idStreamCaptor;

  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
  private List<Entity> testEntities;
  private EntityType emd;

  @BeforeEach
  void beforeMethod() {
    EntityType refEntityType = entityTestHarness.createDynamicRefEntityType();
    emd = entityTestHarness.createDynamicTestEntityType(refEntityType);
    List<Entity> refEntities = entityTestHarness.createTestRefEntities(refEntityType, 2);
    testEntities = entityTestHarness.createTestEntities(emd, 4, refEntities).collect(toList());

    when(entityManager.create(emd, NO_POPULATE))
        .thenAnswer(invocation -> new EntityWithComputedAttributes(new DynamicEntity(emd)));
    when(entityManager.getReference(any(EntityType.class), eq("0"))).thenReturn(refEntities.get(0));
    when(entityManager.getReference(any(EntityType.class), eq("1"))).thenReturn(refEntities.get(1));
    when(entityManager.getReferences(any(EntityType.class), eq(newArrayList("0"))))
        .thenReturn(newArrayList(refEntities.get(0)));
    when(entityManager.getReferences(any(EntityType.class), eq(newArrayList("1"))))
        .thenReturn(newArrayList(refEntities.get(1)));

    when(repository.getEntityType()).thenReturn(emd);
    when(repository.getName()).thenReturn(emd.getId());

    l2Cache =
        new L2Cache(transactionManager, entityHydration, transactionInformation, meterRegistry);
  }

  @Test
  void testAfterCommitTransactionRemovesCacheForDirtyRepository() {
    // load the entity through the cache
    Entity entity2 = testEntities.get(2);
    when(repository.findOneById("2")).thenReturn(entity2);
    Entity result = l2Cache.get(repository, "2");
    assertTrue(EntityUtils.equals(result, entity2));

    // get the entity from the cache without touching the repository
    result = l2Cache.get(repository, "2");
    assertTrue(EntityUtils.equals(result, entity2));
    verify(repository, times(1)).findOneById("2");

    // Commit a transaction that has dirtied the repository
    when(transactionInformation.getEntirelyDirtyRepositories()).thenReturn(singleton(emd.getId()));
    l2Cache.afterCommitTransaction("transactionID");

    // get the entity a third time
    result = l2Cache.get(repository, "2");
    assertTrue(EntityUtils.equals(result, entity2));

    // it was no longer present in the cache and got loaded through the repository
    verify(repository, times(2)).findOneById("2");

    // But now it sits in the cache again
    result = l2Cache.get(repository, "2");
    assertTrue(EntityUtils.equals(result, entity2));
    verify(repository, times(2)).findOneById("2");
  }

  @Test
  void testAfterCommitTransactionRemovesEntityForDirtyEntity() {
    Entity entity2 = testEntities.get(2);
    Entity entity3 = testEntities.get(3);
    when(repository.findOneById("2")).thenReturn(entity2);
    when(repository.findOneById("3")).thenReturn(entity3);

    // load the entities through the cache
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "2"), entity2));
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "3"), entity3));

    // get the entities from the cache without touching the repository
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "2"), entity2));
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "3"), entity3));
    verify(repository, times(1)).findOneById("2");
    verify(repository, times(1)).findOneById("3");

    // Commit a transaction that has dirtied entity3, but not entity2
    when(transactionInformation.getEntirelyDirtyRepositories()).thenReturn(emptySet());
    when(transactionInformation.getDirtyEntities())
        .thenReturn(singleton(EntityKey.create(entity3)));
    l2Cache.afterCommitTransaction("transactionID");

    // get the entities for a third time
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "2"), entity2));
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "3"), entity3));

    // entity2 was still in the cache
    // entity3 was no longer present in the cache and got loaded through the repository
    verify(repository, times(1)).findOneById("2");
    verify(repository, times(2)).findOneById("3");

    // From now on, both sit in the cache again
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "2"), entity2));
    assertTrue(EntityUtils.equals(l2Cache.get(repository, "3"), entity3));
    verify(repository, times(1)).findOneById("2");
    verify(repository, times(2)).findOneById("3");
  }

  @Test
  void testGetStringIdCachesLoadedData() {
    Entity entity2 = testEntities.get(2);
    when(repository.findOneById("2")).thenReturn(entity2);

    Entity result = l2Cache.get(repository, "2");
    assertTrue(EntityUtils.equals(result, entity2));

    result = l2Cache.get(repository, "2");
    assertTrue(EntityUtils.equals(result, entity2));

    verify(repository, times(1)).findOneById("2");
  }

  @Test
  void testGetFetchStringIdCachesLoadedData() {
    Fetch fetch = new Fetch().field(EntityTestHarness.ATTR_ID);
    when(entityManager.createFetch(emd, fetch))
        .thenAnswer(
            invocation ->
                new EntityWithComputedAttributes(
                    new PartialEntity(new DynamicEntity(emd), fetch, entityManager)));

    Entity entity2 = testEntities.get(2);
    when(repository.findOneById("2")).thenReturn(entity2);

    Entity result = l2Cache.get(repository, "2", fetch);
    assertEquals("2", result.getIdValue());

    result = l2Cache.get(repository, "2", fetch);
    assertEquals("2", result.getIdValue());

    verify(repository, times(1)).findOneById("2");
  }

  @Test
  void testGetStringIdLoaderThrowsException() {
    when(repository.findOneById("2"))
        .thenThrow(new MolgenisDataException("Table is missing for entity TestEntity"));
    assertThrows(UncheckedExecutionException.class, () -> l2Cache.get(repository, "2"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetBatchIdLoaderThrowsException() {
    when(repository.findAll(any(Stream.class)))
        .thenThrow(new MolgenisDataException("Table is missing for entity TestEntity"));
    assertThrows(
        UncheckedExecutionException.class,
        () -> l2Cache.getBatch(repository, newArrayList("1", "2")));
  }

  @Test
  void testGetBatch() {
    when(repository.findAll(idStreamCaptor.capture())).thenReturn(testEntities.stream());
    List<Entity> result = l2Cache.getBatch(repository, newArrayList("0", "1", "2", "3"));
    Map<Object, Entity> retrievedEntities =
        result.stream().collect(toMap(Entity::getIdValue, e -> e));
    assertEquals(4, retrievedEntities.size());
    assertTrue(EntityUtils.equals(retrievedEntities.get("1"), testEntities.get(1)));
    assertEquals(newArrayList("0", "1", "2", "3"), idStreamCaptor.getValue().collect(toList()));
  }

  @Test
  void testGetBatchFetch() {
    Fetch fetch = new Fetch().field(EntityTestHarness.ATTR_ID);
    when(entityManager.createFetch(emd, fetch))
        .thenAnswer(
            invocation ->
                new EntityWithComputedAttributes(
                    new PartialEntity(new DynamicEntity(emd), fetch, entityManager)));

    when(repository.findAll(idStreamCaptor.capture())).thenReturn(testEntities.stream());
    List<Entity> result = l2Cache.getBatch(repository, newArrayList("0", "1", "2", "3"), fetch);
    Map<Object, Entity> retrievedEntities =
        result.stream().collect(toMap(Entity::getIdValue, e -> e));
    assertEquals(4, retrievedEntities.size());
    assertEquals(testEntities.get(1).getIdValue(), retrievedEntities.get("1").getIdValue());
    assertEquals(newArrayList("0", "1", "2", "3"), idStreamCaptor.getValue().collect(toList()));
  }

  @Configuration
  @Import({EntityHydration.class, TestHarnessConfig.class})
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
