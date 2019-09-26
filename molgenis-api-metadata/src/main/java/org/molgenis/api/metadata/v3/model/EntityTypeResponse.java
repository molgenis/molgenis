package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
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
  public abstract EntityType getData();

  public static EntityTypeResponse create(
      String entityTypeId, LinksResponse newLinks, EntityType entityType) {
    return builder().setEntityTypeId(entityTypeId).setLinks(newLinks).setData(entityType).build();
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

    public abstract Builder setData(EntityType entityType);

    public abstract EntityTypeResponse build();
  }
}
