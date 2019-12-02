package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.util.AutoGson;

@SuppressWarnings("common-java:DuplicatedBlocks")
@AutoValue
@AutoGson(autoValueClass = AutoValue_AttributeResponseData.class)
public abstract class AttributeResponseData {

  public abstract String getId();

  public abstract String getName();

  public abstract Integer getSequenceNr();

  public abstract String getType();

  public abstract boolean isIdAttribute();

  public abstract boolean isLabelAttribute();

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
  public abstract List<Sort> getOrderBy();

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
  public abstract List<String> getEnumOptions();

  @Nullable
  @CheckForNull
  public abstract List<Category> getCategoricalOptions();

  @Nullable
  @CheckForNull
  public abstract Range getRange();

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

    public abstract Builder setType(String type);

    public abstract Builder setIdAttribute(boolean isIdAttribute);

    public abstract Builder setLabelAttribute(boolean isLabelAttribute);

    public abstract Builder setLookupAttributeIndex(Integer index);

    public abstract Builder setRefEntityType(LinksResponse linksResponse);

    public abstract Builder setCascadeDelete(Boolean isCascadeDelete);

    public abstract Builder setMappedBy(AttributeResponse attribute);

    public abstract Builder setOrderBy(List<Sort> sort);

    public abstract Builder setLabel(String label);

    public abstract Builder setDescription(String description);

    public abstract Builder setLabelI18n(I18nValue label);

    public abstract Builder setDescriptionI18n(I18nValue description);

    public abstract Builder setNullable(boolean isNullable);

    public abstract Builder setAuto(boolean isAuto);

    public abstract Builder setVisible(boolean isVisible);

    public abstract Builder setUnique(boolean isUnique);

    public abstract Builder setReadOnly(boolean isReadOnly);

    public abstract Builder setAggregatable(boolean isAggregatable);

    public abstract Builder setExpression(String expression);

    public abstract Builder setEnumOptions(List<String> enumOptions);

    public abstract Builder setCategoricalOptions(List<Category> categories);

    public abstract Builder setRange(Range range);

    public abstract Builder setParentAttributeId(String parent);

    public abstract Builder setNullableExpression(String nullableExpression);

    public abstract Builder setVisibleExpression(String visibleExpression);

    public abstract Builder setValidationExpression(String validationExpression);

    public abstract Builder setDefaultValue(String defaultValue);

    public abstract AttributeResponseData build();
  }
}
