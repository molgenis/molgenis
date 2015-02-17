package org.molgenis.compute.ui.job;

import org.molgenis.compute.ui.model.JobStatus;

public class JobStatusUpdate
{
	private final String jobId;
	private final JobStatus status;
	private String outputMessage;
	private String errorMessage;

	public JobStatusUpdate(String jobId, JobStatus status)
	{
		this.jobId = jobId;
		this.status = status;
	}

	public String getJobId()
	{
		return jobId;
	}

	public JobStatus getStatus()
	{
		return status;
	}

	public String getOutputMessage()
	{
		return outputMessage;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setOutputMessage(String outputMessage)
	{
		this.outputMessage = outputMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}


	@Override
	public String toString()
	{
		return "JobStatusUpdate{" +
				"jobId='" + jobId + '\'' +
				", status=" + status +
				'}';
	}
}
