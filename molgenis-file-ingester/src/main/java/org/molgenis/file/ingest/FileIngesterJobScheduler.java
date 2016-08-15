package org.molgenis.file.ingest;

import com.google.common.collect.Sets;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.ingest.execution.FileIngestException;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.FILE_INGEST;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Schedule and unschedule FileIngestJobs
 */
@Component
public class FileIngesterJobScheduler
{
	public static final String TRIGGER_GROUP = "fileingest";
	public static final String JOB_GROUP = "fileingest";
	private static final Logger LOG = LoggerFactory.getLogger(FileIngesterJobScheduler.class);
	private final Scheduler scheduler;
	private final DataService dataService;

	@Autowired
	public FileIngesterJobScheduler(Scheduler scheduler, DataService dataService)
	{
		this.scheduler = requireNonNull(scheduler);
		this.dataService = requireNonNull(dataService);
	}

	/**
	 * Execute FileIngest job immediately
	 *
	 * @param fileIngestId
	 */
	public synchronized void runNow(String fileIngestId)
	{
		FileIngest fileIngest = dataService.findOneById(FILE_INGEST, fileIngestId, FileIngest.class);
		if (fileIngest == null)
		{
			throw new UnknownEntityException("Unknown FileIngest entity id '" + fileIngestId + "'");
		}

		try
		{
			JobKey jobKey = new JobKey(fileIngestId, JOB_GROUP);
			if (scheduler.checkExists(jobKey))
			{
				// Run job now
				scheduler.triggerJob(jobKey);
			}
			else
			{
				// Schedule with 'now' trigger
				Trigger trigger = newTrigger().withIdentity(fileIngestId, TRIGGER_GROUP).startNow().build();
				schedule(fileIngestId, trigger);
			}
		}
		catch (SchedulerException e)
		{
			LOG.error("Error runNow FileIngesterJob", e);
			throw new FileIngestException("Error job runNow", e);
		}
	}

	/**
	 * Schedule a FileIngest job with a cron expression defined in the entity.
	 * <p>
	 * Reschedules job if the job already exists.
	 * <p>
	 * If active is false, it unschedules the job
	 *
	 * @param fileIngest
	 */
	public synchronized void schedule(Entity fileIngest)
	{
		String id = fileIngest.getString(FileIngestMetaData.ID);
		String cronExpression = fileIngest.getString(FileIngestMetaData.CRONEXPRESSION);
		String name = fileIngest.getString(FileIngestMetaData.NAME);

		// Validate cron expression
		if (!CronExpression.isValidExpression(cronExpression))
		{
			throw new MolgenisValidationException(
					Sets.newHashSet(new ConstraintViolation("Invalid cronexpression '" + cronExpression + "'", null)));
		}

		try
		{
			// If already scheduled, remove it from the scheduler
			if (scheduler.checkExists(new JobKey(id, JOB_GROUP)))
			{
				unschedule(id);
			}

			// If not active, do not schedule it
			if (!fileIngest.getBoolean(FileIngestMetaData.ACTIVE))
			{
				return;
			}

			// Schedule with 'cron' trigger
			Trigger trigger = newTrigger().withIdentity(id, TRIGGER_GROUP).withSchedule(cronSchedule(cronExpression))
					.build();
			schedule(fileIngest.getIdValue().toString(), trigger);

			LOG.info("Scheduled FileIngesterJob '{}' with trigger '{}'", name, trigger);
		}
		catch (SchedulerException e)
		{
			LOG.error("Error schedule job", e);
			throw new FileIngestException("Error schedule job", e);
		}
	}

	/**
	 * Remove a job from the scheduler
	 *
	 * @param fileIngestId
	 */
	public synchronized void unschedule(String fileIngestId)
	{
		try
		{
			scheduler.deleteJob(new JobKey(fileIngestId, JOB_GROUP));
		}
		catch (SchedulerException e)
		{
			LOG.error("Error unschedule FileIngesterJob '" + fileIngestId + "'", e);
			throw new FileIngestException("Error unscheduling job", e);
		}
	}

	private void schedule(String id, Trigger trigger) throws SchedulerException
	{
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(FileIngesterQuartzJob.ENTITY_KEY, id);
		JobDetail job = newJob(FileIngesterQuartzJob.class).withIdentity(id, JOB_GROUP).usingJobData(jobDataMap)
				.build();

		scheduler.scheduleJob(job, trigger);
	}
}
