package org.molgenis.script;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.script.ScriptTypeMetaData.SCRIPT_TYPE;

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
		Collection<ScriptRunner> scriptRunners = scriptRunnerFactory.getScriptRunners();

		persist(scriptRunners.stream().filter(this::notExists).map(this::createScriptType));
	}

	private boolean notExists(ScriptRunner scriptRunner)
	{
		return dataService.findOneById(SCRIPT_TYPE, scriptRunner.getName(), ScriptType.class) == null;
	}

	private ScriptType createScriptType(ScriptRunner scriptRunner)
	{
		return scriptTypeFactory.create(scriptRunner.getName());
	}

	private void persist(Stream<ScriptType> scriptTypes)
	{
		dataService.add(SCRIPT_TYPE, scriptTypes);
	}
}

