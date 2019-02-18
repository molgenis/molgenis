package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class JobsServiceImpl implements JobsService {
  private final DataService dataService;
  private final JobExecutor jobExecutor;

  JobsServiceImpl(DataService dataService, JobExecutor jobExecutor) {
    this.dataService = requireNonNull(dataService);
    this.jobExecutor = requireNonNull(jobExecutor);
  }

  @Transactional
  @Override
  public void cancel(String jobExecutionType, String jobExecutionId) {
    EntityType jobExecutionMetadata = dataService.getEntityType(jobExecutionType);
    if (!isJobExecutionMetadata(jobExecutionMetadata)) {
      throw new InvalidJobExecutionTypeException(jobExecutionType);
    }

    JobExecution jobExecution =
        (JobExecution) dataService.findOneById(jobExecutionType, jobExecutionId);
    if (jobExecution == null) {
      throw new UnknownEntityException(jobExecutionMetadata, jobExecutionId);
    }

    jobExecutor.cancel(jobExecution);
  }

  private boolean isJobExecutionMetadata(EntityType jobExecutionMetadata) {
    EntityType parentEntityType = jobExecutionMetadata.getExtends();
    if (parentEntityType == null) {
      return false;
    } else {
      if (JobExecutionMetaData.JOB_EXECUTION.equals(parentEntityType.getId())) {
        return true;
      } else {
        return isJobExecutionMetadata(parentEntityType);
      }
    }
  }
}
