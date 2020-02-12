package org.molgenis.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorServiceUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceUtils.class);

  private ExecutorServiceUtils() {}

  /**
   * Shuts down an ExecutorService in two phases, first by calling shutdown to reject incoming
   * tasks, and then calling shutdownNow, if necessary, to cancel any lingering tasks.
   *
   * <p>Uses default timeout values.
   *
   * @param executorService executor service to shutdown
   */
  public static void shutdownAndAwaitTermination(ExecutorService executorService) {
    shutdownAndAwaitTermination(executorService, 10, 5);
  }

  /**
   * Shuts down an ExecutorService in two phases, first by calling shutdown to reject incoming
   * tasks, and then calling shutdownNow, if necessary, to cancel any lingering tasks.
   *
   * @param shutdownTimeout shutdown timeout in seconds
   * @param shutdownNowTimeout shutdown now timeout in seconds
   * @param executorService executor service to shutdown
   */
  public static void shutdownAndAwaitTermination(
      ExecutorService executorService, long shutdownTimeout, long shutdownNowTimeout) {
    // Copied from ExecutorService javadoc
    executorService.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!executorService.awaitTermination(shutdownTimeout, TimeUnit.SECONDS)) {
        executorService.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!executorService.awaitTermination(shutdownNowTimeout, TimeUnit.SECONDS)) {
          LOG.error("Executor service thread pool did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      executorService.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
