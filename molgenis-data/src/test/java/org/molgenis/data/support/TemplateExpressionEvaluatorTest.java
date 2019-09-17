package org.molgenis.data.support;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.BOOL;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTest;

class TemplateExpressionEvaluatorTest extends AbstractMockitoTest {
  @Mock private Attribute expressionAttribute;
  @Mock private EntityType entityType;
  private TemplateExpressionEvaluator templateExpressionEvaluator;

  @BeforeEach
  void setUpBeforeMethod() {
    templateExpressionEvaluator = new TemplateExpressionEvaluator(expressionAttribute, entityType);
  }

  @Test
  void testTemplateExpressionEvaluator() {
    assertThrows(NullPointerException.class, () -> new TemplateExpressionEvaluator(null, null));
  }

  @Test
  void testEvaluateBoolean() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getBoolean("attr")).thenReturn(true).getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my true", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateRef() {
    EntityType refEntityType = mock(EntityType.class);
    Attribute refAttribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(refEntityType.getAttribute("refAttr")).thenReturn(refAttribute);

    Entity refEntity = mock(Entity.class);
    when(refEntity.getEntityType()).thenReturn(refEntityType);
    when(refEntity.getString("refAttr")).thenReturn("value");

    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr.refAttr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getEntity("attr")).thenReturn(refEntity).getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my value", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateRefMultipleAttributes() {
    EntityType refEntityType = mock(EntityType.class);
    Attribute refAttribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    doReturn(refAttribute).when(refEntityType).getAttribute("refAttr");
    Attribute otherRefAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    doReturn(otherRefAttribute).when(refEntityType).getAttribute("otherRefAttr");

    Entity refEntity = mock(Entity.class);
    when(refEntity.getEntityType()).thenReturn(refEntityType);
    doReturn("value").when(refEntity).getString("refAttr");
    doReturn("otherValue").when(refEntity).getString("otherRefAttr");

    when(expressionAttribute.getExpression())
        .thenReturn("{\"template\":\"my {{attr.refAttr}} and {{attr.otherRefAttr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getEntity("attr")).thenReturn(refEntity).getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my value and otherValue", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateMultipleRef() {
    EntityType refEntityType = mock(EntityType.class);
    Attribute refAttribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(refEntityType.getAttribute("refAttr")).thenReturn(refAttribute);

    Entity refEntity0 = mock(Entity.class);
    when(refEntity0.getEntityType()).thenReturn(refEntityType);
    when(refEntity0.getString("refAttr")).thenReturn("value0");
    Entity refEntity1 = mock(Entity.class);
    when(refEntity1.getEntityType()).thenReturn(refEntityType);
    when(refEntity1.getString("refAttr")).thenReturn("value1");

    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr.refAttr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(MREF).getMock();
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity =
        when(mock(Entity.class).getEntities("attr"))
            .thenReturn(asList(refEntity0, refEntity1))
            .getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my value0,value1", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateCompound() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(COMPOUND).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = mock(Entity.class);
    Exception exception =
        assertThrows(
            TemplateExpressionAttributeTypeException.class,
            () -> templateExpressionEvaluator.evaluate(entity));
    assertThat(exception.getMessage())
        .containsPattern(
            "expression:\\{\"template\":\"my \\{\\{attr\\}\\}\"\\} tag:attr type:COMPOUND");
  }

  @Test
  void testEvaluateDate() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DATE).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity =
        when(mock(Entity.class).getLocalDate("attr"))
            .thenReturn(LocalDate.parse("2000-12-31"))
            .getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my 2000-12-31", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateDateTime() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DATE_TIME).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity =
        when(mock(Entity.class).getInstant("attr"))
            .thenReturn(Instant.parse("2015-05-22T06:12:13Z"))
            .getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my 2015-05-22T06:12:13Z", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateDecimal() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(DECIMAL).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getDouble("attr")).thenReturn(1.23).getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my 1.23", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateString() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getString("attr")).thenReturn("value").getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my value", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateInt() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(INT).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getInt("attr")).thenReturn(123).getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my 123", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateLong() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(LONG).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Entity entity = when(mock(Entity.class).getLong("attr")).thenReturn(123L).getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertEquals("my 123", templateExpressionEvaluator.evaluate(entity));
  }

  @Test
  void testEvaluateExpressionNull() {
    when(expressionAttribute.getExpression()).thenReturn(null);
    assertThrows(
        TemplateExpressionException.class,
        () -> templateExpressionEvaluator.evaluate(mock(Entity.class)));
  }

  @Test
  void testEvaluateExpressionInvalidSyntax() {
    when(expressionAttribute.getExpression()).thenReturn("blaat");
    Exception exception =
        assertThrows(
            TemplateExpressionSyntaxException.class,
            () -> templateExpressionEvaluator.evaluate(mock(Entity.class)));
    assertThat(exception.getMessage()).containsPattern("expression:blaat");
  }

  @Test
  void testEvaluateExpressionInvalidTemplateSyntax() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{{attr}}\"}");
    Exception exception =
        assertThrows(
            TemplateExpressionSyntaxException.class,
            () -> templateExpressionEvaluator.evaluate(mock(Entity.class)));
    assertThat(exception.getMessage())
        .containsPattern("expression:\\{\"template\":\"my \\{\\{\\{attr\\}\\}\"\\}");
  }

  @Test
  void testEvaluateExpressionUnknownAttribute() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Exception exception =
        assertThrows(
            TemplateExpressionUnknownAttributeException.class,
            () -> templateExpressionEvaluator.evaluate(mock(Entity.class)));
    assertThat(exception.getMessage())
        .containsPattern("expression:\\{\"template\":\"my \\{\\{attr\\}\\}\"\\} tag:attr");
  }

  @Test
  void testEvaluateRefAttributeMissing() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(XREF).getMock();
    EntityType refEntityType = mock(EntityType.class);
    when(attribute.hasRefEntity()).thenReturn(true);
    when(attribute.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Exception exception =
        assertThrows(
            TemplateExpressionMissingTagException.class,
            () -> templateExpressionEvaluator.evaluate(mock(Entity.class)));
    assertThat(exception.getMessage())
        .containsPattern("expression:\\{\"template\":\"my \\{\\{attr\\}\\}\"\\} tag:attr");
  }

  @Test
  void testEvaluateAttributeWithAdditionalSubTag() {
    when(expressionAttribute.getExpression()).thenReturn("{\"template\":\"my {{attr.label}}\"}");
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    Exception exception =
        assertThrows(
            TemplateExpressionInvalidTagException.class,
            () -> templateExpressionEvaluator.evaluate(mock(Entity.class)));
    assertThat(exception.getMessage())
        .containsPattern("expression:\\{\"template\":\"my \\{\\{attr.label\\}\\}\"\\} tag:label");
  }
}
