package org.molgenis.api;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_VersionResponse.class)
@SuppressWarnings("squid:S1610")
public abstract class VersionResponse {
  public abstract AppVersionResponse getApp();

  public static VersionResponse create(AppVersionResponse appVersion) {
    return new AutoValue_VersionResponse(appVersion);
  }
}
