package org.molgenis.js.magma;

import org.molgenis.data.Entity;
import org.springframework.stereotype.Component;

/** JavaScript script evaluator using the Graal script engine. */
@Component
public class JsMagmaScriptEvaluator {

  /**
   * Evaluate a single expression for a given entity.
   *
   * @param expression JavaScript expression
   * @param entity entity
   * @return evaluated expression result, return type depends on the expression.
   */
  @WithJsMagmaScriptContext
  public Object eval(String expression, Entity entity) {
    JsMagmaScriptContext context = JsMagmaScriptContextHolder.getContext();
    context.bind(entity);
    return context.eval(expression);
  }
}
