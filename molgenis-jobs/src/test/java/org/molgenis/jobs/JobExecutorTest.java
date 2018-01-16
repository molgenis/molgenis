package org.molgenis.jobs;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.config.JobTestConfig;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobType;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.jobs.model.JobExecution.Status.FAILED;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

@ContextConfiguration(classes = { JobExecutorTest.Config.class, JobExecutor.class, JobTestConfig.class })
public class JobExecutorTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private Config config;

	@Autowired
	private DataService dataService;

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private JobFactory jobFactory;

	@Autowired
	private ScheduledJobType scheduledJobType;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JobFactoryRegistry jobFactoryRegistry;

	@Autowired
	private ExecutorService executorService;

	@Mock
	private ScheduledJob scheduledJob;

	@Mock
	private Job<Void> job;

	@Mock
	private JobExecutionContext jobExecutionContext;

	@Autowired
	private EntityManager entityManager;

	@Mock
	private EntityType jobExecutionType;

	@Mock
	private TestJobExecution jobExecution;

	@Mock
	private UserDetails userDetails;

	@Mock
	private GrantedAuthority grantedAuthority1;

	@Mock
	private GrantedAuthority grantedAuthority2;

	@Captor
	private ArgumentCaptor<Runnable> jobCaptor;

	public JobExecutorTest()
	{
		super(Strictness.WARN);
	}

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);

	}

	@BeforeMethod
	public void beforeMethod()
	{
		config.resetMocks();
		reset(jobExecutionContext);
		when(scheduledJobType.getJobExecutionType()).thenReturn(jobExecutionType);
		when(scheduledJobType.getName()).thenReturn("jobName");
		when(jobExecution.getStartDate()).thenReturn(Instant.now());
		when(jobExecution.getSuccessEmail()).thenReturn(new String[] {});
		when(jobExecution.getFailureEmail()).thenReturn(new String[] {});
		when(jobFactoryRegistry.getJobFactory(jobExecution)).thenReturn(jobFactory);
	}

	@Test
	public void executeScheduledJob() throws Exception
	{
		when(dataService.findOneById(SCHEDULED_JOB, "aaaacw67ejuwq7wron3yjriaae", ScheduledJob.class)).thenReturn(
				scheduledJob);
		when(entityManager.create(jobExecutionType, EntityManager.CreationMode.POPULATE)).thenReturn(jobExecution);

		when(jobFactory.createJob(jobExecution)).thenReturn(job);
		when(scheduledJob.getParameters()).thenReturn("{param1:'param1Value', param2:2}");
		when(scheduledJob.getFailureEmail()).thenReturn("x@y.z");
		when(scheduledJob.getSuccessEmail()).thenReturn("a@b.c");
		when(scheduledJob.getUser()).thenReturn("fjant");
		when(scheduledJob.getType()).thenReturn(scheduledJobType);

		when(userDetailsService.loadUserByUsername("fjant")).thenReturn(userDetails);

		Collection<? extends GrantedAuthority> authorities = Arrays.asList(grantedAuthority1, grantedAuthority2);
		when(userDetails.getAuthorities()).thenAnswer(i -> authorities);

		when(jobExecution.getEntityType()).thenReturn(jobExecutionType);
		when(jobExecutionType.getId()).thenReturn("sys_FileIngestJobExecution");
		when(jobExecution.getUser()).thenReturn("fjant");

		jobExecutor.executeScheduledJob("aaaacw67ejuwq7wron3yjriaae");

		verify(jobExecution).setFailureEmail("x@y.z");
		verify(jobExecution).setSuccessEmail("a@b.c");
		verify(jobExecution).setParam1("param1Value");
		verify(jobExecution).setParam2(2);

		verify(dataService).add("sys_FileIngestJobExecution", jobExecution);

		verify(job).call(any(Progress.class));
	}

	@Test
	public void submitJobExecution() throws Exception
	{
		when(jobExecution.getEntityType()).thenReturn(jobExecutionType);
		when(jobExecutionType.getId()).thenReturn("sys_FileIngestJobExecution");
		when(jobExecution.getUser()).thenReturn("fjant");

		when(jobFactory.createJob(jobExecution)).thenReturn(job);
		when(userDetailsService.loadUserByUsername("fjant")).thenReturn(userDetails);

		Collection<? extends GrantedAuthority> authorities = Arrays.asList(grantedAuthority1, grantedAuthority2);
		when(userDetails.getAuthorities()).thenAnswer(i -> authorities);

		jobExecutor.submit(jobExecution);

		verify(dataService).add("sys_FileIngestJobExecution", jobExecution);
		verify(executorService).execute(jobCaptor.capture());

		jobCaptor.getValue().run();
		verify(job).call(any(Progress.class));
	}

	@Test
	public void submitJobExecutionJobFactoryThrowsException() throws Exception
	{
		when(jobExecution.getEntityType()).thenReturn(jobExecutionType);
		when(jobExecutionType.getId()).thenReturn("sys_FileIngestJobExecution");
		when(jobExecution.getUser()).thenReturn("fjant");

		when(jobFactory.createJob(jobExecution)).thenThrow(new NullPointerException());

		try
		{
			jobExecutor.submit(jobExecution);
		}
		catch (NullPointerException expected)
		{
		}

		verify(dataService).add("sys_FileIngestJobExecution", jobExecution);
		verify(jobExecution).setStatus(FAILED);
		verify(dataService).update("sys_FileIngestJobExecution", jobExecution);
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
	@Import({ JobTestConfig.class })
	public static class Config
	{
		public Config()
		{
			initMocks(this);
		}

		@Mock
		JobFactory jobFactory;

		@Mock
		ScheduledJobType scheduledJobType;

		@Mock
		JobFactoryRegistry jobFactoryRegistry;

		@Mock
		private ExecutorService executorService;

		public void resetMocks()
		{
			reset(jobFactory, scheduledJobType, executorService);
		}

		@Bean
		public JobFactoryRegistry jobFactoryRegistry()
		{
			return jobFactoryRegistry;
		}

		@Bean
		public JobFactory jobFactory()
		{
			return jobFactory;
		}

		@Bean
		ScheduledJobType jobType()
		{
			return scheduledJobType;
		}

		@Bean
		ExecutorService executorService()
		{
			return executorService;
		}

		@Bean
		public MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		public UserDetailsService userDetailsService()
		{
			return mock(UserDetailsService.class);
		}

		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}
	}
}
