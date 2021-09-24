package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateEntityTypeRequest.class)
public abstract class CreateEntityTypeRequest {
  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getId();

  @CopyAnnotations(exclude = {NotNull.class, Valid.class})
  @Valid
  @NotNull
  public abstract I18nValue getLabel();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  @SerializedName(value = "abstract")
  public abstract Boolean getAbstract();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  @SerializedName(value = "package")
  public abstract String getPackage();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  @SerializedName(value = "extends")
  public abstract String getExtends();

  @CopyAnnotations(exclude = {NotEmpty.class, Valid.class})
  @Valid
  @NotEmpty
  public abstract ImmutableList<CreateAttributeRequest> getAttributes();

  public static Builder builder() {
    return new AutoValue_CreateEntityTypeRequest.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
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

    public abstract CreateEntityTypeRequest build();
  }
}
