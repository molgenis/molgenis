package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;
import org.springframework.security.core.Authentication;

public interface JobExecutionContextFactory {

  /**
   * Creates a JobExecutionContext with an {@link Authentication} based on the {@link JobExecution}.
   *
   * @param jobExecution the JobExecution to create a context for
   * @return a JobExecutionContext
   */
  JobExecutionContext createJobExecutionContext(JobExecution jobExecution);

  /**
   * Creates a JobExecutionContext with the specified {@link Authentication}.
   *
   * @param jobExecution the JobExecution to create a context for
   * @param authentication the Authentication to add to the JobExecutionContext
   * @return a JobExecutionContext
   */
  JobExecutionContext createJobExecutionContextWithAuthentication(
      JobExecution jobExecution, Authentication authentication);
}
