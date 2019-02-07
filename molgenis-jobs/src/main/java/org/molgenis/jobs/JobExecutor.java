package org.molgenis.jobs;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.jobs.model.ScheduledJobMetadata.SCHEDULED_JOB;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Service;

/** Executes {@link ScheduledJob}s. */
@Service
public class JobExecutor {
  private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>() {}.getType();
  private static final Logger LOG = LoggerFactory.getLogger(JobExecutor.class);

  private final DataService dataService;
  private final EntityManager entityManager;
  private final ExecutorService executorService;
  private final JobFactoryRegistry jobFactoryRegistry;
  private final JobExecutionContextFactory jobExecutionContextFactory;
  private final JobExecutionRegistry jobExecutionRegistry;

  private JobExecutionTemplate jobExecutionTemplate;
  private final Gson gson;

  public JobExecutor(
      DataService dataService,
      EntityManager entityManager,
      ExecutorService executorService,
      JobFactoryRegistry jobFactoryRegistry,
      JobExecutionContextFactory jobExecutionContextFactory,
      JobExecutionRegistry jobExecutionRegistry) {
    this.dataService = requireNonNull(dataService);
    this.entityManager = requireNonNull(entityManager);
    this.executorService = requireNonNull(executorService);
    this.jobFactoryRegistry = requireNonNull(jobFactoryRegistry);
    this.jobExecutionContextFactory = requireNonNull(jobExecutionContextFactory);
    this.jobExecutionRegistry = requireNonNull(jobExecutionRegistry);

    this.jobExecutionTemplate = new JobExecutionTemplate();
    this.gson = new Gson();
  }

  /**
   * Executes a {@link ScheduledJob} in the current thread.
   *
   * @param scheduledJobId ID of the {@link ScheduledJob} to run
   */
  @RunAsSystem
  public void executeScheduledJob(String scheduledJobId) {
    ScheduledJob scheduledJob =
        dataService.findOneById(SCHEDULED_JOB, scheduledJobId, ScheduledJob.class);
    if (scheduledJob == null) {
      throw new UnknownEntityException(SCHEDULED_JOB, scheduledJobId);
    }

    JobExecution jobExecution = createJobExecution(scheduledJob);
    Job molgenisJob = saveExecutionAndCreateJob(jobExecution);

    Progress progress = jobExecutionRegistry.registerJobExecution(jobExecution);
    try {
      runJob(jobExecution, molgenisJob, progress);
    } catch (Exception ex) {
      handleJobException(jobExecution, ex);
    } finally {
      jobExecutionRegistry.unregisterJobExecution(jobExecution);
    }
  }

  private JobExecution createJobExecution(ScheduledJob scheduledJob) {
    JobExecution jobExecution =
        (JobExecution) entityManager.create(scheduledJob.getType().getJobExecutionType(), POPULATE);
    writePropertyValues(jobExecution, getPropertyValues(scheduledJob.getParameters()));
    jobExecution.setFailureEmail(scheduledJob.getFailureEmail());
    jobExecution.setSuccessEmail(scheduledJob.getSuccessEmail());
    jobExecution.setUser(scheduledJob.getUser());
    jobExecution.setScheduledJobId(scheduledJob.getId());
    return jobExecution;
  }

  /**
   * Saves execution in the current thread, then creates a Job and submits that for asynchronous
   * execution.
   *
   * @param jobExecution the {@link JobExecution} to save and submit.
   */
  public CompletableFuture<Void> submit(JobExecution jobExecution) {
    return submit(jobExecution, executorService);
  }

  /**
   * Saves execution in the current thread, then creates a Job and submits that for asynchronous
   * execution to a specific ExecutorService.
   *
   * @param jobExecution the {@link JobExecution} to save and submit.
   * @param executorService the ExecutorService to run the submitted job on
   */
  public CompletableFuture<Void> submit(
      JobExecution jobExecution, ExecutorService executorService) {
    overwriteJobExecutionUser(jobExecution);
    Job molgenisJob = saveExecutionAndCreateJob(jobExecution);

    Progress progress = jobExecutionRegistry.registerJobExecution(jobExecution);
    CompletableFuture<Void> completableFuture =
        CompletableFuture.runAsync(
            () -> runJob(jobExecution, molgenisJob, progress), executorService);

    return completableFuture.handle(
        (voidResult, throwable) -> {
          if (throwable != null) {
            handleJobException(jobExecution, throwable);
          }
          jobExecutionRegistry.unregisterJobExecution(jobExecution);

          return voidResult;
        });
  }

  private void handleJobException(JobExecution jobExecution, Throwable throwable) {
    if (LOG.isErrorEnabled()) {
      LOG.error(
          format(
              "Job of type '%s' with id '%s' completed with exception",
              jobExecution.getType(), jobExecution.getIdentifier()),
          throwable);
    }
  }

  public void cancel(JobExecution jobExecution) {
    Progress progress = jobExecutionRegistry.getJobExecutionProgress(jobExecution);
    progress.canceling();
  }

  private void overwriteJobExecutionUser(JobExecution jobExecution) {
    String username;
    if (SecurityUtils.currentUserIsSystem()) {
      username = null;
    } else {
      username = SecurityUtils.getCurrentUsername();
    }
    jobExecution.setUser(username);
  }

  @SuppressWarnings("unchecked")
  private Job saveExecutionAndCreateJob(JobExecution jobExecution) {
    String entityTypeId = jobExecution.getEntityType().getId();
    dataService.add(entityTypeId, jobExecution);
    try {
      JobFactory jobFactory = jobFactoryRegistry.getJobFactory(jobExecution);
      return jobFactory.createJob(jobExecution);
    } catch (RuntimeException ex) {
      LOG.error("Error creating job for JobExecution.", ex);
      jobExecution.setStatus(JobExecution.Status.FAILED);
      dataService.update(entityTypeId, jobExecution);
      throw ex;
    }
  }

  private void runJob(JobExecution jobExecution, Job<?> job, Progress progress) {
    JobExecutionContext jobExecutionContext =
        jobExecutionContextFactory.createJobExecutionContext(jobExecution);
    jobExecutionTemplate.call(job, progress, jobExecutionContext);
  }

  private void writePropertyValues(JobExecution jobExecution, MutablePropertyValues pvs) {
    BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(jobExecution);
    bw.setPropertyValues(pvs, true);
  }

  private MutablePropertyValues getPropertyValues(String parameterJson) {
    Map<String, Object> parameters = gson.fromJson(parameterJson, MAP_TOKEN);
    MutablePropertyValues pvs = new MutablePropertyValues();
    pvs.addPropertyValues(parameters);
    return pvs;
  }

  /** testability */
  void setJobExecutionTemplate(JobExecutionTemplate jobExecutionTemplate) {
    this.jobExecutionTemplate = requireNonNull(jobExecutionTemplate);
  }
}
