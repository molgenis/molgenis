package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.navigator.model.ResourceIdentifier;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DeleteResourcesRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DeleteResourcesRequest {
  public abstract List<ResourceIdentifier> getResources();

  public static DeleteResourcesRequest create(List<ResourceIdentifier> newResources) {
    return builder().setResources(newResources).build();
  }

  public static Builder builder() {
    return new AutoValue_DeleteResourcesRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setResources(List<ResourceIdentifier> newResources);

    public abstract DeleteResourcesRequest build();
  }
}
