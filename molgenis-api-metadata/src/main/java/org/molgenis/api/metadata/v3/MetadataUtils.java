package org.molgenis.api.metadata.v3;

import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.List;
import java.util.Map.Entry;
import org.molgenis.data.Entity;
import org.molgenis.data.InvalidValueTypeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

public class MetadataUtils {
  private MetadataUtils() {}

  public static <E extends Entity> ImmutableMap<String, String> getI18n(
      E entity, String attributeName) {
    Builder<String, String> builder = ImmutableMap.builder();
    getLanguageCodes()
        .forEach(
            languageCode -> {
              String value = entity.getString(getI18nAttributeName(attributeName, languageCode));
              if (value != null) {
                builder.put(languageCode, value);
              }
            });
    return builder.build();
  }


  static void setSequenceNumber(Attribute attribute, Entry<String, Object> entry) {
    String sequenceString = getStringValue(entry.getValue());
    if (sequenceString != null) {
      attribute.setSequenceNumber(Integer.valueOf(sequenceString));
    } else {
      throw new InvalidValueTypeException(sequenceString, "int", null);
    }
  }

  static void setAttributeType(Attribute attribute, Entry<String, Object> entry) {
    String typeString = getStringValue(entry.getValue());
    if (typeString != null) {
      attribute.setDataType(AttributeType.toEnum(typeString));
    } else {
      throw new InvalidValueTypeException(typeString, "AttributeType", null);
    }
  }

  static void setEnumOptions(Attribute attribute, Entry<String, Object> entry) {
    List<String> options;
    if (entry.getValue() instanceof List) {
      options = (List<String>) entry.getValue();
    } else {
      throw new InvalidValueTypeException(entry.getValue().toString(), "list", null);
    }
    attribute.setEnumOptions(options);
  }

  static void setBooleanValue(Entity entity, Entry<String, Object> entry, String fieldName) {
    String readOnlyValue = getStringValue(entry.getValue());
    if (readOnlyValue != null) {
      entity.set(fieldName, Boolean.valueOf(readOnlyValue));
    } else {
      throw new InvalidValueTypeException(readOnlyValue, "boolean", null);
    }
  }

  static String getStringValue(Object value) {
    return value != null ? value.toString() : null;
  }
}
