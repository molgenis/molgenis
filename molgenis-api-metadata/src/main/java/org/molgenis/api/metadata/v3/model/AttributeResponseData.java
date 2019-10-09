package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.data.Sort;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeResponseData.class)
public abstract class AttributeResponseData {

  public abstract String getId();

  public abstract String getName();

  public abstract Integer getSequenceNr();

  public abstract AttributeType getType();

  @Nullable
  @CheckForNull
  public abstract Integer getLookupAttributeIndex();

  @Nullable
  @CheckForNull
  public abstract LinksResponse getRefEntityType();

  @Nullable
  @CheckForNull
  public abstract Boolean getCascadeDelete();

  @Nullable
  @CheckForNull
  public abstract AttributeResponse getMappedBy();

  @Nullable
  @CheckForNull
  public abstract Sort getOrderBy();

  @Nullable
  @CheckForNull
  public abstract I18nValue getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  public abstract boolean isNullable();

  public abstract boolean isAuto();

  public abstract boolean isVisible();

  public abstract boolean isUnique();

  public abstract boolean isReadOnly();

  public abstract boolean isAggregatable();

  @Nullable
  @CheckForNull
  public abstract String getExpression();

  @Nullable
  @CheckForNull
  public abstract String getEnumOptions();

  @Nullable
  @CheckForNull
  public abstract Long getRangeMin();

  @Nullable
  @CheckForNull
  public abstract Long getRangeMax();

  @Nullable
  @CheckForNull
  public abstract String getParentAttributeId();

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

  public static Builder builder() {
    return new AutoValue_AttributeResponseData.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to Integererfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setSequenceNr(Integer sequenceNr);

    public abstract Builder setType(AttributeType type);

    public abstract Builder setLookupAttributeIndex(Integer index);

    public abstract Builder setRefEntityType(LinksResponse linksResponse);

    public abstract Builder setCascadeDelete(Boolean isCascadeDelete);

    public abstract Builder setMappedBy(AttributeResponse attribute);

    public abstract Builder setOrderBy(Sort sort);

    public abstract Builder setLabel(I18nValue label);

    public abstract Builder setDescription(I18nValue description);

    public abstract Builder setNullable(boolean isNullable);

    public abstract Builder setAuto(boolean isAuto);

    public abstract Builder setVisible(boolean isVisible);

    public abstract Builder setUnique(boolean isUnique);

    public abstract Builder setReadOnly(boolean isReadOnly);

    public abstract Builder setAggregatable(boolean isAggregatable);

    public abstract Builder setExpression(String expression);

    public abstract Builder setEnumOptions(String enumOptions);

    public abstract Builder setRangeMin(Long min);

    public abstract Builder setRangeMax(Long max);

    public abstract Builder setParentAttributeId(String parent);

    public abstract Builder setNullableExpression(String nullableExpression);

    public abstract Builder setVisibleExpression(String visibleExpression);

    public abstract Builder setValidationExpression(String validationExpression);

    public abstract Builder setDefaultValue(String defaultValue);

    public abstract AttributeResponseData build();
  }
}
