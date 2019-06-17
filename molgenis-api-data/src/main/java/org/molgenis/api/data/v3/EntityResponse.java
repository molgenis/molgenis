package org.molgenis.api.data.v3;

import com.google.auto.value.AutoValue;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityResponse.class)
public abstract class EntityResponse {
  public abstract LinksResponse getLinks();

  @Nullable
  @CheckForNull
  public abstract Map<String, Object> getData();

  public static EntityResponse create(LinksResponse newLinks, Map<String, Object> newData) {
    return builder().setLinks(newLinks).setData(newData).build();
  }

  public static Builder builder() {
    return new AutoValue_EntityResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setData(Map<String, Object> newData);

    public abstract EntityResponse build();
  }
}
