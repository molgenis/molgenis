package org.molgenis.data.meta.model;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_AGGREGATABLE;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_AUTO;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_ID_ATTRIBUTE;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_NULLABLE;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_READ_ONLY;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_UNIQUE;
import static org.molgenis.data.meta.model.AttributeMetadata.IS_VISIBLE;
import static org.molgenis.data.meta.model.AttributeMetadata.PARENT;
import static org.molgenis.data.meta.model.AttributeMetadata.TYPE;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Sort;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      AttributeMetadata.class,
      AttributeFactory.class,
      MetadataTestConfig.class
    })
class AttributeTest extends AbstractSystemEntityTest {

  @Autowired AttributeMetadata metadata;
  @Autowired AttributeFactory factory;

  @Override
  protected List<String> getExcludedAttrs() {
    List<String> attrs = new ArrayList<>();
    attrs.add(AttributeMetadata.MAPPED_BY);
    attrs.add(AttributeMetadata.ENUM_OPTIONS);
    return attrs;
  }

  @Override
  protected Map<String, Pair<Class, Object>> getOverriddenReturnTypes() {
    Pair<Class, Object> orderPair = new Pair<>();
    orderPair.setA(Sort.class);
    orderPair.setB(Sort.parse("attr"));

    Map<String, Pair<Class, Object>> map = new HashMap<>();
    map.put(AttributeMetadata.ORDER_BY, orderPair);
    return map;
  }

  @SuppressWarnings("java:S2699") // Tests should include assertions
  @Test
  protected void testSystemEntity() {
    internalTestAttributes(
        metadata, Attribute.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }

  private Attribute attribute;

  @Test
  void setParentNullToAttribute() {
    EntityType entityType = createMockEntityType();

    attribute = new Attribute(entityType);

    Attribute parentAttribute = mock(Attribute.class);
    attribute.setParent(parentAttribute);
    verify(parentAttribute).addChild(attribute);
    verifyNoMoreInteractions(parentAttribute);
  }

  @Test
  void testIsReferenceTypeString() {
    EntityType entityType = createAttributeMetadataMock();
    Attribute attribute = new Attribute(entityType);
    attribute.setDataType(STRING);
    assertFalse(attribute.hasRefEntity());
  }

  @Test
  void testIsReferenceTypeXref() {
    EntityType entityType = createAttributeMetadataMock();
    attribute = new Attribute(entityType);

    attribute.setDataType(XREF);
    assertTrue(attribute.hasRefEntity());
  }

  private EntityType createAttributeMetadataMock() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute isNullableAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAutoAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isVisibleAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAggregatableAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isReadOnlyAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isUniqueAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    doReturn(typeAttr).when(entityType).getAttribute(TYPE);
    doReturn(isNullableAttr).when(entityType).getAttribute(IS_NULLABLE);
    doReturn(isAutoAttr).when(entityType).getAttribute(IS_AUTO);
    doReturn(isVisibleAttr).when(entityType).getAttribute(IS_VISIBLE);
    doReturn(isAggregatableAttr).when(entityType).getAttribute(IS_AGGREGATABLE);
    doReturn(isReadOnlyAttr).when(entityType).getAttribute(IS_READ_ONLY);
    doReturn(isUniqueAttr).when(entityType).getAttribute(IS_UNIQUE);
    return entityType;
  }

  @Test
  void setParentNullToNull() {
    EntityType entityType = createAttributeMetadataMock();
    attribute = new Attribute(entityType);

    attribute.setParent(null);
    assertNull(attribute.getParent());
  }

  @Test
  void setParentAttributeToNull() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute isNullableAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAutoAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isVisibleAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAggregatableAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isReadOnlyAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isUniqueAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute parentAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    doReturn(typeAttr).when(entityType).getAttribute(TYPE);
    doReturn(isNullableAttr).when(entityType).getAttribute(IS_NULLABLE);
    doReturn(isAutoAttr).when(entityType).getAttribute(IS_AUTO);
    doReturn(isVisibleAttr).when(entityType).getAttribute(IS_VISIBLE);
    doReturn(isAggregatableAttr).when(entityType).getAttribute(IS_AGGREGATABLE);
    doReturn(isReadOnlyAttr).when(entityType).getAttribute(IS_READ_ONLY);
    doReturn(isUniqueAttr).when(entityType).getAttribute(IS_UNIQUE);
    doReturn(parentAttribute).when(entityType).getAttribute(PARENT);

