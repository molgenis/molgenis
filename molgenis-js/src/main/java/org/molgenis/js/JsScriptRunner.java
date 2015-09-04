package org.molgenis.js;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Runs a JavaScript with the given inputs and returns one output
 */
@Service
public class JsScriptRunner implements ScriptRunner
{
	private final JsScriptExecutor jsScriptExecutor;

	@Autowired
	public JsScriptRunner(JsScriptExecutor jsScriptExecutor)
	{
		this.jsScriptExecutor = checkNotNull(jsScriptExecutor);
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		String jsScript = script.generateScript(parameters);
		Object scriptResult = jsScriptExecutor.executeScript(jsScript);
		return scriptResult.toString();
	}
}
