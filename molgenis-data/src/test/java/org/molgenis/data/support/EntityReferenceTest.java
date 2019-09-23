package org.molgenis.data.support;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class EntityReferenceTest {
  private static final String ID_ATTR_NAME = "idAttr";
  private static final String LABEL_ATTR_NAME = "labelAttr";

  private EntityType entityType;
  private Attribute idAttribute;
  private EntityReference entityReference;

  @BeforeEach
  void setUpBeforeMethod() {
    entityType = mock(EntityType.class);
    when(entityType.getAttributeNames()).thenReturn(asList(ID_ATTR_NAME, LABEL_ATTR_NAME));

    idAttribute = when(mock(Attribute.class).getName()).thenReturn(ID_ATTR_NAME).getMock();
    when(idAttribute.getDataType()).thenReturn(AttributeType.STRING);
    Attribute labelAttribute =
        when(mock(Attribute.class).getName()).thenReturn(LABEL_ATTR_NAME).getMock();
    when(labelAttribute.getDataType()).thenReturn(AttributeType.STRING);
    when(entityType.getIdAttribute()).thenReturn(idAttribute);
    when(entityType.getLabelAttribute()).thenReturn(labelAttribute);
    when(entityType.getAtomicAttributes()).thenReturn(asList(idAttribute, labelAttribute));
    entityReference = new EntityReference(entityType, "entityId");
  }

  @Test
  void testEntityReference() {
    assertThrows(NullPointerException.class, () -> new EntityReference(null, null));
  }

  @Test
  void testEntityReferenceInvalidIdTrue() {
    assertThrows(MolgenisDataException.class, () -> new EntityReference(entityType, true));
  }

  @Test
  void testGetEntityType() {
    assertEquals(entityType, entityReference.getEntityType());
  }

  @Test
  void testGetAttributeNames() {
    assertEquals(
        asList(ID_ATTR_NAME, LABEL_ATTR_NAME), newArrayList(entityReference.getAttributeNames()));
  }

  @Test
  void testGetIdValue() {
    assertEquals("entityId", entityReference.getIdValue());
  }

  @Test
  void testSetIdValue() {
    entityReference.setIdValue("newEntityId");
    assertEquals("newEntityId", entityReference.getIdValue());
  }

  @Test
  void testSetIdValueStringWrongType() {
    assertThrows(MolgenisDataException.class, () -> entityReference.setIdValue(123));
  }

  @Test
  void testSetIdValueIntegerWrongType() {
    when(idAttribute.getDataType()).thenReturn(AttributeType.INT);
    assertThrows(MolgenisDataException.class, () -> entityReference.setIdValue(34359738368L));
  }

  @Test
  void testSetIdValueLongWrongType() {
    when(idAttribute.getDataType()).thenReturn(AttributeType.LONG);
    assertThrows(MolgenisDataException.class, () -> entityReference.setIdValue("123"));
  }

  @Test
  void testGetLabelValue() {
    Attribute labelAttribute =
        when(mock(Attribute.class).getName()).thenReturn(LABEL_ATTR_NAME).getMock();
    when(entityType.getLabelAttribute()).thenReturn(labelAttribute);
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getLabelValue());
  }

  @Test
  void testGetLabelValueLabelAttributeIsIdAttribute() {
    when(entityType.getLabelAttribute()).thenReturn(idAttribute);
    assertEquals("entityId", entityReference.getLabelValue());
  }

  @Test
  void testGet() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.get(LABEL_ATTR_NAME));
  }

  @Test
  void testGetIdAttribute() {
    assertEquals("entityId", entityReference.get(ID_ATTR_NAME));
  }

  @Test
  void testGetString() {
    assertThrows(
        UnsupportedOperationException.class, () -> entityReference.getString(LABEL_ATTR_NAME));
  }

  @Test
  void testGetStringIdAttribute() {
    assertEquals("entityId", entityReference.get(ID_ATTR_NAME));
  }

  @Test
  void testGetInt() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getInt("someAttr"));
  }

  @Test
  void testGetIntIdAttribute() {
    when(idAttribute.getDataType()).thenReturn(AttributeType.INT);
    EntityReference entityReference = new EntityReference(entityType, 123);
    assertEquals(valueOf(123), entityReference.getInt(ID_ATTR_NAME));
  }

  @Test
  void testGetLong() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getLong("someAttr"));
  }

  @Test
  void testGetLongIdAttribute() {
    when(idAttribute.getDataType()).thenReturn(AttributeType.LONG);
    EntityReference entityReference = new EntityReference(entityType, 123L);
    assertEquals(Long.valueOf(123L), entityReference.getLong(ID_ATTR_NAME));
  }

  @Test
  void testGetBoolean() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getBoolean("someAttr"));
  }

  @Test
  void testGetDouble() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getDouble("someAttr"));
  }

  @Test
  void testGetInstant() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getInstant("someAttr"));
  }

  @Test
  void testGetLocalDate() {
    assertThrows(
        UnsupportedOperationException.class, () -> entityReference.getLocalDate("someAttr"));
  }

  @Test
  void testGetEntity() {
    assertThrows(UnsupportedOperationException.class, () -> entityReference.getEntity("someAttr"));
  }

  @Test
  void testGetEntityClass() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> entityReference.getEntity("someAttr", Entity.class));
  }

  @Test
  void testGetEntities() {
    assertThrows(
        UnsupportedOperationException.class, () -> entityReference.getEntities("someAttr"));
  }

  @Test
  void testGetEntitiesClass() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> entityReference.getEntities("someAttr", Entity.class));
  }

  @Test
  void testSet() {
    assertThrows(
        UnsupportedOperationException.class, () -> entityReference.set("someAttr", "value"));
  }

  @Test
  void testSetIdAttribute() {
    entityReference.set(ID_ATTR_NAME, "newEntityId");
    assertEquals("newEntityId", entityReference.getIdValue());
  }

  @Test
  void testSetEntity() {
    Entity entity = mock(Entity.class);
    when(entity.get(ID_ATTR_NAME)).thenReturn("newEntityId");
    when(entity.getAttributeNames()).thenReturn(asList(ID_ATTR_NAME, LABEL_ATTR_NAME)).getMock();
    assertThrows(UnsupportedOperationException.class, () -> entityReference.set(entity));
  }

  @Test
  void testSetEntityIdAttribute() {
    Entity entity = mock(Entity.class);
    when(entity.getAttributeNames()).thenReturn(singletonList(ID_ATTR_NAME)).getMock();
    when(entity.get(ID_ATTR_NAME)).thenReturn("newEntityId");
    entityReference.set(entity);
    assertEquals("newEntityId", entityReference.getIdValue());
  }
}
