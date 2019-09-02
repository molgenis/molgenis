package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeResponse.class)
public abstract class EntityTypeResponse {
  public abstract String getEntityTypeId();

  public abstract LinksResponse getLinks();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract Map<String, Object> getData();

  public static EntityTypeResponse create(
      String entityTypeId, LinksResponse newLinks, Map<String, Object> newData) {
    return builder().setEntityTypeId(entityTypeId).setLinks(newLinks).setData(newData).build();
  }

  public static Builder builder() {
    return new AutoValue_EntityTypeResponse.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setEntityTypeId(String id);

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setData(Map<String, Object> newData);

    public abstract EntityTypeResponse build();
  }
}
