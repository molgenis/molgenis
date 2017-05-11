package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;

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
	 * Creates a ScheduledJobType to use when scheduling jobs.
	 */
	String getJobType();
}
