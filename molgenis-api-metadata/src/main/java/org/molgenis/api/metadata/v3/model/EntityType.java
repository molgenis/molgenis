package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import java.net.URI;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityType.class)
public abstract class EntityType {

  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract URI getPackage_();

  @Nullable
  @CheckForNull
  public abstract I18nValue getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  public abstract List<AttributeResponse> getAttributes();

  @Nullable
  @CheckForNull
  public abstract String getLabelAttribute();

  @Nullable
  @CheckForNull
  public abstract String getIdAttribute();

  public abstract boolean isAbstract_();

  @Nullable
  @CheckForNull
  public abstract EntityTypeResponse getExtends();

  @Nullable
  @CheckForNull
  public abstract String getBackend();

  @Nullable
  @CheckForNull
  public abstract Integer getIndexingDepth();

  public static EntityType.Builder builder() {
    return new AutoValue_EntityType.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setPackage_(URI pack);

    public abstract Builder setLabel(I18nValue label);

    public abstract Builder setDescription(I18nValue description);

    public abstract Builder setAttributes(List<AttributeResponse> attributes);

    public abstract Builder setLabelAttribute(String attributeId);

    public abstract Builder setIdAttribute(String attributeId);

    public abstract Builder setAbstract_(boolean isAbstract);

    public abstract Builder setExtends(EntityTypeResponse entityType);

    public abstract Builder setBackend(String backend);

    public abstract Builder setIndexingDepth(Integer indexingDepth);

    public abstract EntityType build();
  }
}
