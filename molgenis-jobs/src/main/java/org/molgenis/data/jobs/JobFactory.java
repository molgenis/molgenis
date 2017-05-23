package org.molgenis.data.jobs;

import org.molgenis.data.jobs.model.JobExecution;

/**
 * @param <T> Type of the JobExecutions that this factory understands.
 */
public abstract class JobFactory<T extends JobExecution>
{
	/**
	 * Creates a Job instance.
	 *
	 * @param jobExecution {@link JobExecution} with job parameters
	 * @return the job
	 */
	public abstract Job createJob(T jobExecution);
}
