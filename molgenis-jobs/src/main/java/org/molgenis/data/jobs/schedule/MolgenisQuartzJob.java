package org.molgenis.data.jobs.schedule;

import org.molgenis.data.jobs.JobExecutor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Objects.requireNonNull;

/**
 * Quartz {@link Job} executes a {@link org.molgenis.data.jobs.model.ScheduledJob} using the {@link JobExecutor}.
 * This job's setters get autowired after construction by the{@link org.molgenis.scheduler.AutowiringSpringBeanJobFactory}.
 * Only one MolgenisQuartzJob of a specific {@link org.quartz.JobKey} can run at a time.
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@DisallowConcurrentExecution
public class MolgenisQuartzJob implements Job
{
	private JobExecutor jobExecutor;

	@Autowired
	public void setJobExecutor(JobExecutor jobExecutor)
	{
		this.jobExecutor = requireNonNull(jobExecutor);
	}

	@Override
	public void execute(JobExecutionContext context)
	{
		jobExecutor.executeScheduledJob(context.getMergedJobDataMap().getString(JobScheduler.SCHEDULED_JOB_ID));
	}
}
