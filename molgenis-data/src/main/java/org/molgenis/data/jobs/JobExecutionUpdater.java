package org.molgenis.data.jobs;

/**
 * Updates {@link JobExecution} details in the repository. Runs with system privileges in a separate transaction.
 */
public interface JobExecutionUpdater
{
	void update(JobExecution jobExecution);
}
