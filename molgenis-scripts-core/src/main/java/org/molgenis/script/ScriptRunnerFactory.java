package org.molgenis.script;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOG = LoggerFactory.getLogger(ScriptRunnerFactory.class);

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
			LOG.info("Registering Script type {}.", type);
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
