package org.molgenis.script.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Discovers and registers {@link ScriptRunner} beans with the {@link ScriptRunnerFactory}
 */
@Component
public class ScriptRunnerRegistrar
{
	private final ScriptRunnerFactory scriptRunnerFactory;

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
