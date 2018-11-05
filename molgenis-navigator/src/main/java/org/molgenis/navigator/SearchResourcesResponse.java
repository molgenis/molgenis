package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SearchResourcesResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class SearchResourcesResponse {
  public abstract List<Resource> getResources();

  public static SearchResourcesResponse create(List<Resource> newResources) {
    return builder().setResources(newResources).build();
  }

  public static Builder builder() {
    return new AutoValue_SearchResourcesResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setResources(List<Resource> newResources);

    public abstract SearchResourcesResponse build();
  }
}
