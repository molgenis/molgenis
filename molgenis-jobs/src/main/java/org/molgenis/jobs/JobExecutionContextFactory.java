package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;

public interface JobExecutionContextFactory {

  JobExecutionContext createJobExecutionContext(JobExecution jobExecution);
}
