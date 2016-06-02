package org.molgenis.script;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.script.ScriptTypeMetaData.SCRIPT_TYPE;

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
 * <p>
 * Get a concrete ScriptRunner for a script type
 */
@Component
public class ScriptRunnerFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(ScriptRunnerFactory.class);

	private final DataService dataService;
	private final ScriptTypeFactory scriptTypeFactory;

	private final Map<String, ScriptRunner> scriptRunners = Maps.newHashMap();

	@Autowired
	public ScriptRunnerFactory(DataService dataService, ScriptTypeFactory scriptTypeFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.scriptTypeFactory = requireNonNull(scriptTypeFactory);
	}

	@RunAsSystem
	public void registerScriptExecutor(ScriptRunner scriptExecutor)
	{
		scriptRunners.put(scriptExecutor.getName(), scriptExecutor);

		if (dataService.count(SCRIPT_TYPE, new QueryImpl<>().eq(ScriptTypeMetaData.NAME, scriptExecutor.getName()))
				== 0)
		{
			LOG.info("Registering Script type {}.", scriptExecutor.getName());
			dataService.add(SCRIPT_TYPE, scriptTypeFactory.create(scriptExecutor.getName()));
		}
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
