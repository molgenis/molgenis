package org.molgenis.navigator.copy.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class PretendingEntityTest extends AbstractMockitoTest {

  @Test
  public void testPretendingEntityIsNotCopy() {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("oldId");
    when(entity.getEntityType()).thenReturn(entityType);

    PretendingEntity pretendingEntity = new PretendingEntity(entity, new HashMap<>());

    assertEquals(pretendingEntity.getEntityType(), entityType);
  }

  @Test
  public void testPretendingEntityIsCopy() {
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
  public void testSingleReferenceIsCopy() {
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
  public void testSingleReferenceIsNotCopy() {
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
  public void testMultiReferenceIsCopy() {
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
  public void testMultiReferenceIsNotCopy() {
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
}
