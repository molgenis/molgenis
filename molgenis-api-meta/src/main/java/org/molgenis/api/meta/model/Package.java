package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Package.class)
public abstract class Package {
  public abstract LinksResponse getLinks();

  public abstract String getId();

  public abstract String getLabel();

  public abstract String getDescription();

  public abstract Package getParent();

  public abstract List<Package> getChildren();

  public abstract List<EntityType> getEntityTypes();

  public abstract List<Tag> getTags();

  public static Builder builder() {
    return new AutoValue_Package.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setId(String id);

    public abstract Builder setLabel(String label);

    public abstract Builder setDescription(String description);

    public abstract Builder setParent(Package parent);

    public abstract Builder setChildren(List<Package> children);

    public abstract Builder setEntityTypes(List<EntityType> entityTypes);

    public abstract Builder setTags(List<Tag> tags);

    public abstract Package build();
  }
}
