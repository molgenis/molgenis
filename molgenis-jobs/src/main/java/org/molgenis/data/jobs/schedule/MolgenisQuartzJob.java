package org.molgenis.data.jobs.schedule;

import org.molgenis.data.jobs.JobExecutor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Quartz {@link Job} executes a {@link org.molgenis.data.jobs.model.ScheduledJob} using the {@link JobExecutor}.
 */
@DisallowConcurrentExecution
public class MolgenisQuartzJob implements Job
{
	@Autowired
	private JobExecutor jobExecutor;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		jobExecutor.executeScheduledJob(context.getMergedJobDataMap().getString(JobScheduler.SCHEDULED_JOB_ID));
	}
}
