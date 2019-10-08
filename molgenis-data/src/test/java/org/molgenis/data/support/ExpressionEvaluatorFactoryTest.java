package org.molgenis.data.support;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class ExpressionEvaluatorFactoryTest {
  @Test
  void testCreateExpressionEvaluatorStringExpressionEvaluator() {
    Attribute attribute = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
    Attribute otherAttribute =
        when(mock(Attribute.class).getName()).thenReturn("otherAttr").getMock();
    when(attribute.getExpression()).thenReturn("otherAttr");
    EntityType entityType = mock(EntityType.class);
    when(entityType.getAttribute("attr")).thenReturn(attribute);
    when(entityType.getAttribute("otherAttr")).thenReturn(otherAttribute);
    assertTrue(
        ExpressionEvaluatorFactory.createExpressionEvaluator(attribute, entityType)
            instanceof StringExpressionEvaluator);
  }

  @Test
  void testCreateExpressionEvaluatorTemplateExpressionEvaluator() {
    Attribute attribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
    EntityType entityType = mock(EntityType.class);
    assertTrue(
        ExpressionEvaluatorFactory.createExpressionEvaluator(attribute, entityType)
            instanceof TemplateExpressionEvaluator);
  }
}
