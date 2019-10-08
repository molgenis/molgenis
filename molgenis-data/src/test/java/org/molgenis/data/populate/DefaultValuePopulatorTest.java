package org.molgenis.data.populate;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityReferenceCreator;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class DefaultValuePopulatorTest {
  private static Entity entity1;
  private static Entity entityA;
  private static Entity entityB;

  private DefaultValuePopulator defaultValuePopulator;

  @BeforeAll
  static void setUpBeforeClass() {
    entity1 = mock(Entity.class);
    entityA = mock(Entity.class);
    entityB = mock(Entity.class);
  }

  @BeforeEach
  void setUpBeforeMethod() {
    EntityReferenceCreator entityReferenceCreator = mock(EntityReferenceCreator.class);
    when(entityReferenceCreator.getReference(any(EntityType.class), eq(1))).thenReturn(entity1);
    when(entityReferenceCreator.getReference(any(EntityType.class), eq("a"))).thenReturn(entityA);
    when(entityReferenceCreator.getReference(any(EntityType.class), eq("b"))).thenReturn(entityB);
    when(entityReferenceCreator.getReferences(any(EntityType.class), eq(Arrays.asList("a", "b"))))
        .thenReturn(asList(entityA, entityB));
    this.defaultValuePopulator = new DefaultValuePopulator(entityReferenceCreator);
  }

  static Iterator<Object[]> testPopulateProvider() {
    List<Object[]> populationData = new ArrayList<>(20);
    populationData.add(new Object[] {createEntity(BOOL, "true"), true});
    populationData.add(new Object[] {createEntity(BOOL, "false"), false});
    populationData.add(new Object[] {createEntity(CATEGORICAL, "1"), entity1});
    populationData.add(
        new Object[] {createEntity(CATEGORICAL_MREF, "a,b"), asList(entityA, entityB)});
    populationData.add(
        new Object[] {createEntity(DATE, "2016-11-30"), LocalDate.parse("2016-11-30")});
    populationData.add(
        new Object[] {
          createEntity(DATE_TIME, "2016-10-10T12:00:10+0000"), Instant.parse("2016-10-10T12:00:10Z")
        });
    populationData.add(new Object[] {createEntity(DECIMAL, "1.23"), 1.23});
    populationData.add(
        new Object[] {createEntity(EMAIL, "mail@molgenis.org"), "mail@molgenis.org"});
    populationData.add(new Object[] {createEntity(ENUM, "enum0"), "enum0"});
    populationData.add(new Object[] {createEntity(FILE, "1"), entity1});
    populationData.add(new Object[] {createEntity(HTML, "<h1>text</h1>"), "<h1>text</h1>"});
    populationData.add(
        new Object[] {createEntity(HYPERLINK, "http://test.nl/"), "http://test.nl/"});
    populationData.add(new Object[] {createEntity(INT, "123"), 123});
    populationData.add(new Object[] {createEntity(LONG, "1099511627776"), 1099511627776L});
    populationData.add(new Object[] {createEntity(MREF, "a,b"), asList(entityA, entityB)});
    populationData.add(new Object[] {createEntity(ONE_TO_MANY, "a,b"), asList(entityA, entityB)});
    populationData.add(new Object[] {createEntity(SCRIPT, "script"), "script"});
    populationData.add(new Object[] {createEntity(STRING, "str"), "str"});
    populationData.add(new Object[] {createEntity(TEXT, "text"), "text"});
    populationData.add(new Object[] {createEntity(XREF, "1"), entity1});
    return populationData.iterator();
  }

  private static Entity createEntity(AttributeType attrType, String defaultValue) {
    EntityType entityType = mock(EntityType.class);
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn("attr");
    when(attr.getDataType()).thenReturn(attrType);
    when(attr.hasDefaultValue()).thenReturn(true);
    when(attr.getDefaultValue()).thenReturn(defaultValue);
    when(entityType.getAllAttributes()).thenReturn(singleton(attr));
    if (attrType == CATEGORICAL || attrType == XREF || attrType == FILE) {
      Attribute intIdAttr = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
      EntityType refEntityType =
          when(mock(EntityType.class).getIdAttribute()).thenReturn(intIdAttr).getMock();
      when(attr.getRefEntity()).thenReturn(refEntityType);
    } else if (attrType == CATEGORICAL_MREF || attrType == MREF || attrType == ONE_TO_MANY) {
      Attribute intStrAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
      EntityType refEntityType =
          when(mock(EntityType.class).getIdAttribute()).thenReturn(intStrAttr).getMock();
      when(attr.getRefEntity()).thenReturn(refEntityType);
    }

    Entity entity = when(mock(Entity.class).getEntityType()).thenReturn(entityType).getMock();
    when(entity.toString()).thenReturn(attrType.toString());
    return entity;
  }

  @ParameterizedTest
  @MethodSource("testPopulateProvider")
  void testPopulate(Entity entity, Object expectedValue) {
    defaultValuePopulator.populate(entity);
    verify(entity).set("attr", expectedValue);
  }
}
