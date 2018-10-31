package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_UpdateResourceRequest.class)
public abstract class UpdateResourceRequest {
  public abstract Resource getResource();

  public static UpdateResourceRequest create(Resource resource) {
    return new AutoValue_UpdateResourceRequest(resource);
  }
}
