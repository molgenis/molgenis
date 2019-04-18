package org.molgenis.api.batch.v1;

import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@AutoValue
public abstract class Response {
  public abstract String getId();

  public abstract int getStatus();

  @Nullable
  @CheckForNull
  public abstract Map<String, String> getHeaders();

  @Nullable
  @CheckForNull
  public abstract JsonObject getBody();

  public static Response create(
      String newId, int newStatus, Map<String, String> newHeaders, JsonObject newBody) {
    return builder()
        .setId(newId)
        .setStatus(newStatus)
        .setHeaders(newHeaders)
        .setBody(newBody)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_Response.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String newId);

    public abstract Builder setStatus(int newStatus);

    public abstract Builder setHeaders(Map<String, String> newHeaders);

    public abstract Builder setBody(JsonObject newBody);

    public abstract Response build();
  }
}
