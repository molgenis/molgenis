package org.molgenis.script;

import java.util.Map;

public interface ScriptRunner
{
	String runScript(Script script, Map<String, Object> parameters);
}
