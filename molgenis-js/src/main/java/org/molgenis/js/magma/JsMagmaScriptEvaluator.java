package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;
import static org.molgenis.js.magma.JsMagmaScriptContext.ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH;

import java.util.List;
import java.util.function.Function;
import org.molgenis.data.Entity;
import org.molgenis.js.graal.GraalScriptEngine;
import org.springframework.stereotype.Component;

/** JavaScript script evaluator using the Graal script engine. */
@Component
public class JsMagmaScriptEvaluator {

  private final GraalScriptEngine graalScriptEngine;

  public JsMagmaScriptEvaluator(GraalScriptEngine graalScriptEngine) {
    this.graalScriptEngine = requireNonNull(graalScriptEngine);
  }

  public <T> T withinMagmaScriptContext(Function<JsMagmaScriptContext, T> function) {
    return graalScriptEngine.doWithinContext(
        context -> function.apply(new JsMagmaScriptContext(context)));
  }

  public List<Object> eval(List<String> expressions, Entity entity) {
    return withinMagmaScriptContext(
        context -> context.eval(expressions, entity, ENTITY_REFERENCE_DEFAULT_FETCHING_DEPTH));
  }

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
  public Object eval(String expression, Entity entity, int depth) {
    return withinMagmaScriptContext(
        context -> context.eval(List.of(expression), entity, depth).get(0));
  }
}
