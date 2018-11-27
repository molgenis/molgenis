package org.molgenis.integrationtest.utils;

import java.util.Objects;
import javax.annotation.Nullable;
import org.molgenis.jobs.Progress;
import org.molgenis.jobs.model.JobExecution;

public class TestProgress implements Progress {
  private static final String FAILED = "failed";
  private static final String CANCELED = "canceled";
  private static final String SUCCESS = "success";
  private static final String STARTED = "started";
  private int progress;
  private int progressMax;
  private String message;
  private String resultUrl;

  public TestProgress() {
    this.progress = 0;
    this.progressMax = -1;
    this.message = "";
    this.resultUrl = "";
  }

  public TestProgress(int progress, int progressMax, String message, String resultUrl) {
    this.progress = progress;
    this.progressMax = progressMax;
    this.message = message;
    this.resultUrl = resultUrl;
  }

  @Override
  public void start() {
    updateMessage(STARTED);
  }

  @Override
  public void setProgressMax(int max) {
    this.progressMax = max;
  }

  @Override
  public void progress(int progress, String message) {
    this.progress = progress;
    updateMessage(message);
  }

  @Override
  public void increment(int amount) {
    this.progress = progress + amount;
  }

  @Override
  public void status(String message) {
    updateMessage(message);
  }

  @Override
  public void failed(String message, @Nullable Throwable throwable) {
    updateMessage(FAILED + ": " + throwable.getMessage());
  }

  @Override
  public void canceled() {
    updateMessage(CANCELED);
  }

  @Override
  public void success() {
    updateMessage(SUCCESS);
  }

  @Override
  public Long timeRunning() {
    return -1L;
  }

  @Override
  public void setResultUrl(String resultUrl) {
    this.resultUrl = resultUrl;
  }

  @Override
  public JobExecution getJobExecution() {
    return null;
  }

  private void updateMessage(String message) {
    if (!message.isEmpty() && !this.message.isEmpty()) {
      this.message = "\n" + this.message;
    }
    this.message = message + this.message;
  }

  public int getProgress() {
    return progress;
  }

  public int getProgressMax() {
    return progressMax;
  }

  public String getMessage() {
    return message;
  }

  public String getResultUrl() {
    return resultUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestProgress that = (TestProgress) o;
    return progress == that.progress
        && progressMax == that.progressMax
        && Objects.equals(message, that.message)
        && Objects.equals(resultUrl, that.resultUrl);
  }

  @Override
  public String toString() {
    return "TestProgress{"
        + "progress="
        + progress
        + ", progressMax="
        + progressMax
        + ", message='"
        + message
        + '\''
        + ", resultUrl='"
        + resultUrl
        + '\''
        + '}';
  }
}
