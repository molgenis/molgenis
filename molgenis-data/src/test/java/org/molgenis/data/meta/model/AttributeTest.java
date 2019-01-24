package org.molgenis.data.meta.model;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
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
import static org.molgenis.data.meta.model.AttributeMetadata.getIdAttributeValidationExpression;
import static org.molgenis.data.meta.model.EntityType.AttributeCopyMode.SHALLOW_COPY_ATTRS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.molgenis.data.Sort;
import org.molgenis.data.config.EntityBaseTestConfig;
import org.molgenis.data.config.MetadataTestConfig;
import org.molgenis.data.meta.AbstractSystemEntityTest;
import org.molgenis.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {
      EntityBaseTestConfig.class,
      AttributeMetadata.class,
      AttributeFactory.class,
      MetadataTestConfig.class
    })
public class AttributeTest extends AbstractSystemEntityTest {

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

  @Test
  public void testSystemEntity() {
    internalTestAttributes(
        metadata, Attribute.class, factory, getOverriddenReturnTypes(), getExcludedAttrs(), true);
  }

  private Attribute attribute;

  @Test
  public void setParentNullToAttribute() {
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

    attribute = new Attribute(entityType);

    Attribute parentAttribute = mock(Attribute.class);
    attribute.setParent(parentAttribute);
    verify(parentAttribute).addChild(attribute);
    verifyNoMoreInteractions(parentAttribute);
  }

  @Test
  public void testIsReferenceTypeString() {
    attribute.setDataType(STRING);
    assertFalse(attribute.hasRefEntity());
  }

  @Test
  public void testIsReferenceTypeXref() {
    attribute.setDataType(XREF);
    assertTrue(attribute.hasRefEntity());
  }

  @Test
  public void setParentNullToNull() {
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
    attribute = new Attribute(entityType);

    attribute.setParent(null);
    assertNull(attribute.getParent());
  }

  @Test
  public void setParentAttributeToNull() {
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
  public void setParentAttributeToAttribute() {
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
    assertEquals(attribute.getParent(), parentAttr);

    verify(currentParentAttr).removeChild(attribute);
    verifyNoMoreInteractions(currentParentAttr);
    verify(parentAttr).addChild(attribute);
    verifyNoMoreInteractions(parentAttr);
  }

  @Test
  public void setIdAttributeTrue() {
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
  public void setIdAttributeFalse() {
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

  @Test
  public void testIdValidationExpression() {
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

    attribute = new Attribute(entityType);
    String expression = getIdAttributeValidationExpression();
    assertEquals(
        expression,
        "$('isIdAttribute').eq(false).or($('isIdAttribute').isNull()).or($('isIdAttribute').eq(true)"
            + ".and("
            + "$('type').eq('email')"
            + ".or($('type').eq('hyperlink'))"
            + ".or($('type').eq('int'))"
            + ".or($('type').eq('long'))"
            + ".or($('type').eq('string'))"
            + ".or($('type').isNull())"
            + ")"
            + ".and($('isNullable').eq(false))).value()");
  }

  // Regression test for https://github.com/molgenis/molgenis/issues/6566
  @Test
  public void testNewInstance() {
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
}
