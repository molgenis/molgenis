package org.molgenis.api.metadata.v3.job;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SerializableAttribute.class)
abstract class SerializableAttribute {
  abstract String getId();

  abstract String getName();

  abstract int getSequenceNr();

  abstract String getType();

  abstract boolean isIdAttribute();

  abstract boolean isLabelAttribute();

  @Nullable
  @CheckForNull
  abstract Integer getLookupAttributeIndex();

  @Nullable
  @CheckForNull
  abstract String getRefEntityTypeId();

  abstract Optional<Boolean> getCascadeDelete();

  @Nullable
  @CheckForNull
  abstract String getMappedById();

  @Nullable
  @CheckForNull
  abstract String getOrderBy();

  abstract String getLabel();

  abstract ImmutableMap<String, String> getLabelI18n();

  @Nullable
  @CheckForNull
  abstract String getDescription();

  abstract ImmutableMap<String, String> getDescriptionI18n();

  abstract boolean isNullable();

  abstract boolean isAuto();

  abstract boolean isVisible();

  abstract boolean isUnique();

  abstract boolean isReadOnly();

  abstract boolean isAggregatable();

  @Nullable
  @CheckForNull
  abstract String getExpression();

  @Nullable
  @CheckForNull
  abstract ImmutableList<String> getEnumOptions();

  @Nullable
  @CheckForNull
  abstract Long getRangeMin();

  @Nullable
  @CheckForNull
  abstract Long getRangeMax();

  @Nullable
  @CheckForNull
  abstract String getParentId();

  abstract ImmutableList<String> getTagIds();

  @Nullable
  @CheckForNull
  abstract String getNullableExpression();

  @Nullable
  @CheckForNull
  abstract String getVisibleExpression();

  @Nullable
  @CheckForNull
  abstract String getValidationExpression();

  @Nullable
  @CheckForNull
  abstract String getDefaultValue();

  public static Builder builder() {
    return new AutoValue_SerializableAttribute.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setName(String newName);

    public abstract Builder setSequenceNr(int newSequenceNr);

    public abstract Builder setType(String newType);

    public abstract Builder setIdAttribute(boolean newIdAttribute);

    public abstract Builder setLabelAttribute(boolean newLabelAttribute);

    public abstract Builder setLookupAttributeIndex(Integer newLookupAttributeIndex);

    public abstract Builder setRefEntityTypeId(String newRefEntityTypeId);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public abstract Builder setCascadeDelete(Optional<Boolean> newCascadeDelete);

    public abstract Builder setMappedById(String newMappedById);

    public abstract Builder setOrderBy(String newOrderBy);

    public abstract Builder setLabel(String newLabel);

    public abstract Builder setLabelI18n(ImmutableMap<String, String> newLabelI18n);

    public abstract Builder setDescription(String newDescription);

    public abstract Builder setDescriptionI18n(ImmutableMap<String, String> newDescriptionI18n);

    public abstract Builder setNullable(boolean newNullable);

    public abstract Builder setAuto(boolean newAuto);

    public abstract Builder setVisible(boolean newVisible);

    public abstract Builder setUnique(boolean newUnique);

    public abstract Builder setReadOnly(boolean newReadOnly);

    public abstract Builder setAggregatable(boolean newAggregatable);

    public abstract Builder setExpression(String newExpression);

    public abstract Builder setEnumOptions(ImmutableList<String> newEnumOptions);

    public abstract Builder setRangeMin(Long newRangeMin);

    public abstract Builder setRangeMax(Long newRangeMax);

    public abstract Builder setParentId(String newParentId);

    public abstract Builder setTagIds(ImmutableList<String> newTagIds);

    public abstract Builder setNullableExpression(String newNullableExpression);

    public abstract Builder setVisibleExpression(String newVisibleExpression);

    public abstract Builder setValidationExpression(String newValidationExpression);

    public abstract Builder setDefaultValue(String newDefaultValue);

    public abstract SerializableAttribute build();
  }
}
