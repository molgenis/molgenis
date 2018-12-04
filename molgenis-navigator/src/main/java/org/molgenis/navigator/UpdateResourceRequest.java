package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.navigator.model.Resource;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateResourceRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class UpdateResourceRequest {
  public abstract Resource getResource();

  public static UpdateResourceRequest create(Resource newResource) {
    return builder().setResource(newResource).build();
  }

  public static Builder builder() {
    return new AutoValue_UpdateResourceRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setResource(Resource newResource);

    public abstract UpdateResourceRequest build();
  }
}
