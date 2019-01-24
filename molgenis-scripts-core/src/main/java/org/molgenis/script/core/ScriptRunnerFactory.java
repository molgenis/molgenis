package org.molgenis.script.core;

import static java.lang.String.format;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Register script types.
 *
 * <p>Get a concrete ScriptRunner for a script type
 */
@Component
public class ScriptRunnerFactory {
  private final Map<String, ScriptRunner> scriptRunners;

  public ScriptRunnerFactory() {
    scriptRunners = Maps.newHashMap();
  }

  void registerScriptExecutor(ScriptRunner scriptExecutor) {
    scriptRunners.put(scriptExecutor.getName(), scriptExecutor);
  }

  Collection<ScriptRunner> getScriptRunners() {
    return scriptRunners.values();
  }

  public ScriptRunner getScriptRunner(String type) {
    ScriptRunner scriptRunner = scriptRunners.get(type);
    if (scriptRunner == null) {
      throw new ScriptException(format("Unknown script type [%s]", type));
    }

    return scriptRunner;
  }
}
