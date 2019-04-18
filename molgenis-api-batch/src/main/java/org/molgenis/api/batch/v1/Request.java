package org.molgenis.api.batch.v1;

import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.net.URI;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.springframework.http.HttpMethod;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Request.class)
public abstract class Request {
  public abstract String getId();

  public abstract HttpMethod getMethod();

  public abstract URI getUrl();

  @Nullable
  @CheckForNull
  public abstract Map<String, String> getHeaders();

  @Nullable
  @CheckForNull
  public abstract JsonObject getBody();

  public static Request create(
      String newId,
      HttpMethod newMethod,
      URI newUrl,
      Map<String, String> newHeaders,
      JsonObject newBody) {
    return builder()
        .setId(newId)
        .setMethod(newMethod)
        .setUrl(newUrl)
        .setHeaders(newHeaders)
        .setBody(newBody)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_Request.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setId(String newId);

    public abstract Builder setMethod(HttpMethod newMethod);

    public abstract Builder setUrl(URI newUrl);

    public abstract Builder setHeaders(Map<String, String> newHeaders);

    public abstract Builder setBody(JsonObject newBody);

    public abstract Request build();
  }
}
