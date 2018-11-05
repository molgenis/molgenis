package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SearchResourcesResponse {
  public abstract List<Resource> getResources();

  public static SearchResourcesResponse create(List<Resource> newResources) {
    return new AutoValue_SearchResourcesResponse(newResources);
  }
}
