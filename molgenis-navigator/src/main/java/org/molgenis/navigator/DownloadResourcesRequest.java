package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DownloadResourcesRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DownloadResourcesRequest {
  @NotEmpty
  public abstract List<ResourceIdentifier> getResources();

  public static DownloadResourcesRequest create(List<ResourceIdentifier> newResources) {
    return builder().setResources(newResources).build();
  }

  public static Builder builder() {
    return new AutoValue_DownloadResourcesRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setResources(List<ResourceIdentifier> newResources);

    public abstract DownloadResourcesRequest build();
  }
}
