package org.molgenis.script;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class ScriptRunnerFactory
{
	private final Map<String, ScriptRunner> scriptRunners = Maps.newHashMap();

	public void registerScriptExecutor(String type, ScriptRunner scriptExecutor)
	{
		scriptRunners.put(type, scriptExecutor);
	}

	public ScriptRunner getScriptRunner(String type)
	{
		ScriptRunner scriptRunner = scriptRunners.get(type);
		if (scriptRunner == null)
		{
			throw new ScriptException("Unknown script type [" + type + "]");
		}

		return scriptRunner;
	}
}
