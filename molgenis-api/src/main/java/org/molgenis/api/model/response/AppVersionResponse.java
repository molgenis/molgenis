package org.molgenis.api.model.response;

import com.google.auto.value.AutoValue;
import java.time.Instant;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AppVersionResponse.class)
@SuppressWarnings("java:S1610")
public abstract class AppVersionResponse {
  public abstract String getVersion();

  public abstract Instant getBuildDate();

  public static AppVersionResponse create(String version, Instant date) {
    return new AutoValue_AppVersionResponse(version, date);
  }
}
