package org.molgenis.script;

import java.util.Map;

public interface ScriptRunner
{
	void runScript(Script script, Map<String, Object> parameters);
}
