package org.molgenis.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;
import static org.testng.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.mockito.ArgumentCaptor;
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
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.MailSender;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JobExecutorTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private EntityManager entityManager;
  @Mock private JobExecutionUpdater jobExecutionUpdater;
  @Mock private MailSender mailSender;
  @Mock private ExecutorService executorService;
  @Mock private JobFactoryRegistry jobFactoryRegistry;
  @Mock private JobExecutionContextFactory jobExecutionContextFactory;
  @Mock private JobExecutionTemplate jobExecutionTemplate;
  private JobExecutor jobExecutor;

  @BeforeMethod
  public void setUpBeforeMethod() {
    jobExecutor =
        new JobExecutor(
            dataService,
            entityManager,
            jobExecutionUpdater,
            mailSender,
            executorService,
            jobFactoryRegistry,
            jobExecutionContextFactory);
    jobExecutor.setJobExecutionTemplate(jobExecutionTemplate);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testJobExecutor() {
    new JobExecutor(null, null, null, null, null, null, null);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testExecuteScheduledJob() {
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

    jobExecutor.executeScheduledJob(scheduledJobId);

    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    ArgumentCaptor<Progress> progressCaptor = ArgumentCaptor.forClass(Progress.class);
    verify(jobExecutionTemplate).call(eq(job), progressCaptor.capture(), eq(jobExecutionContext));
    assertEquals(progressCaptor.getValue().getJobExecution(), jobExecution);
  }

  @SuppressWarnings("unchecked")
  @Test(expectedExceptions = RuntimeException.class)
  public void testExecuteScheduledJobRunJobFails() {
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

    doThrow(new RuntimeException())
        .when(jobExecutionTemplate).call(eq(job), any(Progress.class), eq(jobExecutionContext));
    try {
      jobExecutor.executeScheduledJob(scheduledJobId);
    } finally {
      verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
      verify(jobExecution).setStatus(Status.FAILED);
      verify(dataService).update(jobExecutionEntityTypeId, jobExecution);
    }
  }

  @Test(expectedExceptions = UnknownEntityException.class)
  public void testExecuteScheduledJobUnknownJob() {
    String scheduledJobId = "UnknownScheduledJobId";
    jobExecutor.executeScheduledJob(scheduledJobId);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSubmit() throws ExecutionException, InterruptedException {
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
    when(jobExecutionContextFactory.createJobExecutionContext(jobExecution))
        .thenReturn(jobExecutionContext);

    doAnswer(
            (InvocationOnMock invocation) -> {
              ((Runnable) invocation.getArguments()[0]).run();
              return null;
            })
        .when(executorService)
        .execute(any(Runnable.class));
    jobExecutor.submit(jobExecution).get();

    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    ArgumentCaptor<Progress> progressCaptor = ArgumentCaptor.forClass(Progress.class);
    verify(jobExecutionTemplate).call(eq(job), progressCaptor.capture(), eq(jobExecutionContext));
    assertEquals(progressCaptor.getValue().getJobExecution(), jobExecution);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testSubmitJobCreationFails() {
    String jobExecutionEntityTypeId = "MyJobExecutionId";
    EntityType jobExecutionEntityType = mock(EntityType.class);
    when(jobExecutionEntityType.getId()).thenReturn(jobExecutionEntityTypeId);

    JobExecution jobExecution = mock(JobExecution.class);
    when(jobExecution.getEntityType()).thenReturn(jobExecutionEntityType);

    doThrow(new RuntimeException()).when(jobFactoryRegistry).getJobFactory(jobExecution);

    try {
      jobExecutor.submit(jobExecution);
    } finally {
      verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
      verify(jobExecution).setStatus(Status.FAILED);
      verify(dataService).update(jobExecutionEntityTypeId, jobExecution);
    }
  }

  // same as testSubmit but with other ExecutorService
  @SuppressWarnings("unchecked")
  @Test
  public void testSubmitExecutorService() throws ExecutionException, InterruptedException {
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
    when(jobExecutionContextFactory.createJobExecutionContext(jobExecution))
        .thenReturn(jobExecutionContext);

    doAnswer(
            (InvocationOnMock invocation) -> {
              ((Runnable) invocation.getArguments()[0]).run();
              return null;
            })
        .when(myExecutorService)
        .execute(any(Runnable.class));
    jobExecutor.submit(jobExecution, myExecutorService).get();

    verify(dataService).add(jobExecutionEntityTypeId, jobExecution);
    ArgumentCaptor<Progress> progressCaptor = ArgumentCaptor.forClass(Progress.class);
    verify(jobExecutionTemplate).call(eq(job), progressCaptor.capture(), eq(jobExecutionContext));
    assertEquals(progressCaptor.getValue().getJobExecution(), jobExecution);
  }
}
