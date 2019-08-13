package org.molgenis.api;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OptionsResponse.class)
@SuppressWarnings("squid:S1610")
public abstract class OptionsResponse {
  public abstract AppVersionResponse getApp();

  public static OptionsResponse create(AppVersionResponse appVersion) {
    return new AutoValue_OptionsResponse(appVersion);
  }
}
