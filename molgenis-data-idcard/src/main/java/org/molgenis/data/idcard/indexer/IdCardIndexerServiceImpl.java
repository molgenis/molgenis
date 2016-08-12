package org.molgenis.data.idcard.indexer;

import com.google.common.io.BaseEncoding;
import org.molgenis.data.DataService;
import org.molgenis.data.idcard.model.IdCardIndexingEvent;
import org.molgenis.data.idcard.model.IdCardIndexingEventFactory;
import org.molgenis.data.idcard.model.IdCardIndexingEventStatus;
import org.molgenis.data.idcard.settings.IdCardIndexerSettings;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.idcard.indexer.IdCardIndexerJob.JOB_USERNAME;
import static org.molgenis.data.idcard.model.IdCardIndexingEventMetaData.ID_CARD_INDEXING_EVENT;

@Service
public class IdCardIndexerServiceImpl implements IdCardIndexerService, DisposableBean
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardIndexerServiceImpl.class);

	private static final String TRIGGER_GROUP = "idcard";
	private static final String JOB_GROUP = "idcard";
	private static final String INDEX_REBUILD_JOB_KEY = "indexRebuild";

	private final DataService dataService;
	private final IdCardIndexerSettings idCardIndexerSettings;
	private final Scheduler scheduler;
	private final IdCardIndexingEventFactory idCardIndexingEventFactory;
	/**
	 * Salt used to compute trigger name for user scheduled index rebuild jobs
	 */
	private final String triggerNameSalt;
	/**
	 * Unique trigger name used for system scheduled index rebuild job
	 */
	private final String triggerNameScheduled;

	@Autowired
	public IdCardIndexerServiceImpl(DataService dataService, IdCardIndexerSettings idCardBiobankIndexerSettings,
			Scheduler scheduler, IdCardIndexingEventFactory idCardIndexingEventFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.idCardIndexerSettings = requireNonNull(idCardBiobankIndexerSettings);
		this.scheduler = requireNonNull(scheduler);
		this.idCardIndexingEventFactory = idCardIndexingEventFactory;

		this.triggerNameSalt = UUID.randomUUID().toString();
		this.triggerNameScheduled = UUID.randomUUID().toString();
	}

	@Override
	public TriggerKey scheduleIndexRebuild() throws SchedulerException
	{
		TriggerKey triggerKey = getIndexRebuildTriggerKeyCurrentUser();
		if (!scheduler.checkExists(triggerKey))
		{
			TriggerBuilder<?> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(triggerKey)
					.usingJobData(JOB_USERNAME, SecurityUtils.getCurrentUsername()).startNow();
			scheduleIndexRebuildJob(triggerBuilder);
		}
		else
		{
			throw new RuntimeException("Index rebuild already scheduled");
		}
		return triggerKey;
	}

	@Override
	public TriggerState getIndexRebuildStatus(TriggerKey triggerKey) throws SchedulerException
	{
		return scheduler.getTriggerState(triggerKey);
	}

	@Override
	public void destroy() throws Exception
	{
		LOG.debug("Stopping scheduler (waiting for jobs to complete) ...");
		boolean waitForJobsToComplete = true;
		scheduler.shutdown(waitForJobsToComplete);
		LOG.info("Scheduler stopped");
	}

	/**
	 * Generate a trigger key for the current user. The trigger name is a URL safe string that can't be manipulated to
	 * retrieve status of other users.
	 */
	private TriggerKey getIndexRebuildTriggerKeyCurrentUser()
	{
		String rawTriggerName = triggerNameSalt + SecurityUtils.getCurrentUsername();

		// use MD5 hash to prevent ids that are too long
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		byte[] md5Hash = messageDigest.digest(rawTriggerName.getBytes(UTF_8));

		// convert MD5 hash to string ids that can be safely used in URLs
		String triggerName = BaseEncoding.base64Url().omitPadding().encode(md5Hash);

		return new TriggerKey(triggerName, TRIGGER_GROUP);
	}

	private JobKey getIndexRebuildJobKey()
	{
		return new JobKey(INDEX_REBUILD_JOB_KEY, JOB_GROUP);
	}

	private JobKey scheduleIndexRebuildJob(TriggerBuilder<?> triggerBuilder) throws SchedulerException
	{
		JobKey jobKey = getIndexRebuildJobKey();
		JobDetail jobDetail = scheduler.getJobDetail(jobKey);
		if (jobDetail == null)
		{
			jobDetail = JobBuilder.newJob(IdCardIndexerJob.class).withIdentity(jobKey).build();
			scheduler.scheduleJob(jobDetail, triggerBuilder.build());
		}
		else
		{
			scheduler.scheduleJob(triggerBuilder.forJob(jobDetail).build());
		}

		return jobKey;
	}

	@Override
	public void updateIndexerScheduler(boolean initScheduler) throws SchedulerException
	{
		JobKey jobKey = getIndexRebuildJobKey();
		if (idCardIndexerSettings.getBiobankIndexingEnabled())
		{
			String biobankIndexingFrequency = idCardIndexerSettings.getBiobankIndexingFrequency();
			TriggerBuilder<?> triggerBuilder = createCronTrigger(biobankIndexingFrequency);
			if (!scheduler.checkExists(jobKey))
			{
				LOG.info("Scheduling index rebuild job with cron [{}]", biobankIndexingFrequency);
				scheduleIndexRebuildJob(triggerBuilder);

				if (!initScheduler)
				{
					onIndexConfigurationUpdate("Indexing enabled");
				}
			}
			else
			{
				TriggerKey triggerKey = getIndexRebuildTriggerKeySystem();
				Trigger oldTrigger = scheduler.getTrigger(triggerKey);
				Trigger trigger = triggerBuilder.build();

				// trigger.equals(oldTrigger) doesn't return true when the cron expressions are equal
				if (trigger instanceof CronTriggerImpl && oldTrigger instanceof CronTriggerImpl
						&& !((CronTriggerImpl) trigger).getCronExpression()
						.equals(((CronTriggerImpl) oldTrigger).getCronExpression()))
				{
					LOG.info("Rescheduling index rebuild job with cron [{}]", biobankIndexingFrequency);
					scheduler.rescheduleJob(triggerKey, trigger);

					if (!initScheduler)
					{
						String updateMessage = String.format("Indexing schedule update [%s]", biobankIndexingFrequency);
						onIndexConfigurationUpdate(updateMessage);
					}
				}
			}
		}
		else
		{
			if (scheduler.checkExists(jobKey))
			{
				LOG.info("Deleting index rebuild job");
				scheduler.deleteJob(jobKey);

				if (!initScheduler)
				{
					onIndexConfigurationUpdate("Indexing disabled");
				}
			}
		}
	}

	private TriggerKey getIndexRebuildTriggerKeySystem()
	{
		return new TriggerKey(triggerNameScheduled, TRIGGER_GROUP);
	}

	private TriggerBuilder<?> createCronTrigger(String cronExpression) throws SchedulerException
	{
		return TriggerBuilder.newTrigger().withIdentity(getIndexRebuildTriggerKeySystem())
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression));
	}

	private void onIndexConfigurationUpdate(String updateMessage)
	{
		// write log event to db
		IdCardIndexingEvent idCardIndexingEvent = idCardIndexingEventFactory.create();
		idCardIndexingEvent.setStatus(IdCardIndexingEventStatus.CONFIGURATION_CHANGE);
		idCardIndexingEvent.setMessage(updateMessage);
		RunAsSystemProxy.runAsSystem(() ->
		{
			dataService.add(ID_CARD_INDEXING_EVENT, idCardIndexingEvent);
		});
	}
}
