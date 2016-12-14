package org.molgenis.js;

import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Runs a JavaScript with the given inputs and returns one output
 */
@Service
public class JsScriptRunner implements ScriptRunner
{
	private static final String NAME = "JavaScript";

	private final JsScriptExecutor jsScriptExecutor;

	@Autowired
	public JsScriptRunner(JsScriptExecutor jsScriptExecutor)
	{
		this.jsScriptExecutor = requireNonNull(jsScriptExecutor);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		String jsScript = script.generateScript(parameters);
		Object scriptResult = jsScriptExecutor.executeScript(jsScript);
		return scriptResult != null ? scriptResult.toString() : null;
	}
}
