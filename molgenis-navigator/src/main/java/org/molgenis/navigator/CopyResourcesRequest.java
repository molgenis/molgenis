package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.gson.AutoGson;
import org.molgenis.navigator.model.ResourceIdentifier;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyResourcesRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CopyResourcesRequest {
  public abstract List<ResourceIdentifier> getResources();

  public abstract String getTargetFolderId();

  public static CopyResourcesRequest create(
      List<ResourceIdentifier> newResources, String newTargetFolderId) {
    return builder().setResources(newResources).setTargetFolderId(newTargetFolderId).build();
  }

  public static Builder builder() {
    return new AutoValue_CopyResourcesRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setResources(List<ResourceIdentifier> newResources);

    public abstract Builder setTargetFolderId(String newTargetFolderId);

    public abstract CopyResourcesRequest build();
  }
}
