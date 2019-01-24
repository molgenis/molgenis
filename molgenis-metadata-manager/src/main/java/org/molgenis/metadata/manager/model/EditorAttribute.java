package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttribute.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorAttribute {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getName();

  @Nullable
  @CheckForNull
  public abstract String getType();

  @Nullable
  @CheckForNull
  public abstract EditorAttributeIdentifier getParent();

  @Nullable
  @CheckForNull
  public abstract EditorEntityTypeIdentifier getRefEntityType();

  public abstract boolean isCascadeDelete();

  @Nullable
  @CheckForNull
  public abstract EditorAttributeIdentifier getMappedByAttribute();

  @Nullable
  @CheckForNull
  public abstract EditorSort getOrderBy();

  @Nullable
  @CheckForNull
  public abstract String getExpression();

  public abstract boolean isNullable();

  public abstract boolean isAuto();

  public abstract boolean isVisible();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  public abstract Map<String, String> getLabelI18n();

  @Nullable
  @CheckForNull
  public abstract String getDescription();

  public abstract Map<String, String> getDescriptionI18n();

  public abstract boolean isAggregatable();

  @Nullable
  @CheckForNull
  public abstract List<String> getEnumOptions();

  @Nullable
  @CheckForNull
  public abstract Long getRangeMin();

  @Nullable
  @CheckForNull
  public abstract Long getRangeMax();

  public abstract boolean isReadonly();

  public abstract boolean isUnique();

  public abstract List<EditorTagIdentifier> getTags();

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

  public abstract Integer getSequenceNumber();

  public static EditorAttribute create(
      String id,
      @Nullable @CheckForNull String name,
      @Nullable @CheckForNull String type,
      EditorAttributeIdentifier parent,
      EditorEntityTypeIdentifier refEntityType,
      boolean cascadeDelete,
      EditorAttributeIdentifier mappedByAttribute,
      EditorSort orderBy,
      String expression,
      boolean nullable,
      boolean auto,
      boolean visible,
      @Nullable @CheckForNull String label,
      Map<String, String> i18nLabel,
      @Nullable @CheckForNull String description,
      Map<String, String> i18nDescription,
      boolean aggregatable,
      @Nullable @CheckForNull List<String> enumOptions,
      @Nullable @CheckForNull Long rangeMin,
      @Nullable @CheckForNull Long rangeMax,
      boolean readonly,
      boolean unique,
      List<EditorTagIdentifier> tags,
      @Nullable @CheckForNull String nullableExpression,
      @Nullable @CheckForNull String visibleExpression,
      @Nullable @CheckForNull String validationExpression,
      @Nullable @CheckForNull String defaultValue,
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
