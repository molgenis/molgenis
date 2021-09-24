package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.molgenis.api.model.Order;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateAttributeRequest.class)
public abstract class CreateAttributeRequest {
  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getId();

  @CopyAnnotations(exclude = NotNull.class)
  @NotNull
  public abstract String getName();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class, Pattern.class})
  @Pattern(
      regexp =
          "bool|categorical|categorical_mref|compound|date|date_time|decimal|email|enum|file|html|hyperlink|int|long|mref|one_to_many|script|string|text|xref")
  @Nullable
  @CheckForNull
  public abstract String getType();

  @Nullable
  @CheckForNull
  public abstract Integer getMaxLength();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getParent();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getRefEntityType();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getCascadeDelete();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getMappedByAttribute();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract ImmutableList<Order> getOrderBy();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getExpression();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getNullable();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getAuto();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getVisible();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract I18nValue getLabel();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getAggregatable();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract List<String> getEnumOptions();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Range getRange();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getReadonly();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getUnique();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getNullableExpression();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getVisibleExpression();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getValidationExpression();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract String getDefaultValue();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Integer getSequenceNr();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getIdAttribute();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class})
  @Nullable
  @CheckForNull
  public abstract Boolean getLabelAttribute();

  @CopyAnnotations(exclude = {CheckForNull.class, Nullable.class, Min.class})
  @Nullable
  @CheckForNull
  @Min(0)
  public abstract Integer getLookupAttributeIndex();

  public static Builder builder() {
    return new AutoValue_CreateAttributeRequest.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(@Nullable @CheckForNull String newId);

    public abstract Builder setName(String newName);

    public abstract Builder setType(String newType);

    public abstract Builder setMaxLength(Integer maxLength);

    public abstract Builder setParent(@Nullable @CheckForNull String newParent);

    public abstract Builder setRefEntityType(@Nullable @CheckForNull String newRefEntityType);

    public abstract Builder setCascadeDelete(@Nullable @CheckForNull Boolean newCascadeDelete);

    public abstract Builder setMappedByAttribute(
        @Nullable @CheckForNull String newMappedByAttribute);

    public abstract Builder setOrderBy(@Nullable @CheckForNull ImmutableList<Order> newOrderBy);

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

    public abstract Builder setSequenceNr(@Nullable @CheckForNull Integer newSequenceNr);

    public abstract Builder setIdAttribute(@Nullable @CheckForNull Boolean newIdAttribute);

    public abstract Builder setLabelAttribute(@Nullable @CheckForNull Boolean newLabelAttribute);

    public abstract Builder setLookupAttributeIndex(
        @Nullable @CheckForNull Integer newLookupAttributeIndex);

    public abstract CreateAttributeRequest build();
  }
}
