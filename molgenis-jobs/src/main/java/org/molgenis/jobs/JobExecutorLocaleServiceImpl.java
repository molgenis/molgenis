package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.web.i18n.UserLocaleResolver;
import org.springframework.stereotype.Component;

@Component
class JobExecutorLocaleServiceImpl implements JobExecutorLocaleService {
  private final UserLocaleResolver userLocaleResolver;

  JobExecutorLocaleServiceImpl(UserLocaleResolver userLocaleResolver) {
    this.userLocaleResolver = requireNonNull(userLocaleResolver);
  }

  @Override
  public Locale createLocale(JobExecution jobExecution) {
    return jobExecution
        .getUser()
        .map(userLocaleResolver::resolveLocale)
        .orElseGet(Locale::getDefault);
  }
}
