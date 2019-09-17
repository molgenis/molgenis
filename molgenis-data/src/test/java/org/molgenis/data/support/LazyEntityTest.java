package org.molgenis.data.support;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class LazyEntityTest {
  private static final String ENTITY_NAME = "entity";
  private static final String ID_ATTR_NAME = "id";

  private EntityType entityType;
  private Attribute idAttr;

  private DataService dataService;
  private Object id;
  private LazyEntity lazyEntity;
  private Entity entity;

  @BeforeEach
  void setUpBeforeMethod() {
    entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(ENTITY_NAME);
    idAttr = mock(Attribute.class);
    when(idAttr.getName()).thenReturn(ID_ATTR_NAME);
    when(entityType.getIdAttribute()).thenReturn(idAttr);
    dataService = mock(DataService.class);
    entity = mock(Entity.class);
    id = 1;
    when(dataService.findOneById(ENTITY_NAME, id)).thenReturn(entity);
    lazyEntity = new LazyEntity(entityType, dataService, id);
  }

  @Test
  void LazyEntity() {
    assertThrows(NullPointerException.class, () -> new LazyEntity(null, null, null));
  }

  @Test
  void get() {
    String attrName = "attr";
    Object value = mock(Object.class);
    when(entity.get(attrName)).thenReturn(value);
    assertEquals(lazyEntity.get(attrName), value);
    assertEquals(lazyEntity.get(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getIdAttr() {
    assertEquals(lazyEntity.get(ID_ATTR_NAME), id);
  }

  @Test
  void getAttributeNames() {
    Entity entity = new DynamicEntity(entityType);
    Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
    Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
    when(entityType.getAtomicAttributes()).thenReturn(Arrays.asList(attr0, attr1));
    assertEquals(asList("attr0", "attr1"), newArrayList(entity.getAttributeNames()));
  }

  @Test
  void getBoolean() {
    String attrName = "attr";
    Boolean value = Boolean.TRUE;
    when(entity.getBoolean(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getBoolean(attrName), value);
    assertEquals(lazyEntity.getBoolean(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getDouble() {
    String attrName = "attr";
    Double value = 0d;
    when(entity.getDouble(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getDouble(attrName), value);
    assertEquals(lazyEntity.getDouble(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getEntitiesString() {
    String attrName = "attr";
    @SuppressWarnings("unchecked")
    Iterable<Entity> entities = mock(Iterable.class);
    when(entity.getEntities(attrName)).thenReturn(entities);
    assertEquals(lazyEntity.getEntities(attrName), entities);
    assertEquals(lazyEntity.getEntities(attrName), entities);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getEntitiesStringClassE() {
    String attrName = "attr";
    @SuppressWarnings("unchecked")
    Iterable<Entity> entities = mock(Iterable.class);
    when(entity.getEntities(attrName, Entity.class)).thenReturn(entities);
    assertEquals(lazyEntity.getEntities(attrName, Entity.class), entities);
    assertEquals(lazyEntity.getEntities(attrName, Entity.class), entities);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getEntityString() {
    String attrName = "attr";
    Entity value = mock(Entity.class);
    when(entity.getEntity(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getEntity(attrName), value);
    assertEquals(lazyEntity.getEntity(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getEntityStringClassE() {
    String attrName = "attr";
    Entity value = mock(Entity.class);
    when(entity.getEntity(attrName, Entity.class)).thenReturn(value);
    assertEquals(lazyEntity.getEntity(attrName, Entity.class), value);
    assertEquals(lazyEntity.getEntity(attrName, Entity.class), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getEntityType() {
    assertEquals(lazyEntity.getEntityType(), entityType);
  }

  @Test
  void getIdValue() {
    assertEquals(lazyEntity.getIdValue(), id);
  }

  @Test
  void getInt() {
    String attrName = "attr";
    Integer value = 0;
    when(entity.getInt(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getInt(attrName), value);
    assertEquals(lazyEntity.getInt(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getIntIdAttr() {
    assertEquals(lazyEntity.getInt(ID_ATTR_NAME), id);
  }

  @Test
  void getLabelValue() {
    String value = "label";
    when(entity.getLabelValue()).thenReturn(value);
    assertEquals(lazyEntity.getLabelValue(), value);
    assertEquals(lazyEntity.getLabelValue(), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getLabelValueLabelAttrIsIdAttr() {
    when(entityType.getLabelAttribute()).thenReturn(idAttr);
    assertEquals(lazyEntity.getLabelValue().toString(), id.toString());
    verifyNoMoreInteractions(dataService);
  }

  @Test
  void getLong() {
    String attrName = "attr";
    Long value = 0L;
    when(entity.getLong(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getLong(attrName), value);
    assertEquals(lazyEntity.getLong(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getString() {
    String attrName = "attr";
    String value = "str";
    when(entity.getString(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getString(attrName), value);
    assertEquals(lazyEntity.getString(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getStringIdAttr() {
    String strId = "1";
    when(dataService.findOneById(ENTITY_NAME, strId)).thenReturn(entity);
    lazyEntity = new LazyEntity(entityType, dataService, strId);
    assertEquals(lazyEntity.getString(ID_ATTR_NAME), strId);
  }

  @Test
  void getLocalDate() {
    String attrName = "attr";
    LocalDate value = LocalDate.now();
    when(entity.getLocalDate(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getLocalDate(attrName), value);
    assertEquals(lazyEntity.getLocalDate(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void getInstant() {
    String attrName = "attr";
    Instant value = Instant.now();
    when(entity.getInstant(attrName)).thenReturn(value);
    assertEquals(lazyEntity.getInstant(attrName), value);
    assertEquals(lazyEntity.getInstant(attrName), value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
  }

  @Test
  void setStringObject() {
    String attrName = "attr";
    Object value = mock(Object.class);
    lazyEntity.set(attrName, value);
    lazyEntity.set(attrName, value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
    verify(entity, times(2)).set(attrName, value);
  }

  @Test
  void setEntity() {
    Entity value = mock(Entity.class);
    lazyEntity.set(value);
    lazyEntity.set(value);
    verify(dataService, times(1)).findOneById(ENTITY_NAME, id);
    verify(entity, times(2)).set(value);
  }
}