    attribute = new Attribute(entityType);

    Attribute parentAttr = mock(Attribute.class);
    attribute.setParent(parentAttr);
    verify(parentAttr).addChild(attribute);

    attribute.setParent(null);
    assertNull(attribute.getParent());
    verify(parentAttr).removeChild(attribute);
    verifyNoMoreInteractions(parentAttr);
  }

  @Test
  void setParentAttributeToAttribute() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute isNullableAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAutoAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isVisibleAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAggregatableAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isReadOnlyAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isUniqueAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute parentAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    doReturn(typeAttr).when(entityType).getAttribute(TYPE);
    doReturn(isNullableAttr).when(entityType).getAttribute(IS_NULLABLE);
    doReturn(isAutoAttr).when(entityType).getAttribute(IS_AUTO);
    doReturn(isVisibleAttr).when(entityType).getAttribute(IS_VISIBLE);
    doReturn(isAggregatableAttr).when(entityType).getAttribute(IS_AGGREGATABLE);
    doReturn(isReadOnlyAttr).when(entityType).getAttribute(IS_READ_ONLY);
    doReturn(isUniqueAttr).when(entityType).getAttribute(IS_UNIQUE);
    doReturn(parentAttribute).when(entityType).getAttribute(PARENT);

    attribute = new Attribute(entityType);

    Attribute currentParentAttr = mock(Attribute.class);
    attribute.setParent(currentParentAttr);
    verify(currentParentAttr).addChild(attribute);

    Attribute parentAttr = mock(Attribute.class);
    attribute.setParent(parentAttr);
    assertEquals(parentAttr, attribute.getParent());

    verify(currentParentAttr).removeChild(attribute);
    verifyNoMoreInteractions(currentParentAttr);
    verify(parentAttr).addChild(attribute);
    verifyNoMoreInteractions(parentAttr);
  }

  @Test
  void setIdAttributeTrue() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute isNullableAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAutoAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isVisibleAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAggregatableAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isReadOnlyAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isUniqueAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isIdAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    doReturn(typeAttr).when(entityType).getAttribute(TYPE);
    doReturn(isNullableAttr).when(entityType).getAttribute(IS_NULLABLE);
    doReturn(isAutoAttr).when(entityType).getAttribute(IS_AUTO);
    doReturn(isVisibleAttr).when(entityType).getAttribute(IS_VISIBLE);
    doReturn(isAggregatableAttr).when(entityType).getAttribute(IS_AGGREGATABLE);
    doReturn(isReadOnlyAttr).when(entityType).getAttribute(IS_READ_ONLY);
    doReturn(isUniqueAttr).when(entityType).getAttribute(IS_UNIQUE);
    doReturn(isIdAttr).when(entityType).getAttribute(IS_ID_ATTRIBUTE);

    attribute = new Attribute(entityType);
    attribute.setIdAttribute(true);
    assertTrue(attribute.isReadOnly());
    assertTrue(attribute.isUnique());
    assertFalse(attribute.isNillable());
  }

  @Test
  void setIdAttributeFalse() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute isNullableAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAutoAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isVisibleAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAggregatableAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isReadOnlyAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isUniqueAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isIdAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    doReturn(typeAttr).when(entityType).getAttribute(TYPE);
    doReturn(isNullableAttr).when(entityType).getAttribute(IS_NULLABLE);
    doReturn(isAutoAttr).when(entityType).getAttribute(IS_AUTO);
    doReturn(isVisibleAttr).when(entityType).getAttribute(IS_VISIBLE);
    doReturn(isAggregatableAttr).when(entityType).getAttribute(IS_AGGREGATABLE);
    doReturn(isReadOnlyAttr).when(entityType).getAttribute(IS_READ_ONLY);
    doReturn(isUniqueAttr).when(entityType).getAttribute(IS_UNIQUE);
    doReturn(isIdAttr).when(entityType).getAttribute(IS_ID_ATTRIBUTE);

    attribute = new Attribute(entityType);
    attribute.setIdAttribute(false);
    assertFalse(attribute.isReadOnly());
    assertFalse(attribute.isUnique());
    assertTrue(attribute.isNillable());
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6566

  @Test
  void testNewInstance() {
    EntityType entityType = createAttributeMetadataMock();

    attribute = new Attribute(entityType);
    AttributeFactory attributeFactory = mock(AttributeFactory.class);
    when(attributeFactory.create()).thenReturn(mock(Attribute.class));

    Attribute attribute = mock(Attribute.class);
    when(attribute.isVisible()).thenReturn(true);
    when(attribute.getNullableExpression()).thenReturn("nullableExpression");
    when(attribute.getValidationExpression()).thenReturn("expression");
    Attribute attributeCopy =
        Attribute.newInstance(attribute, SHALLOW_COPY_ATTRS, attributeFactory);
    verify(attributeCopy).setVisible(true);
    verify(attributeCopy).setNullableExpression("nullableExpression");
    verify(attributeCopy).setValidationExpression("expression");
  }

  @Test
  void testIsInversedBy() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("MyEntityTypeId").getMock();
    EntityType refEntityType = mock(EntityType.class);

    Attribute attribute = factory.create("MyAttributeId");
    attribute.setName("MyAttributeName");
    attribute.setEntity(entityType);
    attribute.setDataType(XREF);
    attribute.setRefEntity(refEntityType);

    Attribute mappedByAttr = factory.create("MyMappedByAttributeId");
    mappedByAttr.setName("MyAttributeName");
    mappedByAttr.setEntity(refEntityType);
    mappedByAttr.setDataType(ONE_TO_MANY);
    mappedByAttr.setRefEntity(entityType);
    mappedByAttr.setMappedBy(attribute);

    when(refEntityType.getAtomicAttributes()).thenReturn(singletonList(mappedByAttr));

    assertTrue(attribute.isInversedBy());
  }

  @Test
  void testIsInversedByString() {
    EntityType entityType = mock(EntityType.class);

    Attribute attribute = factory.create("MyAttributeId");
    attribute.setEntity(entityType);
    attribute.setDataType(STRING);

    assertFalse(attribute.isInversedBy());
  }

  @Test
  void testIsInversedByDifferentIdentifiers() {
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn("MyEntityTypeId").getMock();
    EntityType otherEntityType =
        when(mock(EntityType.class).getId()).thenReturn("MyOtherEntityTypeId").getMock();
    EntityType refEntityType = mock(EntityType.class);

    Attribute attribute = factory.create("MyAttributeId");
    attribute.setName("MyAttributeName");
    attribute.setEntity(entityType);
    attribute.setDataType(XREF);
    attribute.setRefEntity(refEntityType);

    Attribute mappedByAttr = factory.create("MyMappedByAttributeId");
    mappedByAttr.setName("MyAttributeName");
    mappedByAttr.setEntity(refEntityType);
    mappedByAttr.setDataType(ONE_TO_MANY);
    mappedByAttr.setRefEntity(otherEntityType);
    mappedByAttr.setMappedBy(attribute);

    when(refEntityType.getAtomicAttributes()).thenReturn(singletonList(mappedByAttr));

    assertFalse(attribute.isInversedBy());
  }

  private EntityType createMockEntityType() {
    EntityType entityType = mock(EntityType.class);
    Attribute typeAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    Attribute isNullableAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAutoAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isVisibleAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isAggregatableAttr =
        when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isReadOnlyAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute isUniqueAttr = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    Attribute parentAttr = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    doReturn(typeAttr).when(entityType).getAttribute(TYPE);
    doReturn(isNullableAttr).when(entityType).getAttribute(IS_NULLABLE);
    doReturn(isAutoAttr).when(entityType).getAttribute(IS_AUTO);
    doReturn(isVisibleAttr).when(entityType).getAttribute(IS_VISIBLE);
    doReturn(isAggregatableAttr).when(entityType).getAttribute(IS_AGGREGATABLE);
    doReturn(isReadOnlyAttr).when(entityType).getAttribute(IS_READ_ONLY);
    doReturn(isUniqueAttr).when(entityType).getAttribute(IS_UNIQUE);
    doReturn(parentAttr).when(entityType).getAttribute(PARENT);
    return entityType;
  }
}
