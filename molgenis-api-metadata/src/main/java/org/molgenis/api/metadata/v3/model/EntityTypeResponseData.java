package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import com.google.gson.annotations.SerializedName;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeResponseData.class)
public abstract class EntityTypeResponseData {

  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getLabelI18n();

  @Nullable
  @CheckForNull
  public abstract String getDescription();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescriptionI18n();

  @Nullable
  @CheckForNull
  public abstract AttributeResponse getLabelAttribute();

  @Nullable
  @CheckForNull
  public abstract AttributeResponse getIdAttribute();

  @Nullable
  @CheckForNull
  public abstract AttributesResponse getLookupAttributes();

  public abstract AttributesResponse getAttributes();

  @Nullable
  @CheckForNull
  public abstract PackageResponse getPackage_();

  @SerializedName("blaat")
  public abstract boolean isAbstract();

  @Nullable
  @CheckForNull
  public abstract EntityTypeResponse getExtends_();

  @Nullable
  @CheckForNull
  public abstract Integer getIndexingDepth();

  public static EntityTypeResponseData.Builder builder() {
    return new AutoValue_EntityTypeResponseData.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setPackage_(PackageResponse packageResponse);

    public abstract Builder setLabel(String label);

    public abstract Builder setLabelI18n(I18nValue label);

    public abstract Builder setDescription(String description);

    public abstract Builder setDescriptionI18n(I18nValue description);

    public abstract Builder setAttributes(AttributesResponse attributes);

    public abstract Builder setAbstract(boolean isAbstract);

    public abstract Builder setExtends_(EntityTypeResponse entityType);

    public abstract Builder setIndexingDepth(Integer indexingDepth);

    public abstract Builder setLabelAttribute(AttributeResponse labelAttribute);

    public abstract Builder setIdAttribute(AttributeResponse idAttribute);

    public abstract Builder setLookupAttributes(AttributesResponse lookupAttributes);

    public abstract EntityTypeResponseData build();
  }
}
