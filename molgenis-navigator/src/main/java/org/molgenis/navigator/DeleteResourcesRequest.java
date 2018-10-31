package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_DeleteResourcesRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class DeleteResourcesRequest {
  public abstract List<Resource> getResources();

  public static DeleteResourcesRequest create(List<Resource> resources) {
    return new AutoValue_DeleteResourcesRequest(resources);
  }
}
