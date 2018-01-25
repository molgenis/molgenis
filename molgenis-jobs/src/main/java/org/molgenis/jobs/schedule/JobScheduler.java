package org.molgenis.jobs.schedule;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobMetadata;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static java.text.MessageFormat.format;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Schedules and unschedules {@link ScheduledJob}s with Quartz.
 */
@Service
public class JobScheduler
{
	/**
	 * the key under which the JobScheduler puts the ScheduledJob ID in the JobDataMap
	 */
	static final String SCHEDULED_JOB_ID = "scheduledJobID";
	static final String SCHEDULED_JOB_GROUP = Scheduler.DEFAULT_GROUP; // Group under which the jobs are scheduled in Quartz.
	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);

	private final Scheduler quartzScheduler;
	private final DataService dataService;

	JobScheduler(Scheduler quartzScheduler, DataService dataService)
	{
		this.quartzScheduler = requireNonNull(quartzScheduler);
		this.dataService = requireNonNull(dataService);
	}

	/**
	 * Executes a {@link ScheduledJob} immediately.
	 *
	 * @param scheduledJobId ID of the {@link ScheduledJob} to run
	 */
	public synchronized void runNow(String scheduledJobId)
	{
		ScheduledJob scheduledJob = getJob(scheduledJobId);

		try
		{
			JobKey jobKey = new JobKey(scheduledJobId, SCHEDULED_JOB_GROUP);
			if (quartzScheduler.checkExists(jobKey))
			{
				// Run job now
				quartzScheduler.triggerJob(jobKey);
			}
			else
			{
				// Schedule with 'now' trigger
				Trigger trigger = newTrigger().withIdentity(scheduledJobId, SCHEDULED_JOB_GROUP).startNow().build();
				schedule(scheduledJob, trigger);
			}
		}
		catch (SchedulerException e)
		{
			LOG.error("Error runNow ScheduledJob", e);
			throw new MolgenisDataException("Error job runNow", e);
		}
	}

	private ScheduledJob getJob(String scheduledJobId)
	{
		ScheduledJob scheduledJob = dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);
		if (scheduledJob == null)
		{
			throw new UnknownEntityException(format("Unknown ScheduledJob entity with id ''{0}''", scheduledJobId));
		}
		return scheduledJob;
	}

	/**
	 * Schedule a {@link ScheduledJob} with a cron expression defined in the entity.
	 * <p>
	 * Reschedules job if the job already exists.
	 * <p>
	 * If active is false, it unschedules the job
	 *
	 * @param scheduledJob the {@link ScheduledJob} to schedule
	 */
	public synchronized void schedule(ScheduledJob scheduledJob)
	{
		String id = scheduledJob.getId();
		String cronExpression = scheduledJob.getCronExpression();
		String name = scheduledJob.getName();

		// Validate cron expression
		if (!CronExpression.isValidExpression(cronExpression))
		{
			throw new MolgenisValidationException(
					singleton(new ConstraintViolation("Invalid cronexpression '" + cronExpression + "'", null)));
		}

		try
		{
			// If already scheduled, remove it from the quartzScheduler
			if (quartzScheduler.checkExists(new JobKey(id, SCHEDULED_JOB_GROUP)))
			{
				unschedule(id);
			}

			// If not active, do not schedule it
			if (!scheduledJob.getBoolean(ScheduledJobMetadata.ACTIVE))
			{
				return;
			}

			// Schedule with 'cron' trigger
			Trigger trigger = newTrigger().withIdentity(id, SCHEDULED_JOB_GROUP)
										  .withSchedule(cronSchedule(cronExpression))
										  .build();
			schedule(scheduledJob, trigger);

			LOG.info("Scheduled Job '{}' with trigger '{}'", name, trigger);
		}
		catch (SchedulerException e)
		{
			LOG.error("Error schedule job", e);
			throw new ScheduledJobException("Error schedule job", e);
		}
	}

	/**
	 * Remove a job from the quartzScheduler
	 *
	 * @param scheduledJobId ID of the ScheduledJob to unschedule
	 */
	synchronized void unschedule(String scheduledJobId)
	{
		try
		{
			quartzScheduler.deleteJob(new JobKey(scheduledJobId, SCHEDULED_JOB_GROUP));
		}
		catch (SchedulerException e)
		{
			String message = format("Error deleting ScheduledJob ''{0}''", scheduledJobId);
			LOG.error(message, e);
			throw new ScheduledJobException(message, e);
		}
	}

	private void schedule(ScheduledJob scheduledJob, Trigger trigger) throws SchedulerException
	{
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(SCHEDULED_JOB_ID, scheduledJob.getIdValue());
		JobDetail job = newJob(MolgenisQuartzJob.class).withIdentity(scheduledJob.getId(), SCHEDULED_JOB_GROUP)
													   .usingJobData(jobDataMap)
													   .build();
		quartzScheduler.scheduleJob(job, trigger);
	}

	public void scheduleJobs()
	{
		dataService.findAll(SCHEDULED_JOB, ScheduledJob.class).forEach(this::schedule);
	}
}
