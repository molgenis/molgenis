package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateAttributeRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CreateAttributeRequest {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract String getName();

  @Nullable
  @CheckForNull
  public abstract String getType();

  @Nullable
  @CheckForNull
  public abstract String getParent();

  @Nullable
  @CheckForNull
  public abstract String getRefEntityType();

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

  public abstract Boolean getNullable();

  public abstract Boolean getAuto();

  public abstract Boolean getVisible();

  @Nullable
  @CheckForNull
  public abstract I18nValue getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  public abstract Boolean getAggregatable();

  @Nullable
  @CheckForNull
  public abstract List<String> getEnumOptions();

  @Nullable
  @CheckForNull
  public abstract Long getRangeMin();

  @Nullable
  @CheckForNull
  public abstract Long getRangeMax();

  public abstract Boolean getReadonly();

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

  public abstract Integer getSequenceNumber();

  public static CreateAttributeRequest create(
      String id,
      @Nullable @CheckForNull String name,
      @Nullable @CheckForNull String type,
      String parent,
      String refEntityType,
      boolean cascadeDelete,
      String mappedByAttribute,
      String orderBy,
      String expression,
      boolean nullable,
      boolean auto,
      boolean visible,
      @Nullable @CheckForNull I18nValue label,
      @Nullable @CheckForNull I18nValue description,
      boolean aggregatable,
      @Nullable @CheckForNull List<String> enumOptions,
      @Nullable @CheckForNull Long rangeMin,
      @Nullable @CheckForNull Long rangeMax,
      boolean readonly,
      boolean unique,
      @Nullable @CheckForNull String nullableExpression,
      @Nullable @CheckForNull String visibleExpression,
      @Nullable @CheckForNull String validationExpression,
      @Nullable @CheckForNull String defaultValue,
      Integer sequenceNumber) {
    return new AutoValue_CreateAttributeRequest(
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
        description,
        aggregatable,
        enumOptions,
        rangeMin,
        rangeMax,
        readonly,
        unique,
        nullableExpression,
        visibleExpression,
        validationExpression,
        defaultValue,
        sequenceNumber);
  }
}
