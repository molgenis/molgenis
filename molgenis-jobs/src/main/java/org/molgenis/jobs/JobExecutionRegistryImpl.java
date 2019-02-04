package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.stereotype.Component;

@Component
class JobExecutionRegistryImpl implements JobExecutionRegistry {
  private final ProgressFactory progressFactory;
  private final Map<String, Progress> jobExecutionProgressMap;

  JobExecutionRegistryImpl(ProgressFactory progressFactory) {
    this.progressFactory = requireNonNull(progressFactory);
    jobExecutionProgressMap = new ConcurrentHashMap<>();
  }

  @Override
  public Progress registerJobExecution(JobExecution jobExecution) {
    String jobExecutionId = getJobExecutionId(jobExecution);

    Progress progress = progressFactory.create(jobExecution);
    Progress existingProgress = jobExecutionProgressMap.put(jobExecutionId, progress);
    if (existingProgress != null) {
      throw new IllegalArgumentException("Job execution already registered");
    }
    return progress;
  }

  @Override
  public Progress getJobExecutionProgress(JobExecution jobExecution) {
    String jobExecutionId = getJobExecutionId(jobExecution);
    Progress progress = jobExecutionProgressMap.get(jobExecutionId);
    if (progress == null) {
      throw new InactiveJobExecutionException(jobExecution);
    }
    return progress;
  }

  @Override
  public void unregisterJobExecution(JobExecution jobExecution) {
    String jobExecutionId = getJobExecutionId(jobExecution);
    Progress removedProgress = jobExecutionProgressMap.remove(jobExecutionId);
    if (removedProgress == null) {
      throw new InactiveJobExecutionException(jobExecution);
    }
  }

  private static String getJobExecutionId(JobExecution jobExecution) {
    return jobExecution.getType() + '-' + jobExecution.getIdentifier();
  }
}
