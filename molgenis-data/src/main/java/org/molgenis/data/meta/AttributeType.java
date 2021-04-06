package org.molgenis.data.meta;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public enum AttributeType {
  BOOL,
  CATEGORICAL,
  CATEGORICAL_MREF,
  COMPOUND,
  DATE,
  DATE_TIME,
  DECIMAL,
  EMAIL(255),
  ENUM(255),
  FILE,
  HTML(65535),
  HYPERLINK(255),
  INT,
  LONG,
  MREF,
  ONE_TO_MANY,
  SCRIPT(65535),
  STRING(255),
  TEXT(65535),
  XREF;

  private static final Map<String, AttributeType> strValMap;

  static {
    AttributeType[] dataTypes = AttributeType.values();
    strValMap = newHashMapWithExpectedSize(dataTypes.length);
    for (AttributeType dataType : dataTypes) {
      strValMap.put(getValueString(dataType), dataType);
    }
  }

  private final Integer maxLength;

  AttributeType() {
    this.maxLength = null;
  }

  AttributeType(int maxLength) {
    this.maxLength = maxLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  /**
   * Returns the enum value for the given value string
   *
   * @param valueString value string
   * @return enum value
   */
  public static AttributeType toEnum(String valueString) {
    return strValMap.get(normalize(valueString));
  }

  /**
   * Returns the value string for the given enum value
   *
   * @param value enum value
   * @return value string
   */
  public static String getValueString(AttributeType value) {
    return normalize(value.toString());
  }

  /**
   * Returns the value strings for all enum types in the defined enum order
   *
   * @return value strings
   */
  public static List<String> getOptionsLowercase() {
    return Arrays.stream(values()).map(AttributeType::getValueString).collect(toList());
  }

  private static String normalize(String valueString) {
    return StringUtils.remove(valueString, '_').toLowerCase();
  }
}
