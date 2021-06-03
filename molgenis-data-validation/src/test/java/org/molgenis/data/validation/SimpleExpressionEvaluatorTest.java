package org.molgenis.data.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.molgenis.expression.Parser.ParseException;
import scala.util.Failure;
import scala.util.Success;

class SimpleExpressionEvaluatorTest {
  SimpleExpressionEvaluator simpleExpressionEvaluator = new SimpleExpressionEvaluator();

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
}
