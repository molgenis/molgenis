package org.molgenis.jobs;

import javax.annotation.CheckForNull;
import org.molgenis.jobs.model.JobExecution;

public interface Progress {
  void start();

  void setProgressMax(int max);

  void progress(int progress, String message);

  void increment(int amount);

  void status(String message);

  void failed(String message, @CheckForNull Throwable throwable);

  void canceled();

  void success();

  Long timeRunning();

  void setResultUrl(String string);

  JobExecution getJobExecution();
}
