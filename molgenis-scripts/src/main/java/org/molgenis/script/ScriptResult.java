package org.molgenis.script;

public class ScriptResult
{
	private final String outputFile;
	private final String output;

	public ScriptResult(String outputFile, String output)
	{
		this.outputFile = outputFile;
		this.output = output;
	}

	public String getOutputFile()
	{
		return outputFile;
	}

	public String getOutput()
	{
		return output;
	}

}
