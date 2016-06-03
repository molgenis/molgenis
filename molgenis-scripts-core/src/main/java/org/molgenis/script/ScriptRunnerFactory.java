package org.molgenis.script;

import static java.lang.String.format;

import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * Register script types.
 * <p>
 * Get a concrete ScriptRunner for a script type
 */
@Component
public class ScriptRunnerFactory
{
	private final Map<String, ScriptRunner> scriptRunners;

	public ScriptRunnerFactory()
	{
		scriptRunners = Maps.newHashMap();
	}

	public void registerScriptExecutor(ScriptRunner scriptExecutor)
	{
		scriptRunners.put(scriptExecutor.getName(), scriptExecutor);
	}

	public Stream<ScriptRunner> getScriptRunners()
	{
		return scriptRunners.values().stream();
	}

	public ScriptRunner getScriptRunner(String type)
	{
		ScriptRunner scriptRunner = scriptRunners.get(type);
		if (scriptRunner == null)
		{
			throw new ScriptException(format("Unknown script type [%s]", type));
		}

		return scriptRunner;
	}
}
