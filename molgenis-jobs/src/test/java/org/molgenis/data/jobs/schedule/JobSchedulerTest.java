package org.molgenis.data.jobs.schedule;

import org.mockito.Mock;
import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.jobs.config.JobTestConfig;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.data.jobs.model.ScheduledJobFactory;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.util.GsonConfig;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

@ContextConfiguration(classes = { JobSchedulerTest.Config.class })
public class JobSchedulerTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private Config config;

	@Autowired
	private JobScheduler jobScheduler;

	@Autowired
	private DataService dataService;

	@Autowired
	private Scheduler quartzScheduler;

	@Autowired
	private ScheduledJobFactory scheduledJobFactory;

	@Mock
	private ScheduledJobType scheduledJobType;

	private String id = "id";
	private ScheduledJob scheduledJob;
	private JobKey jobKey = JobKey.jobKey(id, JobScheduler.SCHEDULED_JOB_GROUP);

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		config.resetMocks();
		reset(scheduledJobType);
		scheduledJob = scheduledJobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.setType(scheduledJobType);
	}

	@Test
	public void runNow() throws SchedulerException
	{
		when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
		when(quartzScheduler.checkExists(jobKey)).thenReturn(false);

		jobScheduler.runNow(id);

		verify(quartzScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void runNowUnknownEntity()
	{
		when(dataService.findOneById(SCHEDULED_JOB, id)).thenReturn(null);
		jobScheduler.runNow(id);
	}

	@Test
	public void runNowExists() throws SchedulerException
	{

		when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
		when(quartzScheduler.checkExists(jobKey)).thenReturn(true);

		jobScheduler.runNow(id);

		verify(quartzScheduler).triggerJob(jobKey);
	}

	@Test
	public void schedule() throws SchedulerException
	{
		ScheduledJob scheduledJob = scheduledJobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "	0/20 * * * * ?");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, true);
		scheduledJob.setType(scheduledJobType);

		when(quartzScheduler.checkExists(jobKey)).thenReturn(false);

		jobScheduler.schedule(scheduledJob);

		verify(quartzScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void scheduleInactive() throws SchedulerException
	{
		ScheduledJob scheduledJob = scheduledJobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "	0/20 * * * * ?");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, false);
		scheduledJob.setType(scheduledJobType);

		when(quartzScheduler.checkExists(jobKey)).thenReturn(false);

		jobScheduler.schedule(scheduledJob);

		verify(quartzScheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void scheduleInvalidCronExpression() throws SchedulerException
	{
		ScheduledJob scheduledJob = scheduledJobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "XXX");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, false);
		scheduledJob.setType(scheduledJobType);

		jobScheduler.schedule(scheduledJob);
	}

	@Test
	public void scheduleExisting() throws SchedulerException
	{
		ScheduledJob scheduledJob = scheduledJobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRON_EXPRESSION, "	0/20 * * * * ?");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, true);
		scheduledJob.setType(scheduledJobType);

		when(quartzScheduler.checkExists(jobKey)).thenReturn(true);
		when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);

		jobScheduler.schedule(scheduledJob);

		verify(quartzScheduler).deleteJob((jobKey));
		verify(quartzScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void unschedule() throws SchedulerException
	{
		String id = "id";
		when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
		jobScheduler.unschedule(id);
		verify(quartzScheduler).deleteJob((jobKey));
	}

	@Configuration
	@Import({ SecurityPackage.class, JobTestConfig.class, GsonConfig.class })
	public static class Config
	{
		@Autowired
		private DataService dataService;

		@Mock
		private Scheduler quartzScheduler;

		public Config()
		{
			initMocks(this);
		}

		public void resetMocks()
		{
			reset(quartzScheduler);
		}

		@Bean
		public JobScheduler jobScheduler()
		{
			return new JobScheduler(quartzScheduler(), dataService);
		}

		@Bean
		public Scheduler quartzScheduler()
		{
			return quartzScheduler;
		}
	}
}
