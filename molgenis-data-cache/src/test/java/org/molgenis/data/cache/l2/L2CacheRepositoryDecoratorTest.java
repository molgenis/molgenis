package org.molgenis.data.cache.l2;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.IntStream;
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
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration(classes = TestHarnessConfig.class)
class L2CacheRepositoryDecoratorTest extends AbstractMolgenisSpringTest {
  static final int NUMBER_OF_ENTITIES = 2500;
  private L2CacheRepositoryDecorator l2CacheRepositoryDecorator;
  @Mock private L2Cache l2Cache;
  @Mock private Repository<Entity> delegateRepository;
  @Mock private TransactionInformation transactionInformation;
  @Autowired private EntityTestHarness entityTestHarness;
  private List<Entity> entities;
  @Captor private ArgumentCaptor<Iterable<Object>> cacheIdCaptor;
  @Captor private ArgumentCaptor<Stream<Object>> repoIdCaptor;
  private EntityType emd;

  @BeforeEach
  void beforeMethod() {
    emd = entityTestHarness.createDynamicRefEntityType();
    entities = entityTestHarness.createTestRefEntities(emd, 4);

    when(delegateRepository.getCapabilities()).thenReturn(Sets.newHashSet(CACHEABLE, WRITABLE));
    l2CacheRepositoryDecorator =
        new L2CacheRepositoryDecorator(delegateRepository, l2Cache, transactionInformation);
    when(delegateRepository.getEntityType()).thenReturn(emd);
    when(delegateRepository.getName()).thenReturn(emd.getId());
  }

