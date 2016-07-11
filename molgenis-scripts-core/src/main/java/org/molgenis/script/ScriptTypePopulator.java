package org.molgenis.script;

import static java.util.Objects.requireNonNull;
import static org.molgenis.script.ScriptTypeMetaData.SCRIPT_TYPE;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Populates {@link ScriptType} repository with script type entities based on existing {@link ScriptRunner script runners.}
 */
@Component
public class ScriptTypePopulator
{
	private final ScriptRunnerFactory scriptRunnerFactory;
	private final DataService dataService;
	private final ScriptTypeFactory scriptTypeFactory;

	@Autowired
	public ScriptTypePopulator(ScriptRunnerFactory scriptRunnerFactory, DataService dataService,
			ScriptTypeFactory scriptTypeFactory)
	{
		this.scriptRunnerFactory = requireNonNull(scriptRunnerFactory);
		this.dataService = requireNonNull(dataService);
		this.scriptTypeFactory = requireNonNull(scriptTypeFactory);
	}

	public void populate()
	{
		scriptRunnerFactory.getScriptRunners().map(ScriptRunner::getName).forEach(this::persist);
	}

	private void persist(String scriptTypeName)
	{
		ScriptType scriptType = dataService.findOneById(SCRIPT_TYPE, scriptTypeName, ScriptType.class);
		if (scriptType == null)
		{
			dataService.add(SCRIPT_TYPE, scriptTypeFactory.create(scriptTypeName));
		}
	}
}

