package org.molgenis.api.data.v3;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityManager.CreationMode;
import org.molgenis.data.InvalidAttributeValueException;
import org.molgenis.data.meta.IllegalAttributeTypeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class EntityManagerV3ImplTest extends AbstractMockitoTest {
  @Mock private EntityManager entityManager;
  private EntityManagerV3 entityManagerV3;

  @BeforeEach
  void setUpBeforeMethod() {
    entityManagerV3 = new EntityManagerV3Impl(entityManager);
  }

  @Test
  void testCreate() {
    EntityType entityType = mock(EntityType.class);
    entityManagerV3.create(entityType);

    verify(entityManager).create(entityType, CreationMode.POPULATE);
  }

  @Test
  void testPopulateString() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(STRING);
    when(attribute.getName()).thenReturn("string");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("string", "stringValue");

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("string", "stringValue");
  }

  @Test
  void testPopulateCompound() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(COMPOUND);
    when(attribute.getName()).thenReturn("string");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("string", "stringValue");

    assertThrows(
        IllegalAttributeTypeException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateLong() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(LONG);
    when(attribute.getName()).thenReturn("long");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("long", 15.0);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("long", 15L);
  }

  @Test
  void testPopulateLongInvalid() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(LONG);
    when(attribute.getName()).thenReturn("long");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("long", "test");

    assertThrows(
        InvalidAttributeValueException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateInt() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(INT);
    when(attribute.getName()).thenReturn("int");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("int", 15.0);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("int", 15);
  }

  @Test
  void testPopulateIntNull() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(INT);
    when(attribute.getName()).thenReturn("int");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("int", null);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("int", null);
  }

  @Test
  void testPopulateIntInvalid() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(INT);
    when(attribute.getName()).thenReturn("int");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("int", "15.0");

    assertThrows(
        InvalidAttributeValueException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateDecimalDouble() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DECIMAL);
    when(attribute.getName()).thenReturn("decimal");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("decimal", 15.0);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("decimal", 15.0);
  }

  @Test
  void testPopulateDecimalNumber() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DECIMAL);
    when(attribute.getName()).thenReturn("decimal");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("decimal", 15);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("decimal", 15.0);
  }

  @Test
  void testPopulateDecimalNull() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DECIMAL);
    when(attribute.getName()).thenReturn("decimal");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("decimal", null);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("decimal", null);
  }

  @Test
  void testPopulateDecimalInvalid() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DECIMAL);
    when(attribute.getName()).thenReturn("decimal");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("decimal", "15.0");

    assertThrows(
        InvalidAttributeValueException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateDate() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DATE);
    when(attribute.getName()).thenReturn("date");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("date", "2001-01-01");

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("date", LocalDate.parse("2001-01-01"));
  }

  @Test
  void testPopulateDateNull() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DATE);
    when(attribute.getName()).thenReturn("date");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("date", null);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("date", null);
  }

  @Test
  void testPopulateDateInvalid() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DATE);
    when(attribute.getName()).thenReturn("date");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("date", 1);

    assertThrows(
        InvalidAttributeValueException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateDateTime() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DATE_TIME);
    when(attribute.getName()).thenReturn("datetime");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("datetime", "2000-12-31T10:34:56.789Z");

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("datetime", Instant.parse("2000-12-31T10:34:56.789Z"));
  }

  @Test
  void testPopulateDateTimeNull() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DATE_TIME);
    when(attribute.getName()).thenReturn("datetime");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("datetime", null);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("datetime", null);
  }

  @Test
  void testPopulateDateTimeInvalid() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(DATE_TIME);
    when(attribute.getName()).thenReturn("datetime");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("datetime", 1);

    assertThrows(
        InvalidAttributeValueException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateXref() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    Entity refEntity = mock(Entity.class);
    EntityType refEntityType = mock(EntityType.class);
    Attribute refAttribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(XREF);
    when(attribute.getName()).thenReturn("xref");
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(refEntityType.getIdAttribute()).thenReturn(refAttribute);
    when(refAttribute.getDataType()).thenReturn(INT);

    when(entityManager.getReference(refEntityType, 1)).thenReturn(refEntity);

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("xref", 1);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("xref", refEntity);
  }

  @Test
  void testPopulateXrefNull() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(XREF);
    when(attribute.getName()).thenReturn("xref");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("xref", null);

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("xref", null);
  }

  @Test
  void testPopulateMref() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    Entity refEntity1 = mock(Entity.class);
    Entity refEntity2 = mock(Entity.class);
    Entity refEntity3 = mock(Entity.class);
    EntityType refEntityType = mock(EntityType.class);
    Attribute refAttribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(MREF);
    when(attribute.getName()).thenReturn("mref");
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(refEntityType.getIdAttribute()).thenReturn(refAttribute);

    // 'any()' because the getReferences gets passed a lambda
    when(entityManager.getReferences(eq(refEntityType), any()))
        .thenReturn(Arrays.asList(refEntity1, refEntity2, refEntity3));

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("mref", Arrays.asList(1, 2, 3));

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("mref", Arrays.asList(refEntity1, refEntity2, refEntity3));
  }

  @Test
  void testPopulateMrefEmpty() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    EntityType refEntityType = mock(EntityType.class);
    Attribute refAttribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(MREF);
    when(attribute.getName()).thenReturn("mref");
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(refEntityType.getIdAttribute()).thenReturn(refAttribute);

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("mref", Collections.emptyList());

    entityManagerV3.populate(entityType, entity, valueMap);

    verify(entity).set("mref", Collections.emptyList());
  }

  @Test
  void testPopulateMrefNull() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(MREF);
    when(attribute.getName()).thenReturn("mref");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("mref", null);

    assertThrows(
        NullMrefValueException.class, () -> entityManagerV3.populate(entityType, entity, valueMap));
  }

  @Test
  void testPopulateMrefInvalid() {
    EntityType entityType = mock(EntityType.class);
    Entity entity = mock(Entity.class);
    Attribute attribute = mock(Attribute.class);

    when(attribute.hasExpression()).thenReturn(false);
    when(attribute.isMappedBy()).thenReturn(false);
    when(attribute.getDataType()).thenReturn(MREF);
    when(attribute.getName()).thenReturn("mref");

    when(entityType.getAtomicAttributes()).thenReturn(singletonList(attribute));

    Map<String, Object> valueMap = new HashMap<>();
    valueMap.put("mref", "invalid");

    assertThrows(
        InvalidAttributeValueException.class,
        () -> entityManagerV3.populate(entityType, entity, valueMap));
  }
}
