package org.molgenis.jobs.model;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.jobs.ActiveJobExecutionDeleteForbiddenException;
import org.molgenis.jobs.model.JobExecution.Status;
import org.molgenis.util.UnexpectedEnumException;

class JobExecutionRepositoryDecorator extends AbstractRepositoryDecorator<JobExecution> {
  private static final int BATCH_SIZE = 1000;

  JobExecutionRepositoryDecorator(Repository<JobExecution> delegateRepository) {
    super(delegateRepository);
  }

  @Override
  public void delete(JobExecution jobExecution) {
    validateDeleteAllowed(jobExecution);
    super.delete(jobExecution);
  }

  @Override
  public void deleteById(Object id) {
    validateDeleteAllowedById(id);
    super.deleteById(id);
  }

  @Override
  public void deleteAll() {
    forEachBatched(
        jobExecutionBatch -> jobExecutionBatch.forEach(this::validateDeleteAllowed), BATCH_SIZE);
    super.deleteAll();
  }

  @Override
  public void delete(Stream<JobExecution> jobExecutionStream) {
    super.delete(jobExecutionStream.filter(this::validateDeleteAllowed));
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    super.deleteAll(ids.filter(this::validateDeleteAllowedById));
  }

  private boolean validateDeleteAllowedById(Object jobExecutionId) {
    JobExecution jobExecution = findOneById(jobExecutionId);
    if (jobExecution == null) {
      throw new UnknownEntityException(getEntityType(), jobExecutionId);
    }
    return validateDeleteAllowed(jobExecution);
  }

  private boolean validateDeleteAllowed(JobExecution jobExecution) {
    if (isActiveJobExecution(jobExecution)) {
      throw new ActiveJobExecutionDeleteForbiddenException(jobExecution);
    }
    return true;
  }

  private boolean isActiveJobExecution(JobExecution jobExecution) {
    boolean isActive;

    Status status = jobExecution.getStatus();
    switch (status) {
      case PENDING:
      case RUNNING:
        isActive = true;
        break;
      case SUCCESS:
      case FAILED:
      case CANCELED:
        isActive = false;
        break;
      default:
        throw new UnexpectedEnumException(status);
    }

    return isActive;
  }
}
