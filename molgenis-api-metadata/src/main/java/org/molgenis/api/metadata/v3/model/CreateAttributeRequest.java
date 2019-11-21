package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateAttributeRequest.class)
public abstract class CreateAttributeRequest {
  @Nullable
  @CheckForNull
  public abstract String getId();

  public abstract String getName();

  public abstract String getType();

  @Nullable
  @CheckForNull
  public abstract String getParent();

  @Nullable
  @CheckForNull
  public abstract String getRefEntityType();

  @Nullable
  @CheckForNull
  public abstract Boolean getCascadeDelete();

  @Nullable
  @CheckForNull
  public abstract String getMappedByAttribute();

  @Nullable
  @CheckForNull
  public abstract String getOrderBy();

  @Nullable
  @CheckForNull
  public abstract String getExpression();

  @Nullable
  @CheckForNull
  public abstract Boolean getNullable();

  @Nullable
  @CheckForNull
  public abstract Boolean getAuto();

  @Nullable
  @CheckForNull
  public abstract Boolean getVisible();

  @Nullable
  @CheckForNull
  public abstract I18nValue getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  @Nullable
  @CheckForNull
  public abstract Boolean getAggregatable();

  @Nullable
  @CheckForNull
  public abstract List<String> getEnumOptions();

  @Nullable
  @CheckForNull
  public abstract Range getRange();

  @Nullable
  @CheckForNull
  public abstract Boolean getReadonly();

  @Nullable
  @CheckForNull
  public abstract Boolean getUnique();

  @Nullable
  @CheckForNull
  public abstract String getNullableExpression();

  @Nullable
  @CheckForNull
  public abstract String getVisibleExpression();

  @Nullable
  @CheckForNull
  public abstract String getValidationExpression();

  @Nullable
  @CheckForNull
  public abstract String getDefaultValue();

  @Nullable
  @CheckForNull
  public abstract Integer getSequenceNumber();

  public static Builder builder() {
    return new AutoValue_CreateAttributeRequest.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(@Nullable @CheckForNull String newId);

    public abstract Builder setName(String newName);

    public abstract Builder setType(String newType);

    public abstract Builder setParent(@Nullable @CheckForNull String newParent);

    public abstract Builder setRefEntityType(@Nullable @CheckForNull String newRefEntityType);

    public abstract Builder setCascadeDelete(@Nullable @CheckForNull Boolean newCascadeDelete);

    public abstract Builder setMappedByAttribute(
        @Nullable @CheckForNull String newMappedByAttribute);

    public abstract Builder setOrderBy(@Nullable @CheckForNull String newOrderBy);

    public abstract Builder setExpression(@Nullable @CheckForNull String newExpression);

    public abstract Builder setNullable(@Nullable @CheckForNull Boolean newNullable);

    public abstract Builder setAuto(@Nullable @CheckForNull Boolean newAuto);

    public abstract Builder setVisible(@Nullable @CheckForNull Boolean newVisible);

    public abstract Builder setLabel(@Nullable @CheckForNull I18nValue newLabel);

    public abstract Builder setDescription(@Nullable @CheckForNull I18nValue newDescription);

    public abstract Builder setAggregatable(@Nullable @CheckForNull Boolean newAggregatable);

    public abstract Builder setEnumOptions(@Nullable @CheckForNull List<String> newEnumOptions);

    public abstract Builder setRange(@Nullable @CheckForNull Range newRange);

    public abstract Builder setReadonly(@Nullable @CheckForNull Boolean newReadonly);

    public abstract Builder setUnique(@Nullable @CheckForNull Boolean newUnique);

    public abstract Builder setNullableExpression(
        @Nullable @CheckForNull String newNullableExpression);

    public abstract Builder setVisibleExpression(
        @Nullable @CheckForNull String newVisibleExpression);

    public abstract Builder setValidationExpression(
        @Nullable @CheckForNull String newValidationExpression);

    public abstract Builder setDefaultValue(@Nullable @CheckForNull String newDefaultValue);

    public abstract Builder setSequenceNumber(@Nullable @CheckForNull Integer newSequenceNumber);

    public abstract CreateAttributeRequest build();
  }
}
