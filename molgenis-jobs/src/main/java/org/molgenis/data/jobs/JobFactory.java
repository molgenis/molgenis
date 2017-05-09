package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobType;

public interface JobFactory
{
	Job createJob(JobExecution jobExecution);

	JobType getJobType();
}
