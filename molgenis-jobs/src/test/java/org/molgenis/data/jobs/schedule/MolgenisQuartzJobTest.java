package org.molgenis.data.jobs.schedule;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.config.UserTestConfig;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.config.JobTestConfig;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.molgenis.data.meta.model.EntityType;
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

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = { MolgenisQuartzJobTest.Config.class })
public class MolgenisQuartzJobTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private MolgenisQuartzJob molgenisQuartzJob;

	@Autowired
	private MolgenisJobFactory molgenisJobFactory;

	@Mock
	private JobExecutionContext jobExecutionContext;

	@Autowired
	private EntityManager entityManager;

	@Mock
	private EntityType jobExecutionType;

	@Mock
	private Job job;

	@Mock
	TestJobExecution jobExecution;

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
	public void quartzJobCreatesJobAndRunsIt() throws Exception
	{
		when(molgenisJobFactory.getJobExecutionType()).thenReturn(jobExecutionType);

		when(entityManager.create(jobExecutionType, EntityManager.CreationMode.POPULATE)).thenReturn(jobExecution);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(ScheduledJobMetadata.FAILURE_EMAIL, "x@y.z");
		jobDataMap.put(ScheduledJobMetadata.SUCCESS_EMAIL, "a@b.c");
		jobDataMap.put("param1", "param1Value");
		jobDataMap.put("param2", 2);

		when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

		when(molgenisJobFactory.createJob(jobExecution)).thenReturn(job);

		FileMeta fileMeta = mock(FileMeta.class);
		when(job.call()).thenReturn(fileMeta);

		molgenisQuartzJob.execute(jobExecutionContext);

		verify(jobExecution).setFailureEmail("x@y.z");
		verify(jobExecution).setSuccessEmail("a@b.c");
		verify(jobExecution).setParam1("param1Value");
		verify(jobExecution).setParam2(2);
	}

	public static class TestJobExecution extends JobExecution
	{
		private String param1;
		private int param2;

		public TestJobExecution(Entity entity)
		{
			super(entity);
		}

		public void setParam1(String param1)
		{
			this.param1 = param1;
		}

		public String getParam1()
		{
			return param1;
		}

		public void setParam2(int param2)
		{
			this.param2 = param2;
		}

		public int getParam2()
		{
			return param2;
		}
	}

	@Configuration
	@Import({ UserTestConfig.class, JobTestConfig.class })
	public static class Config
	{
		@Bean
		public MolgenisQuartzJob fileIngesterQuartzJob()
		{
			return new MolgenisQuartzJob();
		}

		@Bean
		public MolgenisJobFactory fileIngestJobFactory()
		{
			return mock(MolgenisJobFactory.class);
		}

		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}
	}
}
