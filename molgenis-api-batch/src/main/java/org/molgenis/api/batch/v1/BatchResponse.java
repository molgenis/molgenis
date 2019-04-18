package org.molgenis.api.batch.v1;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_BatchResponse.class)
public abstract class BatchResponse {
  public abstract List<Response> getResponses();

  public static BatchResponse create(List<Response> newResponses) {
    return builder().setResponses(newResponses).build();
  }

  public static Builder builder() {
    return new AutoValue_BatchResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setResponses(List<Response> newResponses);

    public abstract BatchResponse build();
  }
}
