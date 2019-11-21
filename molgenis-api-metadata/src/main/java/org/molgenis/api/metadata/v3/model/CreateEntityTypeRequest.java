package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateEntityTypeRequest.class)
public abstract class CreateEntityTypeRequest {
  @Nullable
  @CheckForNull
  public abstract String getId();

  public abstract I18nValue getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  @Nullable
  @CheckForNull
  @CopyAnnotations
  @SerializedName(value = "abstract")
  public abstract Boolean getAbstract();

  @Nullable
  @CheckForNull
  @CopyAnnotations
  @SerializedName(value = "package")
  public abstract String getPackage();

  @Nullable
  @CheckForNull
  @CopyAnnotations
  @SerializedName(value = "extends")
  public abstract String getExtends();

  @Nullable
  @CheckForNull
  public abstract ImmutableList<CreateAttributeRequest> getAttributes();

  @Nullable
  @CheckForNull
  public abstract String getIdAttribute();

  @Nullable
  @CheckForNull
  public abstract String getLabelAttribute();

  @Nullable
  @CheckForNull
  public abstract ImmutableList<String> getLookupAttributes();

  public static Builder builder() {
    return new AutoValue_CreateEntityTypeRequest.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(@Nullable @CheckForNull String newId);

    public abstract Builder setLabel(I18nValue newLabel);

    public abstract Builder setDescription(@Nullable @CheckForNull I18nValue newDescription);

    public abstract Builder setAbstract(@Nullable @CheckForNull Boolean newAbstract);

    public abstract Builder setPackage(@Nullable @CheckForNull String newPackage);

    public abstract Builder setExtends(@Nullable @CheckForNull String newExtends);

    public abstract Builder setAttributes(
        @Nullable @CheckForNull ImmutableList<CreateAttributeRequest> newAttributes);

    public abstract Builder setIdAttribute(@Nullable @CheckForNull String newIdAttribute);

    public abstract Builder setLabelAttribute(@Nullable @CheckForNull String newLabelAttribute);

    public abstract Builder setLookupAttributes(
        @Nullable @CheckForNull ImmutableList<String> newLookupAttributes);

    public abstract CreateEntityTypeRequest build();
  }
}
