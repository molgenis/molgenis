package org.molgenis.api.metadata.v3.model;

import java.util.List;

@SuppressWarnings("squid:S1610") // classes without fields should be converted to interfaces
public class CreateAttributeRequest {
  String id;
  String name;
  String type;
  String parent;
  String refEntityType;
  Boolean cascadeDelete;
  String mappedByAttribute;
  String orderBy;
  String expression;
  boolean nullable;
  boolean auto;
  boolean visible;
  I18nValue label;
  I18nValue description;
  boolean aggregatable;
  List<String> enumOptions;
  Range range;
  boolean readonly;
  boolean unique;
  String nullableExpression;
  String visibleExpression;
  String validationExpression;
  String defaultValue;
  Integer sequenceNumber;

  public CreateAttributeRequest(
      String id,
      String name,
      String type,
      String parent,
      String refEntityType,
      Boolean cascadeDelete,
      String mappedByAttribute,
      String orderBy,
      String expression,
      boolean nullable,
      boolean auto,
      boolean visible,
      I18nValue label,
      I18nValue description,
      boolean aggregatable,
      List<String> enumOptions,
      Range range,
      boolean readonly,
      boolean unique,
      String nullableExpression,
      String visibleExpression,
      String validationExpression,
      String defaultValue,
      Integer sequenceNumber) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.parent = parent;
    this.refEntityType = refEntityType;
    this.cascadeDelete = cascadeDelete;
    this.mappedByAttribute = mappedByAttribute;
    this.orderBy = orderBy;
    this.expression = expression;
    this.nullable = nullable;
    this.auto = auto;
    this.visible = visible;
    this.label = label;
    this.description = description;
    this.aggregatable = aggregatable;
    this.enumOptions = enumOptions;
    this.range = range;
    this.readonly = readonly;
    this.unique = unique;
    this.nullableExpression = nullableExpression;
    this.visibleExpression = visibleExpression;
    this.validationExpression = validationExpression;
    this.defaultValue = defaultValue;
    this.sequenceNumber = sequenceNumber;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getParent() {
    return parent;
  }

  public String getRefEntityType() {
    return refEntityType;
  }

  public Boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public String getMappedByAttribute() {
    return mappedByAttribute;
  }

  public String getOrderBy() {
    return orderBy;
  }

  public String getExpression() {
    return expression;
  }

  public Boolean isNullable() {
    return nullable;
  }

  public Boolean isAuto() {
    return auto;
  }

  public Boolean isVisible() {
    return visible;
  }

  public I18nValue getLabel() {
    return label;
  }

  public I18nValue getDescription() {
    return description;
  }

  public Boolean isAggregatable() {
    return aggregatable;
  }

  public List<String> getEnumOptions() {
    return enumOptions;
  }

  public Range getRange() {
    return range;
  }

  public Boolean isReadonly() {
    return readonly;
  }

  public Boolean isUnique() {
    return unique;
  }

  public String getNullableExpression() {
    return nullableExpression;
  }

  public String getVisibleExpression() {
    return visibleExpression;
  }

  public String getValidationExpression() {
    return validationExpression;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Integer getSequenceNumber() {
    return sequenceNumber;
  }
}
