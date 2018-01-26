package org.molgenis.python;

import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Objects.requireNonNull;

@Service
public class PythonScriptRunner implements ScriptRunner
{
	private static final String NAME = "python";

	private final PythonScriptExecutor pythonScriptExecutor;

	public PythonScriptRunner(PythonScriptExecutor pythonScriptExecutor)
	{
		this.pythonScriptExecutor = requireNonNull(pythonScriptExecutor);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		String generatedScript = ScriptUtils.generateScript(script, parameters);
		StringPythonOutputHandler handler = new StringPythonOutputHandler();
		pythonScriptExecutor.executeScript(generatedScript, handler);
		return handler.toString();
	}
}
