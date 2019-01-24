package org.molgenis.js.nashorn;

import static java.util.Objects.requireNonNull;
import static org.molgenis.js.magma.JsMagmaScriptEvaluator.KEY_ID_VALUE;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Weigher;
import java.util.Map;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NashornScriptEngine {
  private static final Logger LOG = LoggerFactory.getLogger(NashornScriptEngine.class);
  private static final int MAX_COMPILED_EXPRESSIONS_SCRIPTS_LENGTH = 500_000;
  private ScriptEngine scriptEngine;
  private LoadingCache<String, CompiledScript> expressions;

  public NashornScriptEngine() {
    initScriptEngine();
  }

  public Object eval(String script) throws ScriptException {
    return convertNashornValue(scriptEngine.eval(script, new SimpleBindings()));
  }

  /**
   * Evaluates an expression using the given bindings.
   *
   * @param bindings the Bindings to use as ENGINE_SCOPE
   * @param expression the expression to evaluate
   * @return result of the evaluation
   * @throws ScriptException if the evaluation fails
   */
  public Object eval(Bindings bindings, String expression) throws ScriptException {
    CompiledScript compiledExpression = requireNonNull(expressions.get(expression));
    Object returnValue = compiledExpression.eval(bindings);
    return convertNashornValue(returnValue);
  }

  public ScriptObjectMirror newJSArray() {
    return (ScriptObjectMirror) ((JSObject) scriptEngine.get("Array")).newObject();
  }

  private void initScriptEngine() {
    LOG.debug("Initializing Nashorn script engine ...");
    NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
    scriptEngine =
        factory.getScriptEngine(s -> false); // create engine with class filter exposing no classes
    expressions =
        Caffeine.newBuilder()
            .maximumWeight(MAX_COMPILED_EXPRESSIONS_SCRIPTS_LENGTH)
            .weigher((Weigher<String, CompiledScript>) (key, value) -> key.length())
            .build(((Compilable) this.scriptEngine)::compile);
    LOG.debug("Initialized Nashorn script engine");
  }

  private Object convertNashornValue(Object nashornValue) {
    if (nashornValue == null) {
      return null;
    }

    Object convertedValue;
    if (nashornValue instanceof ScriptObjectMirror) {
      ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) nashornValue;
      if (scriptObjectMirror.isArray()) {
        convertedValue =
            scriptObjectMirror
                .values()
                .stream()
                .map(this::convertNashornValue)
                .collect(Collectors.toList());
      } else {
        if ("Date".equals(scriptObjectMirror.getClassName())) {
          // convert to Java Interface
          JsDate jsDate = ((Invocable) scriptEngine).getInterface(scriptObjectMirror, JsDate.class);
          return jsDate.getTime();
        } else return ((ScriptObjectMirror) nashornValue).getOrDefault(KEY_ID_VALUE, null);
      }
    } else if (nashornValue instanceof Map) {
      Map mapValue = (Map) (nashornValue);
      if (mapValue.get(KEY_ID_VALUE) != null) {
        convertedValue = mapValue.get(KEY_ID_VALUE);
      } else {
        convertedValue = nashornValue;
      }
    } else {
      convertedValue = nashornValue;
    }
    return convertedValue;
  }
}
