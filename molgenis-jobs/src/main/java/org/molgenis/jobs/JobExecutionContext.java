package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;

/**
 * Keeps track of the currently executing {@link JobExecution}.
 */
public class JobExecutionContext
{
	private JobExecutionContext()
	{

	}

	public static final ThreadLocal<JobExecution> jobContextHolder = new ThreadLocal<>();

	public static void set(JobExecution jobExecution)
	{
		jobContextHolder.set(jobExecution);
	}

	public static void unset()
	{
		jobContextHolder.remove();
	}

	public static JobExecution get()
	{
		return jobContextHolder.get();
	}
}
