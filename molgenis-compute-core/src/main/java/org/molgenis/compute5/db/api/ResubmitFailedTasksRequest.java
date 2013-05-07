package org.molgenis.compute5.db.api;

public class ResubmitFailedTasksRequest
{
	private final String runName;

	public ResubmitFailedTasksRequest(String runName)
	{
		this.runName = runName;
	}

	public String getRunName()
	{
		return runName;
	}

}
