package org.molgenis.web.exception;

import com.google.auto.value.AutoValue;
import java.net.URI;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@AutoValue
abstract class Problem {
  abstract URI getType();

  abstract String getTitle();

  abstract int getStatus();

  @Nullable
  @CheckForNull
  abstract String getDetail();

  @Nullable
  @CheckForNull
  abstract URI getInstance();

  @Nullable
  @CheckForNull
  abstract String getErrorCode();

  @Nullable
  @CheckForNull
  abstract List<String> getStackTrace();

  public static Problem create(
      URI newType,
      String newTitle,
      int newStatus,
      @Nullable @CheckForNull String newDetail,
      @Nullable @CheckForNull URI newInstance,
      @Nullable @CheckForNull String newErrorCode,
      @Nullable @CheckForNull List<String> newStackTrace) {
    return builder()
        .setType(newType)
        .setTitle(newTitle)
        .setStatus(newStatus)
        .setDetail(newDetail)
        .setInstance(newInstance)
        .setErrorCode(newErrorCode)
        .setStackTrace(newStackTrace)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_Problem.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setType(URI newType);

    public abstract Builder setTitle(String newTitle);

    public abstract Builder setStatus(int newStatus);

    public abstract Builder setDetail(String newDetail);

    public abstract Builder setInstance(URI newInstance);

    public abstract Builder setErrorCode(String newErrorCode);

    public abstract Builder setStackTrace(List<String> newStackTrace);

    public abstract Problem build();
  }
}
