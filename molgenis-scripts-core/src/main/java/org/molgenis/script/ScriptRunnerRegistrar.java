package org.molgenis.script;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Discovers and registers {@link ScriptRunner} beans with the {@link ScriptRunnerFactory}
 */
@Component
public class ScriptRunnerRegistrar
{
	private final ScriptRunnerFactory scriptRunnerFactory;

	@Autowired
	public ScriptRunnerRegistrar(ScriptRunnerFactory scriptRunnerFactory)
	{
		this.scriptRunnerFactory = requireNonNull(scriptRunnerFactory);
	}

	public void register(ContextRefreshedEvent event)
	{
		ApplicationContext ctx = event.getApplicationContext();
		Map<String, ScriptRunner> scriptRunnerMap = ctx.getBeansOfType(ScriptRunner.class);
		scriptRunnerMap.values().forEach(this::register);
	}

	private void register(ScriptRunner scriptRunner)
	{
		scriptRunnerFactory.registerScriptExecutor(scriptRunner);
	}
}
