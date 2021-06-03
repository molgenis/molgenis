package org.molgenis.data.validation;

import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.expression.Parser.ParseException;
import org.molgenis.test.AbstractMockitoTest;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;

class ExpressionValidatorTest extends AbstractMockitoTest {
  @Mock private SimpleExpressionEvaluator simpleExpressionEvaluator;
  @Mock private Entity entity;
  private ExpressionValidator expressionValidator;

  @BeforeEach
  void beforeMethod() {
    expressionValidator = new ExpressionValidator(simpleExpressionEvaluator);
  }

  @Test
  void testResolveBooleanExpressions() {
    final var expressions = List.of("{foo}", "true");
    when(simpleExpressionEvaluator.getAllVariableNames(expressions)).thenReturn(Set.of("foo"));
    when(entity.get("foo")).thenReturn(false);
    when(simpleExpressionEvaluator.parseAndEvaluate(expressions, Map.of("foo", false)))
        .thenReturn(List.of(new Success<>(false), new Success<>(true)));
    assertEquals(
        asList(false, true),
        expressionValidator.resolveBooleanExpressions(Arrays.asList("{foo}", "true"), entity));
  }

  static Object[][] resultProvider() {
    return new Object[][] {
      new Object[] {new Success<>(FALSE), false},
      new Object[] {new Success<>(0), false},
      new Object[] {new Success<>(null), false},
      new Object[] {new Success<>(0f), false},
      new Object[] {new Success<>(""), false},
      new Object[] {new Success<>(Double.NaN), false},
      new Object[] {new Success<>("TRUE"), true},
      new Object[] {new Success<>("FALSE"), true},
      new Object[] {new Success<>(42), true},
      new Object[] {new Success<>(42f), true},
      new Object[] {
        new Failure<>(
            new ParseException(
                "Evaluation failed on line 0, column 10: Undefined is not an object", 10)),
        null
      }
    };
  }

  @ParameterizedTest
  @MethodSource("resultProvider")
  void testResolveBooleanExpression(Try<Object> result, Boolean expected) {
    when(simpleExpressionEvaluator.getAllVariableNames(List.of("expression")))
        .thenReturn(Collections.emptySet());
    when(simpleExpressionEvaluator.parseAndEvaluate(List.of("expression"), Map.of()))
        .thenReturn(List.of(result));

    assertEquals(expected, expressionValidator.resolveBooleanExpression("expression", entity));
  }
}
