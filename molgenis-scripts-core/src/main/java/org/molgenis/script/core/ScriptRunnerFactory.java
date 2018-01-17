package org.molgenis.script.core;

import com.google.common.collect.Maps;
import org.molgenis.data.UnknownEntityException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Register script types.
 * <p>
 * Get a concrete ScriptRunner for a script type
 */
@Component
public class ScriptRunnerFactory
{
	private final Map<String, ScriptRunner> scriptRunners;
	private final ScriptTypeMetaData scriptTypeMetaData;

	public ScriptRunnerFactory(ScriptTypeMetaData scriptTypeMetaData)
	{
		this.scriptTypeMetaData = requireNonNull(scriptTypeMetaData);
		scriptRunners = Maps.newHashMap();
	}

	void registerScriptExecutor(ScriptRunner scriptExecutor)
	{
		scriptRunners.put(scriptExecutor.getName(), scriptExecutor);
	}

	Collection<ScriptRunner> getScriptRunners()
	{
		return scriptRunners.values();
	}

	public ScriptRunner getScriptRunner(String type)
	{
		ScriptRunner scriptRunner = scriptRunners.get(type);
		if (scriptRunner == null)
		{
			throw new UnknownEntityException(scriptTypeMetaData, type);
		}

		return scriptRunner;
	}
}
