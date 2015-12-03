package org.molgenis.script;

import java.util.Map;

/**
 * Run a script with given parameters
 * 
 * For each script type a concrete ScriptRunner must be registered by at the ScriptRunnerFactory
 */
public interface ScriptRunner
{
	String runScript(Script script, Map<String, Object> parameters);
}
