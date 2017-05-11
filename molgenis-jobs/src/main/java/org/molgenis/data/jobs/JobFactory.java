package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.JobType;

/**
 * @param <T> Type of the JobExecutions that this factory understands.
 */
public interface JobFactory<T extends JobExecution>
{
	/**
	 * Creates a Job instance.
	 *
	 * @param jobExecution {@link JobExecution} with job parameters
	 * @return the job
	 */
	Job createJob(T jobExecution);

	/**
	 * Creates a JobType to use when scheduling jobs.
	 */
	JobType getJobType();
}
