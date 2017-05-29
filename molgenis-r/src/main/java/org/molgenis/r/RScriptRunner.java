package org.molgenis.r;

import org.molgenis.script.Script;
import org.molgenis.script.ScriptRunner;
import org.molgenis.script.ScriptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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
		String generatedScript = ScriptUtils.generateScript(script, parameters);
		StringROutputHandler handler = new StringROutputHandler();
		rScriptExecutor.executeScript(generatedScript, handler);
		return handler.toString();
	}
}
