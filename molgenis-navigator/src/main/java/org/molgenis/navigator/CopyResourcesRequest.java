package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CopyResourcesRequest.class)
public abstract class CopyResourcesRequest {
  public abstract List<Resource> getResources();

  public abstract String getTargetFolderId();

  public static CopyResourcesRequest create(List<Resource> resources, String targetFolderId) {
    return new AutoValue_CopyResourcesRequest(resources, targetFolderId);
  }
}
