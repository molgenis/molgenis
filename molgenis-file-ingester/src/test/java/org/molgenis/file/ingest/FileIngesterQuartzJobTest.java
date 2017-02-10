package org.molgenis.file.ingest;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.auth.User;
import org.molgenis.auth.UserFactory;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.file.ingest.execution.FileIngestJob;
import org.molgenis.file.ingest.execution.FileIngestJobFactory;
import org.molgenis.file.ingest.meta.FileIngest;
import org.molgenis.file.ingest.meta.FileIngestFactory;
import org.molgenis.file.ingest.meta.FileIngestJobExecution;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.file.model.FileMeta;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

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
	private FileIngestFactory fileIngestFactory;

	@Autowired
	private UserFactory userFactory;

	private JobExecutionContext contextMock;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		contextMock = mock(JobExecutionContext.class);
	}

	@Test
	public void quartzJobRetrievesFileIngestCreatesJobExecutionEntityAndJobAndRunsJob() throws Exception
	{
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(FileIngesterQuartzJob.ENTITY_KEY, "abcde");
		when(contextMock.getMergedJobDataMap()).thenReturn(jobDataMap);

		EntityType targetEntity = entityTypeFactory.create();
		targetEntity.setName("TypeTest");

		FileIngest fileIngest = fileIngestFactory.create();
		fileIngest.setFailureEmail("x@y.z");
		fileIngest.setTargetEntity(targetEntity);
		when(dataService.findOneById(FileIngestMetaData.FILE_INGEST, "abcde", FileIngest.class)).thenReturn(fileIngest);

		Query<User> queryMock = Mockito.mock(Query.class);
		User admin = userFactory.create();
		admin.setUsername("admin");
		when(dataService.query(UserMetaData.USER, User.class)).thenReturn(queryMock);
		when(queryMock.eq(UserMetaData.USERNAME, "admin")).thenReturn(queryMock);
		when(queryMock.findOne()).thenReturn(admin);

		ArgumentCaptor<FileIngestJobExecution> captor = ArgumentCaptor.forClass(FileIngestJobExecution.class);
		FileIngestJob fileIngestJobMock = mock(FileIngestJob.class);
		when(fileIngestJobFactory.createJob(captor.capture())).thenReturn(fileIngestJobMock);

		FileMeta fileMeta = mock(FileMeta.class);
		when(fileIngestJobMock.call()).thenReturn(fileMeta);

		fileIngesterQuartzJob.execute(contextMock);

		// check that properly filled jobExecution entity was fed to the factory
		FileIngestJobExecution jobExecution = captor.getValue();
		assertEquals(jobExecution.getFailureEmail(), new String[] { "x@y.z" });
		assertEquals(jobExecution.getFileIngest(), fileIngest);
		verify(dataService).add("sys_FileMeta", fileMeta);
	}

	@Configuration
	@ComponentScan({ "org.molgenis.file.ingest.meta", "org.molgenis.security.owned", "org.molgenis.file.model",
			"org.molgenis.data.jobs.model", "org.molgenis.auth" })
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

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}
}
