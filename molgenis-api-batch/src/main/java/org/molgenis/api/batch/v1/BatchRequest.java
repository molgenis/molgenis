package org.molgenis.api.batch.v1;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_BatchRequest.class)
public abstract class BatchRequest {
  public abstract List<Request> getRequests();

  public static BatchRequest create(List<Request> newRequests) {
    return builder().setRequests(newRequests).build();
  }

  public static Builder builder() {
    return new AutoValue_BatchRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setRequests(List<Request> newRequests);

    public abstract BatchRequest build();
  }
}
