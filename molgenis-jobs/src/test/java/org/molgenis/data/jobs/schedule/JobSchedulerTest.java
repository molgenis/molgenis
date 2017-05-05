package org.molgenis.data.jobs.schedule;

import com.google.gson.Gson;
import org.molgenis.auth.SecurityPackage;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.jobs.config.JobTestConfig;
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
import static org.molgenis.data.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

@ContextConfiguration(classes = { JobSchedulerTest.Config.class })
public class JobSchedulerTest extends AbstractMolgenisSpringTest
{
	private static Scheduler schedulerMock = mock(Scheduler.class);

	@Autowired
	private JobScheduler jobScheduler;

	@Autowired
	private DataService dataService;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private ScheduledJobFactory jobFactory;

	private String id = "id";
	private String group = "FILE_INGEST";
	private ScheduledJob scheduledJob;
	private JobKey jobKey = JobKey.jobKey(id, group);

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(schedulerMock);
		scheduledJob = jobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.setType(ScheduledJobMetadata.JobType.FILE_INGEST);
	}

	@Test
	public void runNow() throws SchedulerException
	{
		when(dataService.findOneById(SCHEDULED_JOB, id, ScheduledJob.class)).thenReturn(scheduledJob);
		when(scheduler.checkExists(jobKey)).thenReturn(false);

		jobScheduler.runNow(id);

		verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
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
		when(scheduler.checkExists(jobKey)).thenReturn(true);

		jobScheduler.runNow(id);

		verify(scheduler).triggerJob(new JobKey(id, group));
	}

	@Test
	public void schedule() throws SchedulerException
	{
		ScheduledJob scheduledJob = jobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRONEXPRESSION, "	0/20 * * * * ?");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, true);

		when(scheduler.checkExists(jobKey)).thenReturn(false);

		jobScheduler.schedule(scheduledJob);

		verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void scheduleInactive() throws SchedulerException
	{
		ScheduledJob scheduledJob = jobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRONEXPRESSION, "	0/20 * * * * ?");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, false);

		when(scheduler.checkExists(jobKey)).thenReturn(false);

		jobScheduler.schedule(scheduledJob);

		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void scheduleInvalidCronExpression() throws SchedulerException
	{
		ScheduledJob scheduledJob = jobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRONEXPRESSION, "XXX");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, false);

		jobScheduler.schedule(scheduledJob);
	}

	@Test
	public void scheduleExisting() throws SchedulerException
	{
		ScheduledJob scheduledJob = jobFactory.create();
		scheduledJob.setId(id);
		scheduledJob.set(ScheduledJobMetadata.CRONEXPRESSION, "	0/20 * * * * ?");
		scheduledJob.set(ScheduledJobMetadata.NAME, "name");
		scheduledJob.set(ScheduledJobMetadata.ACTIVE, true);

		when(scheduler.checkExists(jobKey)).thenReturn(true);

		jobScheduler.schedule(scheduledJob);

		verify(scheduler).deleteJob((jobKey));
		verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void unschedule() throws SchedulerException
	{
		String id = "id";
		jobScheduler.unschedule(id);
		verify(scheduler).deleteJob((jobKey));
	}

	@Configuration
	@Import({ SecurityPackage.class, JobTestConfig.class, GsonConfig.class })
	public static class Config
	{
		@Autowired
		private Gson gson;

		@Autowired
		private DataService dataService;

		@Bean
		public JobScheduler jobScheduler()
		{
			return new JobScheduler(scheduler(), dataService, gson);
		}

		@Bean
		public Scheduler scheduler()
		{
			return schedulerMock;
		}
	}
}
