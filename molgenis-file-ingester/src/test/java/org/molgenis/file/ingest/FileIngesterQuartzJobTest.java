package org.molgenis.file.ingest;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.config.UserTestConfig;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.file.ingest.config.FileIngestTestConfig;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestJobExecutionMetaData;
import org.molgenis.file.model.FileMeta;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

@ContextConfiguration(classes = { FileIngesterQuartzJobTest.Config.class })
public class FileIngesterQuartzJobTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private FileIngesterQuartzJob fileIngesterQuartzJob;

	@Autowired
	private DataService dataService;

	@Autowired
	private FileIngestJobFactory fileIngestJobFactory;

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private UserFactory userFactory;

	@Mock
	private JobExecutionContext jobExecutionContext;

	@BeforeClass
	public void beforeClass()
	{
		MockitoAnnotations.initMocks(this);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		Mockito.reset(jobExecutionContext);
	}

	@Test
	public void quartzJobRetrievesFileIngestCreatesJobExecutionEntityAndJobAndRunsJob() throws Exception
	{
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(ScheduledJobMetadata.FAILURE_EMAIL, "x@y.z");
		jobDataMap.put(ScheduledJobMetadata.SUCCESS_EMAIL, "a@b.c");
		jobDataMap.put(FileIngestJobExecutionMetaData.ENTITY_META_DATA, "TypeTest");

		when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

		EntityType targetEntity = entityTypeFactory.create("TypeTest");
		when(dataService.getEntityType("TypeTest")).thenReturn(targetEntity);

		@SuppressWarnings("unchecked")
		Query<User> queryMock = Mockito.mock(Query.class);
		User admin = userFactory.create();
		admin.setUsername("admin");
		when(dataService.query(UserMetaData.USER, User.class)).thenReturn(queryMock);
		when(queryMock.eq(UserMetaData.USERNAME, "admin")).thenReturn(queryMock);
		when(queryMock.findOne()).thenReturn(admin);

		ArgumentCaptor<FileIngestJobExecution> fileIngestJobExecutionCaptor = ArgumentCaptor
				.forClass(FileIngestJobExecution.class);
		FileIngestJob fileIngestJobMock = mock(FileIngestJob.class);
		when(fileIngestJobFactory.createJob(fileIngestJobExecutionCaptor.capture())).thenReturn(fileIngestJobMock);

		FileMeta fileMeta = mock(FileMeta.class);
		when(fileIngestJobMock.call()).thenReturn(fileMeta);

		fileIngesterQuartzJob.execute(jobExecutionContext);

		// check that properly filled jobExecution entity was fed to the factory
		FileIngestJobExecution jobExecution = fileIngestJobExecutionCaptor.getValue();
		assertEquals(jobExecution.getFailureEmail(), new String[] { "x@y.z" });
		assertEquals(jobExecution.getFailureEmail(), new String[] { "x@y.z" });
		assertSame(jobExecution.getTargetEntity(), targetEntity);
	}

	@Configuration
	@Import({ UserTestConfig.class, FileIngestTestConfig.class })
	public static class Config
	{
		@Bean
		public FileIngesterQuartzJob fileIngesterQuartzJob()
		{
			return new FileIngesterQuartzJob();
		}

		@Bean
		public FileIngestJobFactory fileIngestJobFactory()
		{
			return mock(FileIngestJobFactory.class);
		}
	}
}