  @Test
  void testFindOneByIdNotDirtyCacheableAndPresent() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(false);
    when(l2Cache.get(delegateRepository, "0")).thenReturn(entities.get(0));
    assertEquals(entities.get(0), l2CacheRepositoryDecorator.findOneById("0"));
  }

  @Test
  void testFindOneByIdNotDirtyCacheableNotPresent() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(false);
    when(l2Cache.get(delegateRepository, "abcde")).thenReturn(null);
    assertNull(l2CacheRepositoryDecorator.findOneById("abcde"));
  }

  @Test
  void testFindOneByIdDirty() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(true);
    when(delegateRepository.findOneById("0")).thenReturn(entities.get(0));
    assertEquals(entities.get(0), l2CacheRepositoryDecorator.findOneById("0"));
  }

  @Test
  void testFindOneByIdEntireRepositoryDirty() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(true);
    when(delegateRepository.findOneById("0")).thenReturn(entities.get(0));
    assertEquals(entities.get(0), l2CacheRepositoryDecorator.findOneById("0"));
  }

  @Test
  void testFindOneByIdFetchNotDirtyCacheableAndPresent() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(false);
    Fetch fetch = new Fetch().field("id");
    when(l2Cache.get(delegateRepository, "0", fetch)).thenReturn(entities.get(0));
    assertEquals(entities.get(0), l2CacheRepositoryDecorator.findOneById("0", fetch));
  }

  @Test
  void testFindOneByIdFetchNotDirtyCacheableNotPresent() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(false);
    Fetch fetch = new Fetch().field("id");
    when(l2Cache.get(delegateRepository, "abcde", fetch)).thenReturn(null);
    assertNull(l2CacheRepositoryDecorator.findOneById("abcde", fetch));
  }

  @Test
  void testFindOneByIdFetchDirty() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(true);
    Fetch fetch = new Fetch().field("id");
    when(delegateRepository.findOneById("0", fetch)).thenReturn(entities.get(0));
    assertEquals(entities.get(0), l2CacheRepositoryDecorator.findOneById("0", fetch));
  }

  @Test
  void testFindOneByIdFetchEntireRepositoryDirty() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(true);
    Fetch fetch = new Fetch().field("id");
    when(delegateRepository.findOneById("0", fetch)).thenReturn(entities.get(0));
    assertEquals(entities.get(0), l2CacheRepositoryDecorator.findOneById("0", fetch));
  }

  @Test
  void testFindAllEntireRepositoryDirty() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(true);
    Stream<Object> ids = Stream.of("0", "1", "2", "3");
    l2CacheRepositoryDecorator.findAll(ids);
    verify(delegateRepository).findAll(repoIdCaptor.capture());
    assertEquals(
        Stream.of("0", "1", "2", "3").collect(toList()), repoIdCaptor.getValue().collect(toList()));
  }

  @Test
  void testFindAllTransactionReadonly() {
    boolean previousReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    try {
      TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

      List<Object> ids = asList("0", "1", "2");
      List<Entity> entities = asList(mock(Entity.class), mock(Entity.class), mock(Entity.class));
      when(l2Cache.getBatch(delegateRepository, ids)).thenReturn(entities);
      assertEquals(entities, l2CacheRepositoryDecorator.findAll(ids.stream()).collect(toList()));
    } finally {
      TransactionSynchronizationManager.setCurrentTransactionReadOnly(previousReadOnly);
    }
  }

  @Test
  void testFindAllFetchEntireRepositoryDirty() {
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(true);
    Stream<Object> ids = Stream.of("0", "1", "2", "3");
    Fetch fetch = new Fetch().field("id");
    l2CacheRepositoryDecorator.findAll(ids, fetch);
    verify(delegateRepository).findAll(repoIdCaptor.capture(), eq(fetch));
    assertEquals(
        Stream.of("0", "1", "2", "3").collect(toList()), repoIdCaptor.getValue().collect(toList()));
  }

  @Test
  void testFindAllFetchTransactionReadonly() {
    boolean previousReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    try {
      TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

      Fetch fetch = mock(Fetch.class);
      List<Object> ids = asList("0", "1", "2");
      List<Entity> entities = asList(mock(Entity.class), mock(Entity.class), mock(Entity.class));
      when(l2Cache.getBatch(delegateRepository, ids, fetch)).thenReturn(entities);
      assertEquals(
          entities, l2CacheRepositoryDecorator.findAll(ids.stream(), fetch).collect(toList()));
    } finally {
      TransactionSynchronizationManager.setCurrentTransactionReadOnly(previousReadOnly);
    }
  }

  @Test
  void testFindAllFetchSplitsIdsOnTransactionInformation() {
    // repository is clean

    // 0: Dirty, not present in repo
    // 1: Dirty, present in decorated repo
    // 2: Not dirty, present in cache
    // 3: Not dirty, absence stored in cache

    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "0"))).thenReturn(true);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "1"))).thenReturn(true);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "2"))).thenReturn(false);
    when(transactionInformation.isEntityDirty(EntityKey.create(emd, "3"))).thenReturn(false);

    Fetch fetch = new Fetch().field("id");
    Stream<Object> ids = Lists.<Object>newArrayList("0", "1", "2", "3").stream();
    when(l2Cache.getBatch(eq(delegateRepository), cacheIdCaptor.capture(), eq(fetch)))
        .thenReturn(newArrayList(entities.get(3)));
    when(delegateRepository.findAll(repoIdCaptor.capture(), eq(fetch)))
        .thenReturn(of(entities.get(1)));

    List<Entity> retrievedEntities =
        l2CacheRepositoryDecorator.findAll(ids, fetch).collect(toList());

    List<Object> decoratedRepoIds = repoIdCaptor.getValue().collect(toList());
    assertEquals(newArrayList("0", "1"), decoratedRepoIds);
    List<Object> cacheIds = Lists.newArrayList(cacheIdCaptor.getValue());
    assertEquals(newArrayList("2", "3"), cacheIds);

    assertEquals(2, retrievedEntities.size());
    assertTrue(EntityUtils.equals(retrievedEntities.get(0), entities.get(1)));
    assertTrue(EntityUtils.equals(retrievedEntities.get(1), entities.get(3)));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAllQueriesInBatches() {
    // repository is clean
    when(transactionInformation.isEntireRepositoryDirty(emd)).thenReturn(false);

    // 2500 IDs make for 3 batches.
    // one third is dirty and needs to be queried from the decorated repository
    when(transactionInformation.isEntityDirty(any(EntityKey.class)))
        .thenAnswer(
            invocation ->
                Integer.parseInt(((EntityKey) invocation.getArguments()[0]).getId().toString()) % 3
                    == 0);

    List<Entity> lotsOfEntities = entityTestHarness.createTestRefEntities(emd, NUMBER_OF_ENTITIES);

    Stream<Object> ids = IntStream.range(0, NUMBER_OF_ENTITIES).mapToObj(Integer::toString);

    when(l2Cache.getBatch(eq(delegateRepository), any(Iterable.class)))
        .thenAnswer(
            invocation -> {
              List<Object> queried =
                  Lists.newArrayList((Iterable<Object>) invocation.getArguments()[1]);
              return Lists.transform(
                  queried, id -> lotsOfEntities.get(Integer.parseInt(id.toString())));
            });
    when(delegateRepository.findAll(any(Stream.class)))
        .thenAnswer(
            invocation -> {
              List<Object> queried =
                  ((Stream<Object>) invocation.getArguments()[0]).collect(toList());
              return Lists.transform(
                  queried, id -> lotsOfEntities.get(Integer.parseInt(id.toString())))
                  .stream();
            });

    List<Entity> retrievedEntities = l2CacheRepositoryDecorator.findAll(ids).collect(toList());

    assertEquals(NUMBER_OF_ENTITIES, retrievedEntities.size());
    for (int i = 0; i < NUMBER_OF_ENTITIES; i++) {
      assertTrue(EntityUtils.equals(retrievedEntities.get(i), lotsOfEntities.get(i)));
    }
  }
}
