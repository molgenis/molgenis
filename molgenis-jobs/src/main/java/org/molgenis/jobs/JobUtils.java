package org.molgenis.jobs;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;

public class JobUtils {
  private JobUtils() {}

  /**
   * Clear the security context and locale context if the job was run in another thread. This
   * prevents accidental use of the contexts when a thread is reused. Furthermore this prevents
   * memory leaks by clearing thread-local values.
   *
   * @param callingThreadId identifier of the thread that requested execution (might be the same as
   *     the execution thread)
   */
  public static void cleanupAfterRunJob(long callingThreadId) {
    if (Thread.currentThread().getId() == callingThreadId) {
      return;
    }

    SecurityContextHolder.clearContext();
    LocaleContextHolder.resetLocaleContext();
  }
}
