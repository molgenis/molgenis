package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyResourcesResponse.class)
public abstract class CopyResourcesResponse {
  public abstract String getJobId();

  public abstract String getJobStatus();

  public static CopyResourcesResponse create(String jobId, String jobStatus) {
    return new AutoValue_CopyResourcesResponse(jobId, jobStatus);
  }
}
