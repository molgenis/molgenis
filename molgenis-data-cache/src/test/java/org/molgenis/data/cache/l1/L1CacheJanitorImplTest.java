package org.molgenis.data.cache.l1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.cache.utils.CacheHit;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class L1CacheJanitorImplTest extends AbstractMockitoTest {
  @Mock private L1Cache l1Cache;
  private L1CacheJanitorImpl l1CacheJanitorImpl;

  @BeforeEach
  void setUpBeforeEach() {
    l1CacheJanitorImpl = new L1CacheJanitorImpl(l1Cache);
  }

  @Test
  void testCleanCacheBeforeAdd() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.empty());

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);

    l1CacheJanitorImpl.cleanCacheBeforeAdd(entity);
    verifyNoInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeAddInversedByNoRefEntityNull() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);

    l1CacheJanitorImpl.cleanCacheBeforeAdd(entity);
    verifyNoInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeAddInversedByRefEntitySelfReference() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getEntity(attribute)).thenReturn(entity);

    l1CacheJanitorImpl.cleanCacheBeforeAdd(entity);
    verifyNoInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeAddInversedByRefEntity() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();

    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn("MyEntityId").getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getEntity(attribute)).thenReturn(refEntity);

    l1CacheJanitorImpl.cleanCacheBeforeAdd(entity);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeAddStream() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.empty());

    Entity entity = mock(Entity.class);

    l1CacheJanitorImpl.cleanCacheBeforeAdd(entityType, Stream.of(entity));
    verifyNoInteractions(l1Cache);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testCleanCacheBeforeAddStreamInversedBy() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();

    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn("MyEntityId").getMock();
    when(entity.getEntity(attribute)).thenReturn(refEntity);

    l1CacheJanitorImpl.cleanCacheBeforeAdd(entityType, Stream.of(entity)).count();
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdate() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.empty());

    Entity entity = mock(Entity.class);
    when(entity.getEntityType()).thenReturn(entityType);

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verifyNoInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateInversedByNotCached() {
    EntityType refEntityType = mock(EntityType.class);
    Attribute attribute = mock(Attribute.class);
    when(attribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(entity.getEntityType()).thenReturn(entityType);

    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.empty());
    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verify(l1Cache).evictAll(refEntityType);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateInversedByCachedBeforeNullAfterNull() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(entity.getEntityType()).thenReturn(entityType);

    Entity currentEntity = mock(Entity.class);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(currentEntity)));

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateInversedByCachedBeforeNullAfterNotNull() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(entity.getEntityType()).thenReturn(entityType);

    Entity currentEntity = mock(Entity.class);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(currentEntity)));

    Entity refEntity = mock(Entity.class);
    when(entity.getEntity(attribute)).thenReturn(refEntity);
    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateInversedByCachedBeforeNotNullAfterNull() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(entity.getEntityType()).thenReturn(entityType);

    Entity currentEntity = mock(Entity.class);
    Entity refEntity = mock(Entity.class);
    when(currentEntity.getEntity(attribute)).thenReturn(refEntity);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(currentEntity)));

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateInversedByCachedBeforeNotNullAfterNotNullUnchanged() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(entity.getEntityType()).thenReturn(entityType);

    Entity currentEntity = mock(Entity.class);
    Entity refEntity = mock(Entity.class);
    when(entity.getEntity(attribute)).thenReturn(refEntity);
    when(currentEntity.getEntity(attribute)).thenReturn(refEntity);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(currentEntity)));

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateInversedByCachedBeforeNotNullAfterNotNullChanged() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);
    when(entity.getEntityType()).thenReturn(entityType);

    Entity currentEntity = mock(Entity.class);
    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();
    Entity currentRefEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("MyOtherRefEntityId").getMock();
    when(entity.getEntity(attribute)).thenReturn(refEntity);
    when(currentEntity.getEntity(attribute)).thenReturn(currentRefEntity);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(currentEntity)));

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entity);
    verify(l1Cache).evict(currentRefEntity);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeUpdateStream() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.empty());

    Entity entity = mock(Entity.class);

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entityType, Stream.of(entity));
    verifyNoInteractions(l1Cache);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testCleanCacheBeforeUpdateStreamInversedBy() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity entity = mock(Entity.class);
    Object entityId = mock(Object.class);
    when(entity.getIdValue()).thenReturn(entityId);

    Entity currentEntity = mock(Entity.class);
    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();
    Entity currentRefEntity =
        when(mock(Entity.class).getIdValue()).thenReturn("MyOtherRefEntityId").getMock();
    when(entity.getEntity(attribute)).thenReturn(refEntity);
    when(currentEntity.getEntity(attribute)).thenReturn(currentRefEntity);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(currentEntity)));

    l1CacheJanitorImpl.cleanCacheBeforeUpdate(entityType, Stream.of(entity)).count();
    verify(l1Cache).evict(currentRefEntity);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeDelete() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();

    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn("MyEntityId").getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getEntity(attribute)).thenReturn(refEntity);

    l1CacheJanitorImpl.cleanCacheBeforeDelete(entity);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testCleanCacheBeforeDeleteStream() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();

    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn("MyEntityId").getMock();
    when(entity.getEntity(attribute)).thenReturn(refEntity);

    l1CacheJanitorImpl.cleanCacheBeforeDelete(entityType, Stream.of(entity)).count();
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeDeleteById() {
    EntityType entityType = mock(EntityType.class);
    Object entityId = mock(Object.class);
    l1CacheJanitorImpl.cleanCacheBeforeDeleteById(entityType, entityId);
    verifyNoInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeDeleteByIdCached() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();

    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn("MyEntityId").getMock();
    when(entity.getEntity(attribute)).thenReturn(refEntity);

    Object entityId = mock(Object.class);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(entity)));

    l1CacheJanitorImpl.cleanCacheBeforeDeleteById(entityType, entityId);
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeDeleteByIdNotCached() {
    EntityType refEntityType = mock(EntityType.class);

    Attribute attribute = mock(Attribute.class);
    when(attribute.getRefEntity()).thenReturn(refEntityType);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Object entityId = mock(Object.class);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.empty());

    l1CacheJanitorImpl.cleanCacheBeforeDeleteById(entityType, entityId);
    verify(l1Cache).evictAll(refEntityType);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void testCleanCacheBeforeDeleteByIdStream() {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.empty());

    Entity entity = mock(Entity.class);

    l1CacheJanitorImpl.cleanCacheBeforeDeleteById(entityType, Stream.of(entity));
    verifyNoInteractions(l1Cache);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testCleanCacheBeforeDeleteByIdStreamInversedBy() {
    Attribute attribute = mock(Attribute.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getInversedByAttributes()).thenReturn(Stream.of(attribute));

    Entity refEntity = when(mock(Entity.class).getIdValue()).thenReturn("MyRefEntityId").getMock();

    Entity entity = when(mock(Entity.class).getIdValue()).thenReturn("MyEntityId").getMock();
    when(entity.getEntity(attribute)).thenReturn(refEntity);

    Object entityId = mock(Object.class);
    when(l1Cache.get(entityType, entityId)).thenReturn(Optional.of(CacheHit.of(entity)));

    l1CacheJanitorImpl.cleanCacheBeforeDeleteById(entityType, Stream.of(entityId)).count();
    verify(l1Cache).evict(refEntity);
    verifyNoMoreInteractions(l1Cache);
  }

  @Test
  void cleanCacheBeforeDeleteAll() {
    EntityType entityType = mock(EntityType.class);
    l1CacheJanitorImpl.cleanCacheBeforeDeleteAll(entityType);
    verifyNoInteractions(l1Cache);
  }
}
