package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;

interface JobExecutionRegistry {
  Progress registerJobExecution(JobExecution jobExecution);

  Progress getJobExecutionProgress(JobExecution jobExecution);

  void unregisterJobExecution(JobExecution jobExecution);
}
