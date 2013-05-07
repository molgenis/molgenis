package org.molgenis.compute5.db.api;

public class RunStatusRequest
{
	private final String runName;

	public RunStatusRequest(String runName)
	{
		this.runName = runName;
	}

	public String getRunName()
	{
		return runName;
	}

}
