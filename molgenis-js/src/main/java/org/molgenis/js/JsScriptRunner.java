package org.molgenis.js;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptUtils;
import org.springframework.stereotype.Service;

/** Runs a JavaScript with the given inputs and returns one output */
@Service
public class JsScriptRunner implements ScriptRunner {
  private static final String NAME = "JavaScript";

  private final JsScriptExecutor jsScriptExecutor;

  public JsScriptRunner(JsScriptExecutor jsScriptExecutor) {
    this.jsScriptExecutor = requireNonNull(jsScriptExecutor);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String runScript(Script script, Map<String, Object> parameters) {
    String jsScript = ScriptUtils.generateScript(script, parameters);
    Object scriptResult = jsScriptExecutor.executeScript(jsScript);
    return scriptResult != null ? scriptResult.toString() : null;
  }
}
