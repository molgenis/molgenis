package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.jobs.model.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JobExecutionUpdaterImpl implements JobExecutionUpdater {
  private static final Logger LOG = LoggerFactory.getLogger(JobExecutionUpdater.class);

  private final JobExecutionContextFactory jobExecutionContextFactory;
  private final ExecutorService executorService;
  @Autowired private DataService dataService;

  JobExecutionUpdaterImpl(JobExecutionContextFactory jobExecutionContextFactory) {
    this.jobExecutionContextFactory = requireNonNull(jobExecutionContextFactory);
    this.executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public void update(JobExecution jobExecution) {
    JobExecutionContext jobExecutionContext =
        jobExecutionContextFactory.createJobExecutionContext(jobExecution);
    executorService.execute(() -> updateInternal(jobExecution, jobExecutionContext));
  }

  private void updateInternal(JobExecution jobExecution, JobExecutionContext jobExecutionContext) {
    SecurityContext originalContext = SecurityContextHolder.getContext();
    try {
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(jobExecutionContext.getAuthentication());
      SecurityContextHolder.setContext(securityContext);

      tryUpdate(jobExecution);
    } finally {
      SecurityContextHolder.setContext(originalContext);
    }
  }

  private void tryUpdate(JobExecution jobExecution) {
    Entity jobExecutionCopy = new DynamicEntity(jobExecution.getEntityType());
    jobExecutionCopy.set(jobExecution);

    try {
      dataService.update(jobExecutionCopy.getEntityType().getId(), jobExecutionCopy);
    } catch (Exception ex) {
      LOG.warn("Error updating job execution", ex);
    }
  }
}
