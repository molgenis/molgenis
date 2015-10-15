package org.molgenis.rdconnect;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.molgenis.data.Entity;
import org.molgenis.data.settings.SettingsEntityListener;
import org.molgenis.security.core.utils.SecurityUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.google.common.io.BaseEncoding;

@Service
public class IdCardBiobankServiceImpl implements IdCardBiobankService, ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardBiobankServiceImpl.class);

	private static final String TRIGGER_GROUP = "idcard";
	private static final String JOB_GROUP = "idcard";
	private static final String JOB_USERNAME = "username";
	private static final String INDEX_REBUILD_JOB_KEY = "indexRebuild";

	private final IdCardBiobankIndexer idCardBiobankIndexer;
	private final IdCardBiobankIndexerSettings idCardBiobankIndexerSettings;
	private final Scheduler scheduler;
	/**
	 * Salt used to compute trigger name for user scheduled index rebuild jobs
	 */
	private final String triggerNameSalt;
	/**
	 * Unique trigger name used for system scheduled index rebuild job
	 */
	private final String triggerNameScheduled;

	@Autowired
	public IdCardBiobankServiceImpl(IdCardBiobankIndexer idCardBiobankIndexer,
			IdCardBiobankIndexerSettings idCardBiobankIndexerSettings, Scheduler scheduler)
	{
		this.idCardBiobankIndexer = requireNonNull(idCardBiobankIndexer);
		this.idCardBiobankIndexerSettings = requireNonNull(idCardBiobankIndexerSettings);
		this.scheduler = requireNonNull(scheduler);

		this.triggerNameSalt = UUID.randomUUID().toString();
		this.triggerNameScheduled = UUID.randomUUID().toString();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		idCardBiobankIndexerSettings.addListener(new SettingsEntityListener()
		{
			@Override
			public void postUpdate(Entity entity)
			{
				try
				{
					updateIndexerScheduler(false);
				}
				catch (SchedulerException e)
				{
					throw new RuntimeException(e);
				}
			}
		});

		try
		{
			updateIndexerScheduler(true);
		}
		catch (SchedulerException e)
		{
			throw new RuntimeException(e);
		}
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
			jobDetail = JobBuilder.newJob(IdCardIndexJob.class).withIdentity(jobKey).build();
			scheduler.scheduleJob(jobDetail, triggerBuilder.build());
		}
		else
		{
			scheduler.scheduleJob(triggerBuilder.forJob(jobDetail).build());
		}

		return jobKey;
	}

	private void updateIndexerScheduler(boolean initScheduler) throws SchedulerException
	{
		JobKey jobKey = getIndexRebuildJobKey();
		if (idCardBiobankIndexerSettings.getBiobankIndexingEnabled())
		{
			String biobankIndexingFrequency = idCardBiobankIndexerSettings.getBiobankIndexingFrequency();
			TriggerBuilder<?> triggerBuilder = createCronTrigger(biobankIndexingFrequency);
			if (!scheduler.checkExists(jobKey))
			{
				LOG.info("Scheduling index rebuild job with cron [{}]", biobankIndexingFrequency);
				scheduleIndexRebuildJob(triggerBuilder);

				if (!initScheduler)
				{
					idCardBiobankIndexer.onIndexConfigurationUpdate("Indexing enabled");
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
						idCardBiobankIndexer.onIndexConfigurationUpdate(updateMessage);
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
					idCardBiobankIndexer.onIndexConfigurationUpdate("Indexing disabled");
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

	@DisallowConcurrentExecution
	public static class IdCardIndexJob implements Job
	{
		@Autowired
		private IdCardBiobankIndexer idCardBiobankIndexer;

		@Override
		public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
		{
			try
			{
				LOG.info("Executing scheduled rebuild index job ...");
				String username = jobExecutionContext.getMergedJobDataMap().getString(JOB_USERNAME);
				idCardBiobankIndexer.rebuildIndex(username);
				LOG.info("Executed scheduled rebuild index job");
			}
			catch (Throwable t)
			{
				LOG.error("An error occured rebuilding index", t);
			}

		}
	}
}
