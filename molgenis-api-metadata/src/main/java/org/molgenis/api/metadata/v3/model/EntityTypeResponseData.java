package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
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
  @CopyAnnotations
  @SerializedName(value = "package")
  public abstract PackageResponse getPackage();

  public abstract AttributesResponse getAttributes();

  @CopyAnnotations
  @SerializedName(value = "abstract")
  public abstract boolean isAbstract();

  @Nullable
  @CheckForNull
  @CopyAnnotations
  @SerializedName(value = "extends")
  public abstract EntityTypeResponse getExtends();

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

    public abstract Builder setLabel(String label);

    public abstract Builder setLabelI18n(I18nValue label);

    public abstract Builder setDescription(String description);

    public abstract Builder setDescriptionI18n(I18nValue description);

    public abstract Builder setPackage(PackageResponse packageResponse);

    public abstract Builder setAttributes(AttributesResponse attributes);

    public abstract Builder setAbstract(boolean isAbstract);

    public abstract Builder setExtends(EntityTypeResponse entityType);

    public abstract Builder setIndexingDepth(Integer indexingDepth);

    public abstract EntityTypeResponseData build();
  }
}
