package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import org.molgenis.jobs.model.JobExecution;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
class JobExecutionContextFactoryImpl implements JobExecutionContextFactory {
  private final JobExecutorTokenService jobExecutorTokenService;
  private final JobExecutorLocaleService jobExecutorLocaleService;

  JobExecutionContextFactoryImpl(
      JobExecutorTokenService jobExecutorTokenService,
      JobExecutorLocaleService jobExecutorLocaleService) {

    this.jobExecutorTokenService = requireNonNull(jobExecutorTokenService);
    this.jobExecutorLocaleService = requireNonNull(jobExecutorLocaleService);
  }

  @Override
  public JobExecutionContext createJobExecutionContext(JobExecution jobExecution) {
    Authentication authentication = jobExecutorTokenService.createToken(jobExecution);
    Locale locale = jobExecutorLocaleService.createLocale(jobExecution);
    return JobExecutionContext.create(authentication, locale);
  }
}
