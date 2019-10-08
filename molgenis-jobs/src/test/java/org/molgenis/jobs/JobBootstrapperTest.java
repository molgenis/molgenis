package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.jobs.model.JobExecution.Status.RUNNING;
import static org.molgenis.jobs.model.JobExecutionMetaData.CANCELING;
import static org.molgenis.jobs.model.JobExecutionMetaData.FAILED;
import static org.molgenis.jobs.model.JobExecutionMetaData.JOB_EXECUTION;
import static org.molgenis.jobs.model.JobExecutionMetaData.LOG;
import static org.molgenis.jobs.model.JobExecutionMetaData.PENDING;
import static org.molgenis.jobs.model.JobExecutionMetaData.PROGRESS_MESSAGE;
import static org.molgenis.jobs.model.JobExecutionMetaData.STATUS;
import static org.molgenis.jobs.model.ScheduledJobTypeMetadata.SCHEDULED_JOB_TYPE;

import java.util.Collections;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.schedule.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration(classes = {JobBootstrapperTest.Config.class, JobBootstrapper.class})
class JobBootstrapperTest extends AbstractMolgenisSpringTest {
  @Autowired private JobBootstrapper jobBootstrapper;

  @Autowired private DataService dataService;

  @Autowired private SystemEntityTypeRegistry systemEntityTypeRegistry;

  @Autowired private ScheduledJobType scheduledJobType;

  @Mock private Repository<ScheduledJobType> jobTypeRepo;

  @Mock private SystemEntityType fileIngestJobExecutionType;

  @Mock private JobExecution fileIngestJob1;

  @Mock private JobExecution fileIngestJob2;

  @Mock private JobExecution fileIngestJob3;

  @Mock private EntityType jobExecutionType;

  @Mock private Query<Entity> query;

  @Captor private ArgumentCaptor<String> stringCaptor;

  @Autowired private JobScheduler jobScheduler;

  @Autowired private Config config;

  @BeforeEach
  void beforeMethod() {
    config.resetMocks();
  }

  @Test
  void testBootstrap() {
    when(systemEntityTypeRegistry.getSystemEntityTypes())
        .thenReturn(Stream.of(fileIngestJobExecutionType));
    when(fileIngestJobExecutionType.getExtends()).thenReturn(jobExecutionType);
    when(jobExecutionType.getId()).thenReturn(JOB_EXECUTION);
    when(fileIngestJobExecutionType.getId()).thenReturn("sys_FileIngestJobExecution");

    when(dataService.query("sys_FileIngestJobExecution")).thenReturn(query);
    when(query.eq(STATUS, RUNNING)).thenReturn(query);
    when(query.eq(STATUS, PENDING)).thenReturn(query);
    when(query.eq(STATUS, CANCELING)).thenReturn(query);
    when(query.or()).thenReturn(query);

    when(query.findAll()).thenReturn(Stream.of(fileIngestJob1, fileIngestJob2, fileIngestJob3));

    when(dataService.getRepository(SCHEDULED_JOB_TYPE, ScheduledJobType.class))
        .thenReturn(jobTypeRepo);

    when(fileIngestJob1.get(LOG)).thenReturn("Current log");
    when(fileIngestJob1.getEntityType()).thenReturn(fileIngestJobExecutionType);

    when(fileIngestJob2.get(LOG)).thenReturn(null);
    when(fileIngestJob2.getEntityType()).thenReturn(fileIngestJobExecutionType);

    String hugeLog = RandomStringUtils.random(JobExecution.MAX_LOG_LENGTH - 10);
    when(fileIngestJob3.get(LOG)).thenReturn(hugeLog);
    when(fileIngestJob3.getEntityType()).thenReturn(fileIngestJobExecutionType);

    jobBootstrapper.bootstrap();

    verify(fileIngestJob1).set(STATUS, FAILED);
    verify(fileIngestJob1).set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
    verify(fileIngestJob1).set(LOG, "Current log\nFAILED - Application terminated unexpectedly");

    verify(fileIngestJob2).set(STATUS, FAILED);
    verify(fileIngestJob2).set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
    verify(fileIngestJob2).set(LOG, "FAILED - Application terminated unexpectedly");

    verify(fileIngestJob3).set(STATUS, FAILED);
    verify(fileIngestJob3).set(PROGRESS_MESSAGE, "Application terminated unexpectedly");
    verify(fileIngestJob3).set(eq(LOG), stringCaptor.capture());

    String log3 = stringCaptor.getValue();
    assertEquals(
        log3.length(),
        JobExecution.MAX_LOG_LENGTH,
        "Updated log length mustn't exceed MAX_LOG_LENGTH");
    assertTrue(
        log3.contains(JobExecution.TRUNCATION_BANNER),
        "Updated log should contain truncation banner");
    assertTrue(log3.endsWith("\nFAILED - Application terminated unexpectedly"));

    verify(dataService).update("sys_FileIngestJobExecution", fileIngestJob1);
    verify(dataService).update("sys_FileIngestJobExecution", fileIngestJob2);
    verify(jobScheduler).scheduleJobs();
    verify(jobTypeRepo).upsertBatch(Collections.singletonList(scheduledJobType));
  }

  @Configuration
  static class Config {
    @Mock private JobFactory fileIngestJobFactory;

    @Mock private ScheduledJobType fileIngestScheduledJobType;

    @Mock private JobScheduler jobScheduler;

    private void resetMocks() {
      reset(jobScheduler, fileIngestScheduledJobType, fileIngestJobFactory);
    }

    Config() {
      org.mockito.MockitoAnnotations.initMocks(this);
    }

    @Bean
    JobFactory fileIngestJobFactory() {
      return fileIngestJobFactory;
    }

    @Bean
    ScheduledJobType jobType() {
      return fileIngestScheduledJobType;
    }

    @Bean
    JobScheduler jobScheduler() {
      return jobScheduler;
    }

    @Bean
    SystemEntityTypeRegistry systemEntityTypeRegistry() {
      return mock(SystemEntityTypeRegistry.class);
    }
  }
}
