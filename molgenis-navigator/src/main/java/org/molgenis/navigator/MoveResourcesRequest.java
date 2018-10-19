package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MoveResourcesRequest.class)
public abstract class MoveResourcesRequest {
  public abstract List<Resource> getResources();

  public abstract String getTargetFolderId();

  public static MoveResourcesRequest create(List<Resource> resources, String targetFolderId) {
    return new AutoValue_MoveResourcesRequest(resources, targetFolderId);
  }
}
