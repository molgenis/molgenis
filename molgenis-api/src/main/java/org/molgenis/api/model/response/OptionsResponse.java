package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OptionsResponse.class)
@SuppressWarnings("java:S1610")
public abstract class OptionsResponse {
  public abstract AppVersionResponse getApp();

  public static OptionsResponse create(AppVersionResponse appVersion) {
    return new AutoValue_OptionsResponse(appVersion);
  }
}
