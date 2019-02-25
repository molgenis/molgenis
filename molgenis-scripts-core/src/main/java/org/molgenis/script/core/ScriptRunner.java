package org.molgenis.script.core;

import java.util.Map;

/**
 * Run a script with given parameters
 *
 * <p>For each script type a concrete ScriptRunner must be registered by at the ScriptRunnerFactory
 */
public interface ScriptRunner {
  String getName();

  /** @return whether the script outputs a file */
  boolean hasFileOutput(Script script);

  String runScript(Script script, Map<String, Object> parameters);
}
