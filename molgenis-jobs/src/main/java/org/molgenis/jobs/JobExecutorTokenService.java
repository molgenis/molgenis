package org.molgenis.jobs;

import org.molgenis.jobs.model.JobExecution;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Creates {@link org.springframework.security.core.Authentication} for jobs based on their job
 * execution.
 */
public interface JobExecutorTokenService {
  AbstractAuthenticationToken createToken(JobExecution jobExecution);
}
