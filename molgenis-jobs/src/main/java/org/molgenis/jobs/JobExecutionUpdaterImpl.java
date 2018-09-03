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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JobExecutionUpdaterImpl implements JobExecutionUpdater {
  private static final Logger LOG = LoggerFactory.getLogger(JobExecutionUpdater.class);

  private final JobExecutorTokenService jobExecutorTokenService;
  private final ExecutorService executorService;
  @Autowired private DataService dataService;

  JobExecutionUpdaterImpl(JobExecutorTokenService jobExecutorTokenService) {
    this.jobExecutorTokenService = requireNonNull(jobExecutorTokenService);
    this.executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public void update(JobExecution jobExecution) {
    Authentication authentication = jobExecutorTokenService.createToken(jobExecution);
    executorService.execute(() -> updateInternal(authentication, jobExecution));
  }

  private void updateInternal(Authentication authentication, JobExecution jobExecution) {
    SecurityContext originalContext = SecurityContextHolder.getContext();
    try {
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(authentication);
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
