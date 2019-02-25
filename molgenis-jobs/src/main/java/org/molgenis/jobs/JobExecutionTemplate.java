package org.molgenis.jobs;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import org.molgenis.i18n.ErrorCoded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/** Template to execute jobs. */
class JobExecutionTemplate {
  private static final Logger LOG = LoggerFactory.getLogger(JobExecutionTemplate.class);

  /**
   * Executes a job in the calling thread.
   *
   * @param <T> Job result type
   * @param job the {@link Job} to execute
   * @param progress {@link Progress} to report progress to
   * @param jobExecutionContext {@link Authentication} to run the job with
   * @return the result of the job execution
   * @throws JobExecutionException if job execution throws an exception
   */
  <T> T call(Job<T> job, Progress progress, JobExecutionContext jobExecutionContext) {
    return runWithContext(jobExecutionContext, () -> tryCall(job, progress));
  }

  private <T> T runWithContext(JobExecutionContext jobExecutionContext, Callable<T> callable) {
    final SecurityContext originalContext = SecurityContextHolder.getContext();
    final Locale originalLocale = LocaleContextHolder.getLocale();
    try {
      SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
      SecurityContextHolder.getContext().setAuthentication(jobExecutionContext.getAuthentication());
      LocaleContextHolder.setLocale(jobExecutionContext.getLocale());
      return callable.call();
    } catch (RuntimeException rte) {
      throw rte;
    } catch (Exception e) {
      throw new IllegalStateException("Lambda should only throw runtime exception", e);
    } finally {
      SecurityContextHolder.setContext(originalContext);
      LocaleContextHolder.setLocale(originalLocale);
    }
  }

  private <T> T tryCall(Job<T> job, Progress progress) {
    progress.start();
    try {
      T result = job.call(progress);
      progress.success();
      return result;
    } catch (CancellationException e) {
      progress.canceled();
      throw new JobExecutionException(e);
    } catch (Exception ex) {
      LOG.warn("Error executing job", ex);
      progress.failed(createFailureMessage(ex), ex);
      throw new JobExecutionException(ex);
    }
  }

  private String createFailureMessage(Exception exception) {
    String message = exception.getLocalizedMessage();
    if (exception instanceof ErrorCoded) {
      message += " (" + ((ErrorCoded) exception).getErrorCode() + ')';
    }
    return message;
  }
}
