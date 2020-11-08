package org.molgenis.js.graal;

import static java.util.stream.Collectors.toList;
import static org.molgenis.js.magma.JsMagmaScriptContext.KEY_ID_VALUE;

import java.util.List;
import java.util.function.Function;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

@Component
public class GraalScriptEngine {
  // see
  // https://www.graalvm.org/reference-manual/embed-languages/#code-caching-across-multiple-contexts
  Engine engine;
  Context.Builder contextBuilder;

  public GraalScriptEngine() {
    engine = Engine.newBuilder().build(); // TODO: look at the options
    contextBuilder =
        Context.newBuilder("js")
            .engine(engine)
            .allowExperimentalOptions(true)
            .option("js.experimental-foreign-object-prototype", "true")
            .allowAllAccess(false);
  }

  public Object eval(String script) {
    return doWithinContext(context -> eval(context, script));
  }

  public Object eval(Context context, String script) {
    return convertGraalValue(context.eval("js", script));
  }

  public <T> T doWithinContext(Function<Context, T> function) {
    try (Context context = createContext()) {
      context.enter();
      T result = function.apply(context);
      context.leave();
      return result;
    }
  }

  public Context createContext() {
    return contextBuilder.build();
  }

  public static Object convertGraalValue(Object value) {
    if (value == null) {
      return null;
    }
    if (!(value instanceof Value)) {
      return value;
    }
    Value graalValue = (Value) value;
    if (graalValue.isNull()) {
      return null;
    }
    if (graalValue.isInstant()) {
      return graalValue.getMember("getTime").execute().asLong();
    }
    if (graalValue.hasArrayElements()) {
      return graalValue.as(List.class).stream()
          .map(element -> convertGraalValue(element))
          .collect(toList());
    }
    if (graalValue.hasMembers() && graalValue.hasMember(KEY_ID_VALUE)) {
      return convertGraalValue(graalValue.getMember(KEY_ID_VALUE));
    }
    return graalValue.as(Object.class);
  }
}
