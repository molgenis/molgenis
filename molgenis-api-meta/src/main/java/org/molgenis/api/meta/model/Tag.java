package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Tag.class)
public abstract class Tag {
  public abstract LinksResponse getLinks();

  public abstract String getId();

  public abstract String getObjectIRI();

  public abstract String getLabel();

  public abstract String getRelationIRI();

  public abstract String getRelationLabel();

  public abstract String getCodeSystem();

  public static Builder builder() {
    return new AutoValue_Tag.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setId(String id);

    public abstract Builder setObjectIRI(String objectIRI);

    public abstract Builder setLabel(String label);

    public abstract Builder setRelationIRI(String relationIRI);

    public abstract Builder setRelationLabel(String relationLabel);

    public abstract Builder setCodeSystem(String codeSystem);

    public abstract Tag build();
  }
}
