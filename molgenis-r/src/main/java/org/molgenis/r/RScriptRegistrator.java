package org.molgenis.r;

import org.molgenis.script.ScriptRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class RScriptRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final ScriptRunnerFactory scriptRunnerFactory;
	private final RScriptRunner rScriptRunner;

	@Autowired
	public RScriptRegistrator(ScriptRunnerFactory scriptRunnerFactory, RScriptRunner rScriptRunner)
	{
		this.scriptRunnerFactory = scriptRunnerFactory;
		this.rScriptRunner = rScriptRunner;
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		scriptRunnerFactory.registerScriptExecutor("R", rScriptRunner);
	}

}
