package org.molgenis.navigator.download;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.navigator.resource.Resource;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DownloadResourcesRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DownloadResourcesRequest {

  public abstract List<Resource> getResources();

  public static DownloadResourcesRequest create(List<Resource> resources) {
    return new AutoValue_DownloadResourcesRequest(resources);
  }
}
