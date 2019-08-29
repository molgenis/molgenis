package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Attribute.class)
public abstract class Attribute {
  public abstract LinksResponse getLinks();

  public abstract String getId();

  public abstract String getName();

  public abstract int getSequenceNr();

  public abstract AttributeType getType();

  public abstract boolean isIdAttribute();

  public abstract boolean isLabelAttribute();

  public abstract int getLookupAttributeIndex();

  public abstract String getRefEntityTypeId();

  public abstract boolean isCascadeDelete();

  public abstract Attribute getMappedBy();

  public abstract Sort getOrderBy();

  public abstract String getLabel();

  public abstract String getDescription();

  public abstract boolean isNullable();

  public abstract boolean isAuto();

  public abstract boolean isVisible();

  public abstract boolean isUnique();

  public abstract boolean isReadOnly();

  public abstract boolean isAggregatable();

  public abstract String getExpression();

  public abstract String getEnumOptions();

  public abstract int getRangeMin();

  public abstract int getRangeMax();

  public abstract Attribute getParent();

  public abstract List<Attribute> getChildren();

  public abstract List<String> getTags();

  public abstract String getNullableExpression();

  public abstract String getVisibleExpression();

  public abstract String getValidationExpression();

  public abstract String getDefaultValue();

  public static Builder builder() {
    return new AutoValue_Attribute.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLinks(LinksResponse newLinks);

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setSequenceNr(int sequenceNr);

    public abstract Builder setType(AttributeType type);

    public abstract Builder setIdAttribute(boolean isIdAttr);

    public abstract Builder setLabelAttribute(boolean isLabelAttr);

    public abstract Builder setLookupAttributeIndex(int index);

    public abstract Builder setRefEntityTypeId(String refEntityTypeId);

    public abstract Builder setCascadeDelete(boolean isCascadeDelete);

    public abstract Builder setMappedBy(Attribute attribute);

    public abstract Builder setOrderBy(Sort sort);

    public abstract Builder setLabel(String label);

    public abstract Builder setDescription(String description);

    public abstract Builder setNullable(boolean isNullable);

    public abstract Builder setAuto(boolean isAuto);

    public abstract Builder setVisible(boolean isVisible);

    public abstract Builder setUnique(boolean isUnique);

    public abstract Builder setReadOnly(boolean isReadOnly);

    public abstract Builder setAggregatable(boolean isAggregatable);

    public abstract Builder setExpression(String expression);

    public abstract Builder setEnumOptions(String enumOptions);

    public abstract Builder setRangeMin(int min);

    public abstract Builder setRangeMax(int max);

    public abstract Builder setParent(Attribute parent);

    public abstract Builder setChildren(List<Attribute> children);

    public abstract Builder setTags(List<String> tags);

    public abstract Builder setNullableExpression(String nullableExpression);

    public abstract Builder setVisibleExpression(String visibleExpression);

    public abstract Builder setValidationExpression(String validationExpression);

    public abstract Builder setDefaultValue(String defaultValue);

    public abstract Attribute build();
  }
}
