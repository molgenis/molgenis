package org.molgenis.navigator.download;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.gson.AutoGson;
import org.molgenis.navigator.model.Resource;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DownloadResourcesRequest.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DownloadResourcesRequest {

  public abstract List<Resource> getResources();

  public static DownloadResourcesRequest create(List<Resource> resources) {
    return new AutoValue_DownloadResourcesRequest(resources);
  }
}
