package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeResponse.class)
public abstract class EntityTypeResponse {
  public abstract LinksResponse getLinks();

  // can be null when selecting zero attributes (e.g. for referenced entities)
  @Nullable
  @CheckForNull
  public abstract EntityTypeResponseData getData();

  public static EntityTypeResponse create(
      LinksResponse newLinks, EntityTypeResponseData entityType) {
    return builder().setLinks(newLinks).setData(entityType).build();
  }

  public static Builder builder() {
    return new AutoValue_EntityTypeResponse.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setData(EntityTypeResponseData entityType);

    public abstract EntityTypeResponse build();
  }
}
