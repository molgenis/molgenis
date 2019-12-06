package org.molgenis.data.cache.l1;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class L1CacheRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Entity> delegateRepository;
  @Mock private L1Cache l1Cache;
  private L1CacheRepositoryDecorator l1CacheRepositoryDecorator;

  @BeforeEach
  public void setUpBeforeEach() {
    when(delegateRepository.getCapabilities()).thenReturn(singleton(CACHEABLE));
    l1CacheRepositoryDecorator = new L1CacheRepositoryDecorator(delegateRepository, l1Cache);
  }

  @Test
  void testAdd() {
    Entity entity = mock(Entity.class);
    l1CacheRepositoryDecorator.add(entity);
    verify(l1Cache).put(entity);
    verify(delegateRepository).add(entity);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAddStream() {
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    doAnswer(invocation -> (int) ((Stream<?>) invocation.getArguments()[0]).count())
        .when(delegateRepository)
        .add(any(Stream.class));
    assertEquals(2, l1CacheRepositoryDecorator.add(Stream.of(entity0, entity1)));
    verify(l1Cache).put(entity0);
    verify(l1Cache).put(entity1);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testFindOneById() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = mock(Object.class);
    Entity entity = mock(Entity.class);
    when(l1Cache.get(entityType, entityId, null)).thenReturn(Optional.of(CacheHit.of(entity)));
    assertEquals(entity, l1CacheRepositoryDecorator.findOneById(entityId));
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testFindOneByIdCacheMiss() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = mock(Object.class);
    Entity entity = mock(Entity.class);
    when(l1Cache.get(entityType, entityId, null)).thenReturn(Optional.empty());
    when(delegateRepository.findOneById(entityId)).thenReturn(entity);
    assertEquals(entity, l1CacheRepositoryDecorator.findOneById(entityId));
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testFindOneByIdCacheNull() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);
    Object entityId = mock(Object.class);
    when(l1Cache.get(entityType, entityId, null)).thenReturn(Optional.of(CacheHit.empty()));
    assertNull(l1CacheRepositoryDecorator.findOneById(entityId));
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testFindOneByIdFetch() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);

    Object entityId = mock(Object.class);
    Fetch fetch = mock(Fetch.class);
    Entity entity = mock(Entity.class);
    when(l1Cache.get(entityType, entityId, fetch)).thenReturn(Optional.of(CacheHit.of(entity)));
    assertEquals(entity, l1CacheRepositoryDecorator.findOneById(entityId, fetch));
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAll() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);

    Object entityId0 = mock(Object.class);
    Object entityId1 = mock(Object.class);
    Object entityId2 = mock(Object.class);
    Object entityId3 = mock(Object.class);
    Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(entityId0).getMock();
    Entity entity1 = mock(Entity.class);
    Entity entity3 = when(mock(Entity.class).getIdValue()).thenReturn(entityId3).getMock();

    when(l1Cache.get(entityType, entityId0, null)).thenReturn(Optional.empty());
    when(l1Cache.get(entityType, entityId1, null)).thenReturn(Optional.of(CacheHit.of(entity1)));
    when(l1Cache.get(entityType, entityId2, null)).thenReturn(Optional.of(CacheHit.empty()));
    when(l1Cache.get(entityType, entityId3, null)).thenReturn(Optional.empty());

    doAnswer(invocation -> Stream.of(entity0, entity3))
        .when(delegateRepository)
        .findAll(any(Stream.class));
    assertEquals(
        asList(entity0, entity1, entity3),
        l1CacheRepositoryDecorator
            .findAll(Stream.of(entityId0, entityId1, entityId2, entityId3))
            .collect(Collectors.toList()));
    ArgumentCaptor<Stream<Object>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).findAll(entityStreamCaptor.capture());
    assertEquals(asList(entityId0, entityId3), entityStreamCaptor.getValue().collect(toList()));
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testFindAllFetch() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);

    Object entityId0 = mock(Object.class);
    Object entityId1 = mock(Object.class);
    Object entityId2 = mock(Object.class);
    Object entityId3 = mock(Object.class);
    Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(entityId0).getMock();
    Entity entity1 = mock(Entity.class);
    Entity entity3 = when(mock(Entity.class).getIdValue()).thenReturn(entityId3).getMock();

    Fetch fetch = mock(Fetch.class);
    when(l1Cache.get(entityType, entityId0, fetch)).thenReturn(Optional.empty());
    when(l1Cache.get(entityType, entityId1, fetch)).thenReturn(Optional.of(CacheHit.of(entity1)));
    when(l1Cache.get(entityType, entityId2, fetch)).thenReturn(Optional.of(CacheHit.empty()));
    when(l1Cache.get(entityType, entityId3, fetch)).thenReturn(Optional.empty());

    doAnswer(invocation -> Stream.of(entity0, entity3))
        .when(delegateRepository)
        .findAll(any(Stream.class), eq(fetch));
    assertEquals(
        asList(entity0, entity1, entity3),
        l1CacheRepositoryDecorator
            .findAll(Stream.of(entityId0, entityId1, entityId2, entityId3), fetch)
            .collect(Collectors.toList()));
    ArgumentCaptor<Stream<Object>> entityStreamCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).findAll(entityStreamCaptor.capture(), eq(fetch));
    assertEquals(asList(entityId0, entityId3), entityStreamCaptor.getValue().collect(toList()));
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testUpdate() {
    Entity entity = mock(Entity.class);
    l1CacheRepositoryDecorator.update(entity);
    verify(l1Cache).put(entity);
    verify(delegateRepository).update(entity);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testUpdateStream() {
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    doAnswer(invocation -> ((Stream<?>) invocation.getArguments()[0]).count())
        .when(delegateRepository)
        .update(any(Stream.class));
    l1CacheRepositoryDecorator.update(Stream.of(entity0, entity1));
    verify(l1Cache).put(entity0);
    verify(l1Cache).put(entity1);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testDelete() {
    Entity entity = mock(Entity.class);
    l1CacheRepositoryDecorator.delete(entity);
    verify(l1Cache).putDeletion(entity);
    verify(delegateRepository).delete(entity);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteStream() {
    Entity entity0 = mock(Entity.class);
    Entity entity1 = mock(Entity.class);
    doAnswer(invocation -> ((Stream<?>) invocation.getArguments()[0]).count())
        .when(delegateRepository)
        .delete(any(Stream.class));
    l1CacheRepositoryDecorator.delete(Stream.of(entity0, entity1));
    verify(l1Cache).putDeletion(entity0);
    verify(l1Cache).putDeletion(entity1);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testDeleteById() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);

    Object entityId = mock(Object.class);
    l1CacheRepositoryDecorator.deleteById(entityId);
    verify(l1Cache).putDeletion(entityType, entityId);
    verify(delegateRepository).deleteById(entityId);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDeleteAllStream() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);

    Object entityId0 = mock(Object.class);
    Object entityId1 = mock(Object.class);
    doAnswer(invocation -> ((Stream<?>) invocation.getArguments()[0]).count())
        .when(delegateRepository)
        .deleteAll(any(Stream.class));
    l1CacheRepositoryDecorator.deleteAll(Stream.of(entityId0, entityId1));
    verify(l1Cache).putDeletion(entityType, entityId0);
    verify(l1Cache).putDeletion(entityType, entityId1);
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }

  @Test
  void testDeleteAll() {
    EntityType entityType = mock(EntityType.class);
    when(delegateRepository.getEntityType()).thenReturn(entityType);

    l1CacheRepositoryDecorator.deleteAll();
    verify(l1Cache).evictAll(entityType);
    verify(delegateRepository).deleteAll();
    verifyNoMoreInteractions(l1Cache, delegateRepository);
  }
}
