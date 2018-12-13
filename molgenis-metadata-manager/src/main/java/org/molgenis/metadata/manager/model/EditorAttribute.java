package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttribute.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorAttribute {
  public abstract String getId();

  @CheckForNull
  public abstract String getName();

  @CheckForNull
  public abstract String getType();

  @CheckForNull
  public abstract EditorAttributeIdentifier getParent();

  @CheckForNull
  public abstract EditorEntityTypeIdentifier getRefEntityType();

  public abstract boolean isCascadeDelete();

  @CheckForNull
  public abstract EditorAttributeIdentifier getMappedByAttribute();

  @CheckForNull
  public abstract EditorSort getOrderBy();

  @CheckForNull
  public abstract String getExpression();

  public abstract boolean isNullable();

  public abstract boolean isAuto();

  public abstract boolean isVisible();

  @CheckForNull
  public abstract String getLabel();

  public abstract Map<String, String> getLabelI18n();

  @CheckForNull
  public abstract String getDescription();

  public abstract Map<String, String> getDescriptionI18n();

  public abstract boolean isAggregatable();

  @CheckForNull
  public abstract List<String> getEnumOptions();

  @CheckForNull
  public abstract Long getRangeMin();

  @CheckForNull
  public abstract Long getRangeMax();

  public abstract boolean isReadonly();

  public abstract boolean isUnique();

  public abstract List<EditorTagIdentifier> getTags();

  @CheckForNull
  public abstract String getNullableExpression();

  @CheckForNull
  public abstract String getVisibleExpression();

  @CheckForNull
  public abstract String getValidationExpression();

  @CheckForNull
  public abstract String getDefaultValue();

  public abstract Integer getSequenceNumber();

  public static EditorAttribute create(
      String id,
      @CheckForNull String name,
      @CheckForNull String type,
      EditorAttributeIdentifier parent,
      EditorEntityTypeIdentifier refEntityType,
      boolean cascadeDelete,
      EditorAttributeIdentifier mappedByAttribute,
      EditorSort orderBy,
      String expression,
      boolean nullable,
      boolean auto,
      boolean visible,
      @CheckForNull String label,
      Map<String, String> i18nLabel,
      @CheckForNull String description,
      Map<String, String> i18nDescription,
      boolean aggregatable,
      @CheckForNull List<String> enumOptions,
      @CheckForNull Long rangeMin,
      @CheckForNull Long rangeMax,
      boolean readonly,
      boolean unique,
      List<EditorTagIdentifier> tags,
      @CheckForNull String nullableExpression,
      @CheckForNull String visibleExpression,
      @CheckForNull String validationExpression,
      @CheckForNull String defaultValue,
      Integer sequenceNumber) {
    return new AutoValue_EditorAttribute(
        id,
        name,
        type,
        parent,
        refEntityType,
        cascadeDelete,
        mappedByAttribute,
        orderBy,
        expression,
        nullable,
        auto,
        visible,
        label,
        i18nLabel,
        description,
        i18nDescription,
        aggregatable,
        enumOptions,
        rangeMin,
        rangeMax,
        readonly,
        unique,
        tags,
        nullableExpression,
        visibleExpression,
        validationExpression,
        defaultValue,
        sequenceNumber);
  }
}
