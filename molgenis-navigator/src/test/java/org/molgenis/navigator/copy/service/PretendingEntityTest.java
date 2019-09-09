package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.test.AbstractMockitoTest;

class PretendingEntityTest extends AbstractMockitoTest {

  @Test
  void testPretendingEntityIsNotCopy() {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("oldId");
    when(entity.getEntityType()).thenReturn(entityType);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, new HashMap<>());

    assertEquals(pretendingEntity.getEntityType(), entityType);
  }

  @Test
  void testPretendingEntityIsCopy() {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("oldId");
    EntityType entityTypeCopy = mock(EntityType.class);
    when(entity.getEntityType()).thenReturn(entityType);
    Map<String, EntityType> copiedEntityTypes = ImmutableMap.of("oldId", entityTypeCopy);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, copiedEntityTypes);

    assertEquals(pretendingEntity.getEntityType(), entityTypeCopy);
  }

  @Test
  void testSingleReferenceIsCopy() {
    Entity entity = mock(Entity.class);
    Entity refEntity = mock(Entity.class);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("oldId");
    EntityType refEntityTypeCopy = mock(EntityType.class);
    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(entity.getEntity("ref")).thenReturn(refEntity);
    Map<String, EntityType> copiedEntityTypes = ImmutableMap.of("oldId", refEntityTypeCopy);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, copiedEntityTypes);
    Entity actualRefEntity = pretendingEntity.getEntity("ref");

    assertEquals(actualRefEntity.getEntityType(), refEntityTypeCopy);
  }

  @Test
  void testSingleReferenceIsNotCopy() {
    Entity entity = mock(Entity.class);
    Entity refEntity = mock(Entity.class);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("oldId");
    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(entity.getEntity("ref")).thenReturn(refEntity);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, new HashMap<>());
    Entity actualRefEntity = pretendingEntity.getEntity("ref");

    assertEquals(actualRefEntity.getEntityType(), refEntityType);
  }

  @Test
  void testMultiReferenceIsCopy() {
    Entity entity = mock(Entity.class);
    Entity refEntity1 = mock(Entity.class);
    Entity refEntity2 = mock(Entity.class);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("oldId");
    EntityType refEntityTypeCopy = mock(EntityType.class);
    when(refEntity1.getEntityType()).thenReturn(refEntityType);
    when(refEntity2.getEntityType()).thenReturn(refEntityType);
    when(entity.getEntities("ref")).thenReturn(asList(refEntity1, refEntity2));
    Map<String, EntityType> copiedEntityTypes = ImmutableMap.of("oldId", refEntityTypeCopy);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, copiedEntityTypes);
    List<Entity> actualRefEntities = newArrayList(pretendingEntity.getEntities("ref"));

    assertEquals(actualRefEntities.get(0).getEntityType(), refEntityTypeCopy);
    assertEquals(actualRefEntities.get(1).getEntityType(), refEntityTypeCopy);
  }

  @Test
  void testMultiReferenceIsNotCopy() {
    Entity entity = mock(Entity.class);
    Entity refEntity1 = mock(Entity.class);
    Entity refEntity2 = mock(Entity.class);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("oldId");
    when(refEntity1.getEntityType()).thenReturn(refEntityType);
    when(refEntity2.getEntityType()).thenReturn(refEntityType);
    when(entity.getEntities("ref")).thenReturn(asList(refEntity1, refEntity2));

    PretendingEntity pretendingEntity = new PretendingEntity(entity, new HashMap<>());
    List<Entity> actualRefEntities = newArrayList(pretendingEntity.getEntities("ref"));

    assertEquals(actualRefEntities.get(0).getEntityType(), refEntityType);
    assertEquals(actualRefEntities.get(1).getEntityType(), refEntityType);
  }

  @Test
  void testTypedReferenceIsFileMeta() {
    Entity entity = mock(Entity.class);
    FileMeta refEntity = mock(FileMeta.class);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("oldId");
    EntityType refEntityTypeCopy = mock(EntityType.class);
    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(entity.getEntity("ref", FileMeta.class)).thenReturn(refEntity);
    Map<String, EntityType> copiedEntityTypes = ImmutableMap.of("oldId", refEntityTypeCopy);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, copiedEntityTypes);
    FileMeta actualRefEntity = pretendingEntity.getEntity("ref", FileMeta.class);

    assertEquals(actualRefEntity.getEntityType(), refEntityTypeCopy);
  }

  @Test
  void testTypedMultiReferenceFileMeta() {
    Entity entity = mock(Entity.class);
    FileMeta refEntity1 = mock(FileMeta.class);
    FileMeta refEntity2 = mock(FileMeta.class);
    EntityType refEntityType = mock(EntityType.class);
    when(refEntityType.getId()).thenReturn("oldId");
    EntityType refEntityTypeCopy = mock(EntityType.class);
    when(refEntity1.getEntityType()).thenReturn(refEntityType);
    when(refEntity2.getEntityType()).thenReturn(refEntityType);
    when(entity.getEntities("ref", FileMeta.class)).thenReturn(asList(refEntity1, refEntity2));
    Map<String, EntityType> copiedEntityTypes = ImmutableMap.of("oldId", refEntityTypeCopy);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, copiedEntityTypes);
    List<FileMeta> actualRefEntities =
        newArrayList(pretendingEntity.getEntities("ref", FileMeta.class));

    assertEquals(actualRefEntities.get(0).getEntityType(), refEntityTypeCopy);
    assertEquals(actualRefEntities.get(1).getEntityType(), refEntityTypeCopy);
  }

  @Test
  void testTypedReferenceIsNotFileMeta() {
    Entity entity = mock(Entity.class);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, new HashMap<>());
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> pretendingEntity.getEntity("ref", Plugin.class));
    assertThat(exception.getMessage()).containsPattern("Can't return typed pretending entities");
  }

  @Test
  void testTypedMultiReferenceNotFileMeta() {
    Entity entity = mock(Entity.class);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, new HashMap<>());
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> pretendingEntity.getEntities("ref", Plugin.class));
    assertThat(exception.getMessage()).containsPattern("Can't return typed pretending entities");
  }
}
