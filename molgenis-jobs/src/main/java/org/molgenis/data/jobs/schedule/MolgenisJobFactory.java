package org.molgenis.data.jobs.schedule;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;

/**
 * Interface to be implemented by Job factories so that their jobs can be scheduled by Quartz.
 */
public interface MolgenisJobFactory
{
	/**
	 * @return EntityType of the JobExecutions of this job factory
	 */
	EntityType getJobExecutionType();

	/**
	 * The actual factory method
	 *
	 * @param jobExecution the JobExecution for the Job to create
	 * @return the created {@link JobExecution}
	 */
	Job createJob(JobExecution jobExecution);
}
