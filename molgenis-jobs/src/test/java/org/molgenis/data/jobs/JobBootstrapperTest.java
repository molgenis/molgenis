package org.molgenis.data.jobs;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.jobs.model.JobExecution.Status.RUNNING;
import static org.molgenis.data.jobs.model.JobExecutionMetaData.*;

@ContextConfiguration(classes = { JobBootstrapperTest.Config.class, JobBootstrapper.class })
public class JobBootstrapperTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private JobBootstrapper jobBootstrapper;

	@Autowired
	private DataService dataService;

	@Autowired
	private SystemEntityTypeRegistry systemEntityTypeRegistry;

	@Mock
	private SystemEntityType jobType1;

	@Mock
	private JobExecution job1;

	@Mock
	private JobExecution job2;

	@Mock
	private EntityType jobExecutionType;

	@Mock
	private Query<Entity> query;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
	}

	@Test
	public void testBootstrap()
	{
		when(systemEntityTypeRegistry.getSystemEntityTypes()).thenReturn(Stream.of(jobType1));
		when(jobType1.getExtends()).thenReturn(jobExecutionType);
		when(jobExecutionType.getName()).thenReturn(JOB_EXECUTION);
		when(jobType1.getName()).thenReturn("JobType1");

		when(dataService.query("JobType1")).thenReturn(query);
		when(query.eq(STATUS, RUNNING)).thenReturn(query);
		when(query.eq(STATUS, PENDING)).thenReturn(query);
		when(query.or()).thenReturn(query);

		when(query.findAll()).thenReturn(Stream.of(job1, job2));

		when(job1.get(LOG)).thenReturn("Current log");
		when(job1.getEntityType()).thenReturn(jobType1);

		when(job2.get(LOG)).thenReturn(null);
		when(job2.getEntityType()).thenReturn(jobType1);

		jobBootstrapper.bootstrap();

		verify(job1).set(STATUS, FAILED);
		verify(job1).set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
		verify(job1).set(LOG, "Current log\nFAILED - Application terminated unexpectedly");

		verify(job2).set(STATUS, FAILED);
		verify(job2).set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
		verify(job2).set(LOG, "FAILED - Application terminated unexpectedly");

		verify(dataService).update("JobType1", job1);
		verify(dataService).update("JobType1", job2);

	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public SystemEntityTypeRegistry systemEntityTypeRegistry()
		{
			return mock(SystemEntityTypeRegistry.class);
		}
	}
}
