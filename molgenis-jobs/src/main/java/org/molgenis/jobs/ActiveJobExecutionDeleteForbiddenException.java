package org.molgenis.jobs;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.JobExecution.Status;

public class ActiveJobExecutionDeleteForbiddenException extends CodedRuntimeException {
  private static final String ERROR_CODE = "JOB01";

  private final String jobExecutionId;
  private final Status jobExecutionStatus;

  public ActiveJobExecutionDeleteForbiddenException(JobExecution jobExecution) {
    super(ERROR_CODE);
    requireNonNull(jobExecution);
    this.jobExecutionId = jobExecution.getIdentifier();
    this.jobExecutionStatus = jobExecution.getStatus();
  }

  @Override
  public String getMessage() {
    return format("id:%s status:%s", jobExecutionId, jobExecutionStatus);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
