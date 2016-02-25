package org.molgenis.file.ingest;

import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * Schedule and unschedule FileIngestJobs
 */
@Component
public class FileIngesterJobScheduler implements ApplicationListener<ContextRefreshedEvent>
{
	public static final String TRIGGER_GROUP = "fileingest";
	public static final String JOB_GROUP = "fileingest";
	private static final Logger LOG = LoggerFactory.getLogger(FileIngesterJobScheduler.class);
	private final Scheduler scheduler;
	private final DataService dataService;

	@Autowired
	public FileIngesterJobScheduler(Scheduler scheduler, DataService dataService)
	{
		this.scheduler = scheduler;
		this.dataService = dataService;
	}

	/**
	 * Execute FileIngest job immediately
	 * 
	 * @param fileIngestId
	 */
	public synchronized void runNow(String fileIngestId)
	{
		Entity fileIngest = dataService.findOne(FileIngestMetaData.ENTITY_NAME, fileIngestId);
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
				schedule(fileIngest, trigger);
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
	 * 
	 * Reschedules job if the job already exists.
	 * 
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
			throw new MolgenisValidationException(Sets.newHashSet(new ConstraintViolation("Invalid cronexpression '"
					+ cronExpression + "'", null)));
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
			schedule(fileIngest, trigger);

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

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		// Schedule all FileIngest jobs
		runAsSystem(() -> dataService.findAll(FileIngestMetaData.ENTITY_NAME).forEach(this::schedule));
	}

	private void schedule(Entity fileIngest, Trigger trigger) throws SchedulerException
	{
		String id = fileIngest.getString(FileIngestMetaData.ID);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(FileIngesterJob.ENTITY_KEY, fileIngest);
		JobDetail job = newJob(FileIngesterJob.class).withIdentity(id, JOB_GROUP).usingJobData(jobDataMap).build();

		scheduler.scheduleJob(job, trigger);
	}
}
