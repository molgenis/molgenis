package org.molgenis.python;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptOutputHandler;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptUtils;
import org.springframework.stereotype.Service;

@Service
public class PythonScriptRunner implements ScriptRunner {
  private static final String NAME = "python";

  private final PythonScriptExecutor pythonScriptExecutor;

  public PythonScriptRunner(PythonScriptExecutor pythonScriptExecutor) {
    this.pythonScriptExecutor = requireNonNull(pythonScriptExecutor);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean hasFileOutput(Script script) {
    return false;
  }

  @Override
  public String runScript(Script script, Map<String, Object> parameters) {
    String generatedScript = ScriptUtils.generateScript(script, parameters);
    var handler = new ScriptOutputHandler();
    pythonScriptExecutor.executeScript(generatedScript, handler);
    return handler.toString();
  }

  @Override
  public void runScript(
      Script script, Map<String, Object> parameters, ScriptOutputHandler outputHandler) {
    String generatedScript = ScriptUtils.generateScript(script, parameters);
    pythonScriptExecutor.executeScript(generatedScript, outputHandler);
  }
}
