package org.molgenis.js.magma;

import static org.molgenis.js.magma.JsMagmaScriptContext.ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH;

import org.molgenis.data.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** JavaScript script evaluator using the Graal script engine. */
@Component
public class JsMagmaScriptEvaluator {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsMagmaScriptEvaluator.class);

  @WithJsMagmaScriptContext
  public Object eval(String expression, Entity entity) {
    return eval(expression, entity, ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH);
  }

  /**
   * Evaluate a single expression for a given entity.
   *
   * @param expression JavaScript expression
   * @param entity entity
   * @return evaluated expression result, return type depends on the expression.
   */
  @WithJsMagmaScriptContext
  public Object eval(String expression, Entity entity, int depth) {
    JsMagmaScriptContext context = JsMagmaScriptContextHolder.getContext();
    context.bind(entity, depth);
    return context.eval(expression);
  }
}
