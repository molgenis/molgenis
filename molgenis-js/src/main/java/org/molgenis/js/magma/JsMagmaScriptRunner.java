package org.molgenis.js.magma;

import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.molgenis.script.core.exception.ScriptExecutionException;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Runs a JavaScript using the Magma API with the given inputs and returns one output
 */
@Service
public class JsMagmaScriptRunner implements ScriptRunner
{
	public static final String NAME = "JavaScript (Magma)";

	private final JsMagmaScriptExecutor jsScriptExecutor;

	public JsMagmaScriptRunner(JsMagmaScriptExecutor jsMagmaScriptExecutor)
	{
		this.jsScriptExecutor = requireNonNull(jsMagmaScriptExecutor);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		String jsScript = script.getContent();

		Object scriptResult;
		try
		{
			scriptResult = jsScriptExecutor.executeScript(jsScript, parameters);

		}
		catch (ScriptExecutionException see)
		{
			scriptResult = see.getLocalizedMessage();
		}

		return scriptResult != null ? scriptResult.toString() : null;
	}
}