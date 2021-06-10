package org.molgenis.data.validation;

import static com.google.common.collect.Streams.stream;
import static org.molgenis.data.util.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.util.EntityTypeUtils.isReferenceType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.data.Entity;
import org.molgenis.expression.Expressions;
import org.molgenis.expression.Parser;
import org.springframework.stereotype.Component;
import scala.util.Try;

@Component
public class SimpleExpressionEvaluator {

  private static final int MAX_EXPRESSIONS_CACHED = 1000;
  private final Expressions expressions = new Expressions(MAX_EXPRESSIONS_CACHED);

  public static Object resolve(Entity entity, String variableName) {
    var attribute = entity.getEntityType().getAttributeByName(variableName);
    if (isMultipleReferenceType(attribute)) {
      return stream(entity.getEntities(attribute))
          .map(Entity::getIdValue)
          .collect(Collectors.toList());
    }
    if (isReferenceType(attribute)) {
      return entity.getEntity(attribute).getIdValue();
    }
    return entity.get(attribute);
  }

  /**
   * Gets all variable names used in expressions. Skips expressions that it cannot parse.
   *
   * @param expressions List of expression Strings
   * @return Set of variable names
   */
  public Set<String> getAllVariableNames(List<String> expressions) {
    return this.expressions.getAllVariableNames(expressions);
  }

  /**
   * Gets variable names used in an expression.
   *
   * @param expression the expression to parse
   * @return Set of variable names found in the expression
   * @throws Parser.ParseException if parsing the expression fails
   */
  public Set<String> getVariableNames(String expression) throws Parser.ParseException {
    return expressions.getVariableNames(expression);
  }

  /**
   * Evaluate expressions in a context
   *
   * @param expressions the expressions to evaluate
   * @param context the context to evaluate the expression in
   * @return evaluation results, using a {@link Try} to represent {@link scala.util.Success} or
   *     {@link scala.util.Failure}
   */
  public List<Try<Object>> parseAndEvaluate(List<String> expressions, Map<String, Object> context) {
    return this.expressions.parseAndEvaluate(expressions, context);
  }
}
