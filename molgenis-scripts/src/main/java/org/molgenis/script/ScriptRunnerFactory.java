package org.molgenis.script;

import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
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
	public ScriptRunnerFactory(DataService dataService, MysqlRepositoryCollection mysqlRepositoryCollection)
	{
		this.dataService = dataService;
		mysqlRepositoryCollection.add(ScriptParameter.META_DATA);
		mysqlRepositoryCollection.add(ScriptType.META_DATA);
		mysqlRepositoryCollection.add(Script.META_DATA);
	}

	public void registerScriptExecutor(String type, ScriptRunner scriptExecutor)
	{
		scriptRunners.put(type, scriptExecutor);

		if (dataService.query(ScriptType.ENTITY_NAME).eq(ScriptType.NAME, type).count() == 0)
		{
			dataService.add(ScriptType.ENTITY_NAME, new ScriptType(type));
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
