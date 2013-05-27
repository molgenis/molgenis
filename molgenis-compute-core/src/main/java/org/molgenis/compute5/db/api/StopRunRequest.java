package org.molgenis.compute5.db.api;

public class StopRunRequest
{
	private final String runName;

	public StopRunRequest(String runName)
	{
		this.runName = runName;
	}

	public String getRunName()
	{
		return runName;
	}

}
