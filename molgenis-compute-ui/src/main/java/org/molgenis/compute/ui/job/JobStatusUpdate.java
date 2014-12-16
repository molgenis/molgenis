package org.molgenis.compute.ui.job;

import javax.validation.constraints.NotNull;

import org.molgenis.compute.ui.model.JobStatus;

public class JobStatusUpdate
{
	private String jobId;

	@NotNull
	private JobStatus status;

	public String getJobId()
	{
		return jobId;
	}

	public void setJobId(String jobId)
	{
		this.jobId = jobId;
	}

	public JobStatus getStatus()
	{
		return status;
	}

	public void setStatus(JobStatus status)
	{
		this.status = status;
	}

}
