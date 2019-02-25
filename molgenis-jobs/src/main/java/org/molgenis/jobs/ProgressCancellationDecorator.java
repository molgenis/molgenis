package org.molgenis.jobs;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.util.concurrent.CancellationException;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.jobs.model.JobExecution;

class ProgressCancellationDecorator extends ForwardingObject implements Progress {
  private final Progress delegateProgress;
  private volatile boolean cancelOnNextProgressChange;

  ProgressCancellationDecorator(Progress delegateProgress) {
    this.delegateProgress = requireNonNull(delegateProgress);
  }

  @Override
  protected Object delegate() {
    return delegateProgress;
  }

  @Override
  public void start() {
    delegateProgress.start();
    verifyCancelJob();
  }

  @Override
  public void setProgressMax(int max) {
    delegateProgress.setProgressMax(max);
    verifyCancelJob();
  }

  @Override
  public void progress(int progress, String message) {
    verifyCancelJob();
    delegateProgress.progress(progress, message);
  }

  @Override
  public void increment(int amount) {
    verifyCancelJob();
    delegateProgress.increment(amount);
  }

  @Override
  public void status(String message) {
    verifyCancelJob();
    delegateProgress.status(message);
  }

  @Override
  public void failed(String message, @CheckForNull @Nullable Throwable throwable) {
    verifyCancelJob();
    delegateProgress.failed(message, throwable);
  }

  @Override
  public void canceled() {
    delegateProgress.canceled();
  }

  @Override
  public void success() {
    verifyCancelJob();
    delegateProgress.success();
  }

  @Override
  public Long timeRunning() {
    return delegateProgress.timeRunning();
  }

  @Override
  public void setResultUrl(String string) {
    delegateProgress.setResultUrl(string);
  }

  @Override
  public JobExecution getJobExecution() {
    return delegateProgress.getJobExecution();
  }

  @Override
  public void canceling() {
    delegateProgress.canceling();
    cancelOnNextProgressChange = true;
  }

  /** @throws CancellationException if the job execution should be canceled */
  private void verifyCancelJob() {
    if (cancelOnNextProgressChange) {
      throw new CancellationException();
    }
  }
}
