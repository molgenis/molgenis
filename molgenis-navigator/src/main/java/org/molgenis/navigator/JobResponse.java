package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_JobResponse.class)
public abstract class JobResponse {
  public abstract String getJobId();

  public abstract String getJobStatus();

  public static JobResponse create(String newJobId, String newJobStatus) {
    return builder().setJobId(newJobId).setJobStatus(newJobStatus).build();
  }

  public static Builder builder() {
    return new AutoValue_JobResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setJobId(String newJobId);

    public abstract Builder setJobStatus(String newJobStatus);

    public abstract JobResponse build();
  }
}
