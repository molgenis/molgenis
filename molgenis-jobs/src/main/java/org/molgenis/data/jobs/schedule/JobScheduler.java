package org.molgenis.data.jobs.schedule;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;

import static java.text.MessageFormat.format;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Schedules, runs and unschedules {@link ScheduledJob}s.
 */
@Component
public class JobScheduler
{
	private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);
	public static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>()
	{
	}.getType();
	private final Scheduler quartzScheduler;
	private final DataService dataService;
	private Gson gson;

	@Autowired
	public JobScheduler(Scheduler quartzScheduler, DataService dataService, Gson gson)
	{
		this.quartzScheduler = requireNonNull(quartzScheduler);
		this.dataService = requireNonNull(dataService);
		this.gson = requireNonNull(gson);
	}

	/**
	 * Executes a {@link ScheduledJob} immediately.
	 *
	 * @param scheduledJobId ID of the {@link ScheduledJob} to run
	 */
	public synchronized void runNow(String scheduledJobId)
	{
		ScheduledJob scheduledJob = dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);
		if (scheduledJob == null)
		{
			throw new UnknownEntityException(format("Unknown ScheduledJob entity with id ''{0}''", scheduledJobId));
		}

		try
		{
			JobKey jobKey = new JobKey(scheduledJobId, scheduledJob.getGroup());
			if (quartzScheduler.checkExists(jobKey))
			{
				// Run job now
				quartzScheduler.triggerJob(jobKey);
			}
			else
			{
				// Schedule with 'now' trigger
				Trigger trigger = newTrigger().withIdentity(scheduledJobId, scheduledJob.getGroup()).startNow().build();
				schedule(scheduledJob, trigger);
			}
		}
		catch (SchedulerException e)
		{
			LOG.error("Error runNow ScheduledJob", e);
			throw new MolgenisDataException("Error job runNow", e);
		}
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
			if (quartzScheduler.checkExists(new JobKey(id, scheduledJob.getGroup())))
			{
				unschedule(id);
			}

			// If not active, do not schedule it
			if (!scheduledJob.getBoolean(ScheduledJobMetadata.ACTIVE))
			{
				return;
			}

			// Schedule with 'cron' trigger
			Trigger trigger = newTrigger().withIdentity(id, scheduledJob.getGroup())
					.withSchedule(cronSchedule(cronExpression)).build();
			schedule(scheduledJob, trigger);

			LOG.info("Scheduled FileIngesterJob '{}' with trigger '{}'", name, trigger);
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
	 * @param scheduledJobId
	 */
	public synchronized void unschedule(String scheduledJobId)
	{
		ScheduledJob scheduledJob = dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);
		if (scheduledJob == null)
		{
			String message = format("No {0} entity found with id {1}.", SCHEDULED_JOB, scheduledJobId);
			LOG.error(message);
			throw new UnknownEntityException(message);
		}
		try
		{
			quartzScheduler.deleteJob(new JobKey(scheduledJob.getId(), scheduledJob.getGroup()));
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
		Map<String, Object> parameters = gson.fromJson(scheduledJob.getParameters(), MAP_TOKEN);
		parameters.forEach(jobDataMap::put);
		JobDetail job = newJob(scheduledJob.getJobClass()).withIdentity(scheduledJob.getId(), scheduledJob.getGroup())
				.usingJobData(jobDataMap).build();
		quartzScheduler.scheduleJob(job, trigger);
	}
}
