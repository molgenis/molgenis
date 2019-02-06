package org.molgenis.jobs;

import static java.lang.String.format;

import org.molgenis.jobs.model.JobExecution;

public class InactiveJobExecutionException extends RuntimeException {
  public InactiveJobExecutionException(JobExecution jobExecution) {
    super(format("'%s' '%s' is executing", jobExecution.getType(), jobExecution.getIdentifier()));
  }
}
