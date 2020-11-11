package org.molgenis.js.magma;

import static com.google.common.base.Throwables.getRootCause;
import static java.util.stream.Collectors.toList;
import static org.molgenis.js.graal.GraalScriptEngine.convertGraalValue;
import static org.molgenis.util.ResourceUtils.getString;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.molgenis.data.Entity;
import org.molgenis.script.core.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsMagmaScriptContext {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsMagmaScriptContext.class);
  private static final String KEY_IS_NULL = "_isNull";
  private static final String KEY_NEW_VALUE = "newValue";
  private static final String KEY_DOLLAR = "$";
  private static final String KEY_MAGMA_SCRIPT = "MagmaScript";
  private static final String BIND = "bind";
  public static final String KEY_ID_VALUE = "_idValue";
  private static final List<Source> SOURCES;

  private final Context context;

  static {
    SOURCES = Stream.of("/js/magma.js").map(JsMagmaScriptContext::getSource).collect(toList());
  }

  private static Source getSource(String resourceName) {
    try {
      String script = getString(JsMagmaScriptEvaluator.class, resourceName);
      return Source.newBuilder("js", script, resourceName).build();
    } catch (IOException ex) {
      throw new IllegalStateException("Resource not found: " + resourceName);
    }
  }

  JsMagmaScriptContext(Context context) {
    this.context = Objects.requireNonNull(context);
    prepare(context);
  }

  private void prepare(Context context) {
    LOGGER.debug("preparing context");
    SOURCES.forEach(context::eval);
    Value bindings = context.getBindings("js");
    Value magmaScript = bindings.getMember(KEY_MAGMA_SCRIPT);
    bindings.putMember(KEY_NEW_VALUE, magmaScript.getMember(KEY_NEW_VALUE));
    bindings.putMember(KEY_IS_NULL, magmaScript.getMember(KEY_IS_NULL));
  }

  public Object tryEval(String expression) {
    try {
      return eval(expression);
    } catch (Exception t) {
      return new ScriptException(getRootCause(t).getMessage());
    }
  }

  public Object eval(String expression) {
    return convertGraalValue(context.eval("js", expression));
  }

  /**
   * Binds to a given Entity.
   *
   * @param entity the entity to bind to the magmascript $ function
   */
  public void bind(Entity entity) {
    var bindings = context.getBindings("js");
    Value magmaScript = bindings.getMember(KEY_MAGMA_SCRIPT);
    Value dollarFunction = magmaScript.getMember(KEY_DOLLAR);
    Value boundDollar = dollarFunction.invokeMember(BIND, new EntityProxy(entity));
    bindings.putMember(KEY_DOLLAR, boundDollar);
  }

  void enter() {
    context.enter();
  }

  void leave() {
    context.leave();
  }

  void close() {
    context.close();
  }
}
