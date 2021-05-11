package org.molgenis.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = JobExecutorTest.Config.class)
class JobExecutorTest extends AbstractMockitoSpringContextTests {

  @Configuration
  static class Config {}

  @Mock private DataService dataService;
  @Mock private EntityManager entityManager;
  @Mock private ExecutorService executorService;
  @Mock private JobFactoryRegistry jobFactoryRegistry;
  @Mock private JobExecutionContextFactory jobExecutionContextFactory;
  @Mock private JobExecutionTemplate jobExecutionTemplate;
  @Mock private JobExecutionRegistry jobExecutionRegistry;
  private JobExecutor jobExecutor;
  private SecurityContext previousContext;

  @BeforeEach
  void setUpBeforeMethod() {
    jobExecutor =
        new JobExecutor(
            dataService,
            entityManager,
            executorService,
            jobFactoryRegistry,
            jobExecutionContextFactory,
            jobExecutionRegistry);
    jobExecutor.setJobExecutionTemplate(jobExecutionTemplate);

    previousContext = SecurityContextHolder.getContext();
  }

  @AfterEach
  void tearDownAfterEach() {
    SecurityContextHolder.setContext(previousContext);
  }

  @Test
  void testJobExecutor() {
    assertThrows(
        NullPointerException.class, () -> new JobExecutor(null, null, null, null, null, null));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testExecuteScheduledJob() {
    String scheduledJobId = "MyScheduledJobId";

    EntityType jobExecutionType = mock(EntityType.class);
    ScheduledJobType scheduledJobType = mock(ScheduledJobType.class);
    when(scheduledJobType.getJobExecutionType()).thenReturn(jobExecutionType);

    ScheduledJob scheduledJob = mock(ScheduledJob.class);
    when(scheduledJob.getType()).thenReturn(scheduledJobType);
    when(dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class))
        .thenReturn(scheduledJob);

    String jobExecutionEntityTypeId = "MyJobExecutionId";
    EntityType jobExecutionEntityType = mock(EntityType.class);
    when(jobExecutionEntityType.getId()).thenReturn(jobExecutionEntityTypeId);

    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(jobExecutionEntityType);
    when(entityManager.create(jobExecutionType, POPULATE)).thenReturn(jobExecution);

    Job job = mock(Job.class);

    JobFactory jobFactory = mock(JobFactory.class);
    when(jobFactoryRegistry.getJobFactory(jobExecution)).thenReturn(jobFactory);
    when(jobFactory.createJob(jobExecution)).thenReturn(job);

    JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
    when(jobExecutionContextFactory.createJobExecutionContext(jobExecution))
        .thenReturn(jobExecutionContext);

    Progress progress = mock(Progress.class);
    when(jobExecutionRegistry.registerJobExecution(jobExecution)).thenReturn(progress);

    jobExecutor.executeScheduledJob(scheduledJobId);

    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    verify(jobExecutionTemplate).call(job, progress, jobExecutionContext);
    verify(jobExecutionRegistry).unregisterJobExecution(jobExecution);
  }

  @Test
  void testExecuteScheduledJobUnknownJob() {
    String scheduledJobId = "UnknownScheduledJobId";
    assertThrows(
        UnknownEntityException.class, () -> jobExecutor.executeScheduledJob(scheduledJobId));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser
  void testSubmit() throws ExecutionException, InterruptedException {
    String jobExecutionEntityTypeId = "MyJobExecutionId";
    EntityType jobExecutionEntityType = mock(EntityType.class);
    when(jobExecutionEntityType.getId()).thenReturn(jobExecutionEntityTypeId);

    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(jobExecutionEntityType);

    Job job = mock(Job.class);

    JobFactory jobFactory = mock(JobFactory.class);
    when(jobFactoryRegistry.getJobFactory(jobExecution)).thenReturn(jobFactory);
    when(jobFactory.createJob(jobExecution)).thenReturn(job);

    JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    when(jobExecutionContextFactory.createJobExecutionContextWithAuthentication(
            jobExecution, authentication))
        .thenReturn(jobExecutionContext);

    Progress progress = mock(Progress.class);
    when(jobExecutionRegistry.registerJobExecution(jobExecution)).thenReturn(progress);
    doAnswer(
            (InvocationOnMock invocation) -> {
              assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
              ((Runnable) invocation.getArguments()[0]).run();
              return null;
            })
        .when(executorService)
        .execute(any(Runnable.class));
    jobExecutor.submit(jobExecution).get();

    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    verify(jobExecutionTemplate).call(job, progress, jobExecutionContext);
    verify(jobExecutionRegistry).unregisterJobExecution(jobExecution);
  }

  @Test
  void testSubmitJobCreationFails() {
    String jobExecutionEntityTypeId = "MyJobExecutionId";
    EntityType jobExecutionEntityType = mock(EntityType.class);
    when(jobExecutionEntityType.getId()).thenReturn(jobExecutionEntityTypeId);

    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(jobExecutionEntityType);

    doThrow(new RuntimeException()).when(jobFactoryRegistry).getJobFactory(jobExecution);

    assertThrows(RuntimeException.class, () -> jobExecutor.submit(jobExecution));
    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    verify(jobExecution).setStatus(Status.FAILED);
    verify(dataService).update(jobExecutionEntityTypeId, jobExecution);
  }

  // same as testSubmit but with other ExecutorService
  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser
  void testSubmitExecutorService() throws ExecutionException, InterruptedException {
    ExecutorService myExecutorService = mock(ExecutorService.class);

    String jobExecutionEntityTypeId = "MyJobExecutionId";
    EntityType jobExecutionEntityType = mock(EntityType.class);
    when(jobExecutionEntityType.getId()).thenReturn(jobExecutionEntityTypeId);

    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(jobExecutionEntityType);

    Job job = mock(Job.class);

    JobFactory jobFactory = mock(JobFactory.class);
    when(jobFactoryRegistry.getJobFactory(jobExecution)).thenReturn(jobFactory);
    when(jobFactory.createJob(jobExecution)).thenReturn(job);

    JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    when(jobExecutionContextFactory.createJobExecutionContextWithAuthentication(
            jobExecution, authentication))
        .thenReturn(jobExecutionContext);

    Progress progress = mock(Progress.class);
    when(jobExecutionRegistry.registerJobExecution(jobExecution)).thenReturn(progress);
    doAnswer(
            (InvocationOnMock invocation) -> {
              assertEquals(authentication, SecurityContextHolder.getContext().getAuthentication());
              ((Runnable) invocation.getArguments()[0]).run();
              return null;
            })
        .when(myExecutorService)
        .execute(any(Runnable.class));
    jobExecutor.submit(jobExecution, myExecutorService).get();

    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    verify(jobExecutionTemplate).call(job, progress, jobExecutionContext);
    verify(jobExecutionRegistry).unregisterJobExecution(jobExecution);
  }

  @Test
  void testCancel() {
    JobExecution jobExecution = mock(JobExecution.class);
    Progress progress = mock(Progress.class);
    when(jobExecutionRegistry.getJobExecutionProgress(jobExecution)).thenReturn(progress);
    jobExecutor.cancel(jobExecution);
    verify(progress).canceling();
  }
}
