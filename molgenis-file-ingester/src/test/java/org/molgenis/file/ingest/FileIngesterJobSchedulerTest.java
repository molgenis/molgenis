package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestFactory;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.FILE_INGEST;

@ContextConfiguration(classes = { FileIngesterJobSchedulerTest.Config.class })
public class FileIngesterJobSchedulerTest extends AbstractMolgenisSpringTest
{
	private static DataService dataServiceMock = mock(DataService.class);
	private static Scheduler schedulerMock = mock(Scheduler.class);

	@Autowired
	private FileIngesterJobScheduler fileIngesterJobScheduler;

	@Autowired
	private DataService dataService;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private FileIngestFactory fileIngestFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		reset(dataServiceMock);
		reset(schedulerMock);
	}

	@Test
	public void runNow() throws SchedulerException
	{
		String id = "id";
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setId(id);

		when(dataService.findOneById(FILE_INGEST, id, FileIngest.class)).thenReturn(fileIngest);
		when(scheduler.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(false);

		fileIngesterJobScheduler.runNow(id);

		verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test(expectedExceptions = UnknownEntityException.class)
	public void runNowUnknownEntity()
	{
		String id = "id";
		when(dataService.findOneById(FILE_INGEST, id)).thenReturn(null);
		fileIngesterJobScheduler.runNow(id);
	}

	@Test
	public void runNowExists() throws SchedulerException
	{
		String id = "id";
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setId(id);

		when(dataService.findOneById(FILE_INGEST, id, FileIngest.class)).thenReturn(fileIngest);
		when(scheduler.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(true);

		fileIngesterJobScheduler.runNow(id);

		verify(scheduler).triggerJob(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP));
	}

	@Test
	public void schedule() throws SchedulerException
	{
		String id = "id";
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setId(id);
		fileIngest.setCronExpression("	0/20 * * * * ?");
		fileIngest.setName("name");
		fileIngest.setActive(true);

		when(scheduler.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(false);

		fileIngesterJobScheduler.schedule(fileIngest);

		verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void scheduleInactive() throws SchedulerException
	{
		String id = "id";
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setId(id);
		fileIngest.setCronExpression("	0/20 * * * * ?");
		fileIngest.setName("name");
		fileIngest.setActive(false);

		when(scheduler.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(false);

		fileIngesterJobScheduler.schedule(fileIngest);

		verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test(expectedExceptions = MolgenisValidationException.class)
	public void scheduleInvalidCronExpression() throws SchedulerException
	{
		String id = "id";
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setId(id);
		fileIngest.setCronExpression("XXX");
		fileIngest.setName("name");
		fileIngest.setActive(false);

		fileIngesterJobScheduler.schedule(fileIngest);
	}

	@Test
	public void scheduleExisting() throws SchedulerException
	{
		String id = "id";
		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setId(id);
		fileIngest.setCronExpression("	0/20 * * * * ?");
		fileIngest.setName("name");
		fileIngest.setActive(true);

		when(scheduler.checkExists(new JobKey(id, FileIngesterJobScheduler.JOB_GROUP))).thenReturn(true);

		fileIngesterJobScheduler.schedule(fileIngest);

		verify(scheduler).deleteJob((new JobKey(id, FileIngesterJobScheduler.JOB_GROUP)));
		verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	@Test
	public void unschedule() throws SchedulerException
	{
		String id = "id";
		fileIngesterJobScheduler.unschedule(id);
		verify(scheduler).deleteJob((new JobKey(id, FileIngesterJobScheduler.JOB_GROUP)));
	}

	@Configuration
	@ComponentScan({ "org.molgenis.file.ingest.meta", "org.molgenis.security.owned", "org.molgenis.file.model",
			"org.molgenis.data.jobs.model", "org.molgenis.auth" })
	public static class Config
	{
		@Bean
		public FileIngesterJobScheduler fileIngesterJobScheduler()
		{
			return new FileIngesterJobScheduler(scheduler(), dataService());
		}

		@Bean
		public Scheduler scheduler()
		{
			return schedulerMock;
		}

		@Bean
		public DataService dataService()
		{
			return dataServiceMock;
		}
	}
}
