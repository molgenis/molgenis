package org.molgenis.script;

import java.util.Map;

import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
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
	private final MysqlRepository scriptTypeRepo;

	@Autowired
	public ScriptRunnerFactory(MysqlRepositoryCollection mysqlRepositoryCollection)
	{
		mysqlRepositoryCollection.add(ScriptParameter.META_DATA);
		scriptTypeRepo = mysqlRepositoryCollection.add(ScriptType.META_DATA);
		mysqlRepositoryCollection.add(Script.META_DATA);
	}

	@RunAsSystem
	public void registerScriptExecutor(String type, ScriptRunner scriptExecutor)
	{
		scriptRunners.put(type, scriptExecutor);

		if (scriptTypeRepo.count(new QueryImpl().eq(ScriptType.NAME, type)) == 0)
		{
			scriptTypeRepo.add(new ScriptType(type));
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
