package org.molgenis.data.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.expression.Parser.ParseException;
import org.molgenis.test.AbstractMockitoTest;
import scala.util.Failure;
import scala.util.Success;

class SimpleExpressionEvaluatorTest extends AbstractMockitoTest {
  SimpleExpressionEvaluator simpleExpressionEvaluator = new SimpleExpressionEvaluator();
  @Mock private Entity entity;
  @Mock private Entity foo1;
  @Mock private Entity foo2;
  @Mock private EntityType entityType;
  @Mock private Attribute attribute;

  @Test
  void testGetVariableNames() throws ParseException {
    assertEquals(Set.of("foo"), simpleExpressionEvaluator.getVariableNames("{foo}"));
  }

  @Test
  void testGetVariableNamesThrows() {
    assertThrows(ParseException.class, () -> simpleExpressionEvaluator.getVariableNames("{foo"));
  }

  @Test
  void testGetAllVariableNamesFilters() {
    assertEquals(
        Set.of("bar"), simpleExpressionEvaluator.getAllVariableNames(List.of("{foo", "{bar}")));
  }

  @Test
  void testParseAndEvaluate() {
    assertEquals(
        List.of(
            new Failure<>(new ParseException("Expected \"}\":1:5, found \"\"", 4)),
            new Success<>(42)),
        simpleExpressionEvaluator.parseAndEvaluate(List.of("{foo", "{bar}"), Map.of("bar", 42)));
  }

  @Test
  void testResolveMref() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAttributeByName("foo")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.MREF);
    when(entity.getEntities(attribute)).thenReturn(List.of(foo1, foo2));
    when(foo1.getIdValue()).thenReturn(1);
    when(foo2.getIdValue()).thenReturn(2);

    assertEquals(List.of(1, 2), SimpleExpressionEvaluator.resolve(entity, "foo"));
  }

  @Test
  void testResolveXref() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAttributeByName("foo")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.XREF);
    when(entity.getEntity(attribute)).thenReturn(foo1);
    when(foo1.getIdValue()).thenReturn(1);

    assertEquals(1, SimpleExpressionEvaluator.resolve(entity, "foo"));
  }

  @Test
  void testResolveXrefNull() {
    when(entity.getEntityType()).thenReturn(entityType);
    when(entityType.getAttributeByName("foo")).thenReturn(attribute);
    when(attribute.getDataType()).thenReturn(AttributeType.XREF);
    when(entity.getEntity(attribute)).thenReturn(null);

    assertNull(
        "null xref should resolve to null", SimpleExpressionEvaluator.resolve(entity, "foo"));
  }
}
