package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class PartialEntityTest {
  private PartialEntity partialEntity;
  private Entity originalEntity;
  private Entity decoratedEntity;
  private Fetch fetch;
  private EntityManager entityManager;
  private EntityType meta;

  @BeforeEach
  void setUpBeforeMethod() {
    Attribute idAttr = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
    meta = when(mock(EntityType.class).getId()).thenReturn("entity").getMock();
    when(meta.getIdAttribute()).thenReturn(idAttr);

    originalEntity = mock(Entity.class);

    decoratedEntity = mock(Entity.class);
    when(decoratedEntity.getEntityType()).thenReturn(meta);
    when(decoratedEntity.getIdValue()).thenReturn("id");

    fetch = new Fetch().field("id");
    entityManager = mock(EntityManager.class);
    when(entityManager.getReference(meta, "id")).thenReturn(originalEntity);
    partialEntity = new PartialEntity(decoratedEntity, fetch, entityManager);
  }

  @Test
  void get() {
    partialEntity.get("id");
    verify(decoratedEntity, times(1)).get("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getNotInFetch() {
    partialEntity.get("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getBoolean() {
    partialEntity.getBoolean("id");
    verify(decoratedEntity, times(1)).getBoolean("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getBooleanNotInFetch() {
    partialEntity.getBoolean("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getDateNotInFetch() {
    partialEntity.getLocalDate("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getDouble() {
    partialEntity.getDouble("id");
    verify(decoratedEntity, times(1)).getDouble("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getDoubleNotInFetch() {
    partialEntity.getDouble("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getEntitiesString() {
    partialEntity.getEntities("id");
    verify(decoratedEntity, times(1)).getEntities("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getEntitiesStringNotInFetch() {
    partialEntity.getEntities("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getEntitiesStringClassE() {
    partialEntity.getEntities("id", Entity.class);
    verify(decoratedEntity, times(1)).getEntities("id", Entity.class);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getEntitiesStringClassENotInFetch() {
    partialEntity.getEntities("label", Entity.class);
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getEntityString() {
    partialEntity.getEntity("id");
    verify(decoratedEntity, times(1)).getEntity("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getEntityStringNotInFetch() {
    partialEntity.getEntity("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getEntityStringClassE() {
    partialEntity.getEntity("id", Entity.class);
    verify(decoratedEntity, times(1)).getEntity("id", Entity.class);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getEntityStringClassENotInFetch() {
    partialEntity.getEntity("label", Entity.class);
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getInt() {
    partialEntity.getInt("id");
    verify(decoratedEntity, times(1)).getInt("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getIntNotInFetch() {
    partialEntity.getInt("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getLong() {
    partialEntity.getLong("id");
    verify(decoratedEntity, times(1)).getLong("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getLongNotInFetch() {
    partialEntity.getLong("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getString() {
    partialEntity.getString("id");
    verify(decoratedEntity, times(1)).getString("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getStringNotInFetch() {
    partialEntity.getString("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getTimestamp() {
    partialEntity.getLocalDate("id");
    verify(decoratedEntity, times(1)).getLocalDate("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getTimestampNotInFetch() {
    partialEntity.getLocalDate("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void getUtilDate() {
    partialEntity.getInstant("id");
    verify(decoratedEntity, times(1)).getInstant("id");
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getUtilDateNotInFetch() {
    partialEntity.getInstant("label");
    verify(entityManager, times(1)).getReference(meta, "id");
  }

  @Test
  void setStringObject() {
    Object obj = mock(Object.class);
    partialEntity.set("test", obj);
    verify(decoratedEntity, times(1)).set("test", obj);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void setEntity() {
    Entity e = mock(Entity.class);
    partialEntity.set(e);
    verify(decoratedEntity, times(1)).set(e);
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getAttributeNames() {
    partialEntity.getAttributeNames();
    verify(decoratedEntity, times(1)).getAttributeNames();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getEntityType() {
    partialEntity.getEntityType();
    verify(decoratedEntity, times(1)).getEntityType();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getIdValue() {
    partialEntity.getIdValue();
    verify(decoratedEntity, times(1)).getIdValue();
    verifyZeroInteractions(entityManager);
  }

  @Test
  void getLabelValue() {
    partialEntity.getLabelValue();
    verify(decoratedEntity, times(1)).getLabelValue();
    verifyZeroInteractions(entityManager);
  }
}
