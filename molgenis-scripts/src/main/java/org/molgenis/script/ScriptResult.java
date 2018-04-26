package org.molgenis.script;

import org.molgenis.data.file.model.FileMeta;

public class ScriptResult
{
	private final FileMeta fileMeta;
	private final String output;

	public ScriptResult(FileMeta fileMeta, String output)
	{
		this.fileMeta = fileMeta;
		this.output = output;
	}

	public FileMeta getOutputFile()
	{
		return fileMeta;
	}

	public String getOutput()
	{
		return output;
	}

}
