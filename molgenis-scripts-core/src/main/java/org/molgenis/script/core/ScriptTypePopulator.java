package org.molgenis.script.core;

import org.molgenis.data.DataService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.script.core.ScriptTypeMetaData.SCRIPT_TYPE;

/**
 * Populates {@link ScriptType} repository with script type entities based on existing {@link ScriptRunner script runners.}
 */
@Component
public class ScriptTypePopulator
{
	private final ScriptRunnerFactory scriptRunnerFactory;
	private final DataService dataService;
	private final ScriptTypeFactory scriptTypeFactory;

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

		List<ScriptType> newScriptTypes = scriptRunners.stream()
													   .filter(this::notExists)
													   .map(this::createScriptType)
													   .collect(toList());
		if (!newScriptTypes.isEmpty())
		{
			persist(newScriptTypes);
		}
	}

	private boolean notExists(ScriptRunner scriptRunner)
	{
		return dataService.findOneById(SCRIPT_TYPE, scriptRunner.getName(), ScriptType.class) == null;
	}

	private ScriptType createScriptType(ScriptRunner scriptRunner)
	{
		return scriptTypeFactory.create(scriptRunner.getName());
	}

	private void persist(List<ScriptType> scriptTypes)
	{
		dataService.add(SCRIPT_TYPE, scriptTypes.stream());
	}
}

