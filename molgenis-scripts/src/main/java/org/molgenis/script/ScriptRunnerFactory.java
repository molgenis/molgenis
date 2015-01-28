package org.molgenis.script;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * Register script types.
 * 
 * Get a concrete ScriptRunner for a script type
 */
@Component
public class ScriptRunnerFactory
{
	private final Map<String, ScriptRunner> scriptRunners = Maps.newHashMap();
	private final DataService dataService;

	@Autowired
	public ScriptRunnerFactory(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RunAsSystem
	public void registerScriptExecutor(String type, ScriptRunner scriptExecutor)
	{
		scriptRunners.put(type, scriptExecutor);

		if (dataService.count(ScriptType.ENTITY_NAME, new QueryImpl().eq(ScriptType.NAME, type)) == 0)
		{
			dataService.add(ScriptType.ENTITY_NAME, new ScriptType(type, dataService));
		}
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
