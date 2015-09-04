package org.molgenis.js;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.script.ScriptRunnerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Registers the JavaScript script type as a script runner
 */
@Component
public class JsScriptRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	public static final String SCRIPT_TYPE_JAVASCRIPT = "JavaScript";

	private final ScriptRunnerFactory scriptRunnerFactory;
	private final JsScriptRunner jsScriptRunner;

	@Autowired
	public JsScriptRegistrator(ScriptRunnerFactory scriptRunnerFactory, JsScriptRunner jsScriptRunner)
	{
		this.scriptRunnerFactory = checkNotNull(scriptRunnerFactory);
		this.jsScriptRunner = checkNotNull(jsScriptRunner);
	}

	@Override
	public int getOrder()
	{
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		scriptRunnerFactory.registerScriptExecutor(SCRIPT_TYPE_JAVASCRIPT, jsScriptRunner);
	}
}
