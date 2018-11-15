package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;

/**
 * Superclass for molgenis jobs that keeps track of their progress. Delegates the hard work to
 * {@link JobExecutionTemplate}
 *
 * @deprecated Do the actual work in a Service bean with @Transactional annotation and configure a
 *     {@link JobFactory} instead.
 */
@Deprecated
public abstract class NontransactionalJob<T> implements Callable<T>, Job<T> {
  private final Progress progress;
  private Authentication authentication;
  private final JobExecutionTemplate jobExecutionTemplate = new JobExecutionTemplate();

  public NontransactionalJob(Progress progress, Authentication authentication) {
    this.progress = requireNonNull(progress);
    this.authentication = requireNonNull(authentication);
  }

  @Override
  public T call() {
    // FIXME determine locale using job username
    JobExecutionContext jobExecutionContext =
        JobExecutionContext.builder()
            .setAuthentication(authentication)
            .setLocale(LocaleContextHolder.getLocale())
            .build();
    return jobExecutionTemplate.call(this, progress, jobExecutionContext);
  }
}
