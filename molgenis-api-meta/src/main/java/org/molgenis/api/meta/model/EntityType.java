package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityType.class)
public abstract class EntityType {
  public abstract LinksResponse getLinks();

  public abstract String getId();

  public abstract Package getPackage();

  public abstract String getLabel();

  public abstract String getDescription();

  public abstract List<Attribute> getAttributes();

  public abstract boolean isAbstract();

  public abstract EntityType getExtends();

  public abstract List<String> getTags();

  public abstract String getBackend();

  public abstract int getIndexingDepth();

  public static EntityType.Builder builder() {
    return new AutoValue_EntityType.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setId(String id);

    public abstract Builder setPackage(Package pack);

    public abstract Builder setLabel(String label);

    public abstract Builder setDescription(String description);

    public abstract Builder setAttributes(List<Attribute> attributes);

    public abstract Builder setAbstract(boolean isAbstract);

    public abstract Builder setExtends(EntityType entityType);

    public abstract Builder setTags(List<String> tags);

    public abstract Builder setBackend(String backend);

    public abstract Builder setIndexingDepth(int indexingDepth);

    public abstract EntityType build();
  }
}
