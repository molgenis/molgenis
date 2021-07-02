package org.molgenis.data.validation;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.molgenis.data.Entity;
import org.molgenis.expression.Evaluator;
import org.springframework.stereotype.Component;
import scala.util.Try;

@Component
public class ExpressionValidator {

  private final SimpleExpressionEvaluator evaluator;

  public ExpressionValidator(SimpleExpressionEvaluator evaluator) {
    this.evaluator = requireNonNull(evaluator);
  }

  /**
   * Resolves a boolean expression with an Entity as context.
   *
   * @param expression expression to evaluate
   * @param entity the entity to use as context
   * @return value of the expression or null if evaluation failed
   */
  Boolean resolveBooleanExpression(String expression, Entity entity) {
    return resolveBooleanExpressions(List.of(expression), entity).get(0);
  }

  /**
   * Evaluates boolean expressions with an Entity as context.
   *
   * @param expressions the expressions to evaluate
   * @param entity the entity to use as context
   * @return value of each expression or * null if evaluation failed, in same order as expressions
   */
  public List<Boolean> resolveBooleanExpressions(List<String> expressions, Entity entity) {
    Map<String, Object> context = createContext(expressions, entity);
    return evaluator.parseAndEvaluate(expressions, context).stream()
        .map(this::convertToBoolean)
        .collect(Collectors.toList());
  }

  private Map<String, Object> createContext(List<String> expressions, Entity entity) {
    var variableNames = evaluator.getAllVariableNames(expressions);
    Map<String, Object> context = new HashMap<>();
    for (String variableName : variableNames) {
      context.put(variableName, SimpleExpressionEvaluator.resolve(entity, variableName));
    }
    return context;
  }

  private Boolean convertToBoolean(Try<Object> result) {
    return result.map(Evaluator::isTruthy).getOrElse(() -> null);
  }
}
