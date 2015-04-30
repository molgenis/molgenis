package org.molgenis.python;

import org.molgenis.script.ScriptRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class PythonScriptRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final ScriptRunnerFactory scriptRunnerFactory;
	private final PythonScriptRunner pythonScriptRunner;

	@Autowired
	public PythonScriptRegistrator(ScriptRunnerFactory scriptRunnerFactory, PythonScriptRunner pythonScriptRunner)
	{
		this.scriptRunnerFactory = scriptRunnerFactory;
		this.pythonScriptRunner = pythonScriptRunner;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		scriptRunnerFactory.registerScriptExecutor("python", pythonScriptRunner);
	}
}
