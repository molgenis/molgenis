package org.molgenis.compute5.db.api;

public class ResubmitFailedTasksResponse extends ApiResponse
{
	private int nrOfResubmittedTasks;

	public int getNrOfResubmittedTasks()
	{
		return nrOfResubmittedTasks;
	}

	public void setNrOfResubmittedTasks(int nrOfResubmittedTasks)
	{
		this.nrOfResubmittedTasks = nrOfResubmittedTasks;
	}

}
