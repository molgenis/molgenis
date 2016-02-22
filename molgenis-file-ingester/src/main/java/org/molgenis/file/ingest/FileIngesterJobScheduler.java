package org.molgenis.file.ingest;

import static org.quartz.CronScheduleBuilder.cronSchedule;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.quartz.CronExpression;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
	private static final String TRIGGER_GROUP = "fileingest";
	private static final String JOB_GROUP = "fileingest";
	private static final Logger LOG = LoggerFactory.getLogger(FileIngesterJobScheduler.class);
	private final Scheduler scheduler;
	private final DataService dataService;

	@Autowired
	public FileIngesterJobScheduler(Scheduler scheduler, DataService dataService)
	{
		this.scheduler = scheduler;
		this.dataService = dataService;
	}

	public synchronized void schedule(Entity fileIngest)
	{
		String name = fileIngest.getString(FileIngestMetaData.NAME);
		try
		{
			String id = fileIngest.getString(FileIngestMetaData.ID);
			String cronExpression = fileIngest.getString(FileIngestMetaData.CRONEXPRESSION);
			if (!CronExpression.isValidExpression(cronExpression))
			{
				throw new MolgenisValidationException(
Sets.newHashSet(new ConstraintViolation(
						"Invalid cronexpression '" + cronExpression + "'",
						null)));
			}

			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put(FileIngesterJob.ENTITY_KEY, fileIngest);
			JobDetail job = JobBuilder.newJob(FileIngesterJob.class).withIdentity(id, JOB_GROUP)
					.usingJobData(jobDataMap).build();

			if (scheduler.checkExists(job.getKey()))
			{
				unschedule(id);
			}

			if (!fileIngest.getBoolean(FileIngestMetaData.ACTIVE))
			{
				return;
			}

			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(id, TRIGGER_GROUP)
					.withSchedule(cronSchedule(cronExpression)).build();

			scheduler.scheduleJob(job, trigger);
			LOG.info("Scheduled FileIngesterJob '{}' with cronexpression '{}'", name, cronExpression);
		}
		catch (SchedulerException e)
		{
			LOG.error("Error scheduling FileIngesterJob '" + name + "'", e);
			throw new FileIngestException("Error scheduling job", e);
		}
	}

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
		RunAsSystemProxy.runAsSystem(() -> dataService.findAll(FileIngestMetaData.ENTITY_NAME).forEach(this::schedule));
	}

}
