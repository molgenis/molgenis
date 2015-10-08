package org.molgenis.js.magma;

import static java.util.Objects.requireNonNull;

import org.molgenis.script.ScriptRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Registers the JavaScript Magma script type as a script runner
 */
@Component
public class JsMagmaScriptRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	public static final String SCRIPT_TYPE_JAVASCRIPT_MAGMA = "JavaScript (Magma)";

	private final ScriptRunnerFactory scriptRunnerFactory;
	private final JsMagmaScriptRunner jsMagmaScriptRunner;

	@Autowired
	public JsMagmaScriptRegistrator(ScriptRunnerFactory scriptRunnerFactory, JsMagmaScriptRunner jsMagmaScriptRunner)
	{
		this.scriptRunnerFactory = requireNonNull(scriptRunnerFactory);
		this.jsMagmaScriptRunner = requireNonNull(jsMagmaScriptRunner);
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		scriptRunnerFactory.registerScriptExecutor(SCRIPT_TYPE_JAVASCRIPT_MAGMA, jsMagmaScriptRunner);
	}
}
