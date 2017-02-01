package org.molgenis.r;

import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Service
public class RScriptRunner implements ScriptRunner
{
	private static final String NAME = "R";

	private final RScriptExecutor rScriptExecutor;

	@Autowired
	public RScriptRunner(RScriptExecutor rScriptExecutor)
	{
		this.rScriptExecutor = requireNonNull(rScriptExecutor);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String runScript(Script script, Map<String, Object> parameters)
	{
		String rScript = script.generateScript(parameters);
		String outputFile = getOutputFile(parameters);
		String scriptResult = rScriptExecutor.executeScript(rScript, outputFile);
		return scriptResult;
	}

	private String getOutputFile(Map<String, Object> parameters)
	{
		Object outputFile = parameters.get("outputFile");
		if (outputFile == null)
		{
			return null;
		}
		if (!(outputFile instanceof String))
		{
			throw new RuntimeException(
					format("Parameter outputFile is of type [] instead of []", outputFile.getClass().getSimpleName(),
							String.class.getSimpleName()));
		}
		return (String) outputFile;
	}
}
