package org.molgenis.api.metadata.v3;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.data.InvalidValueTypeException;

class I18nValueMapper {
  private I18nValueMapper() {}

  static I18nValue toI18nValue(Object value) {
    I18nValue.Builder builder = I18nValue.builder();
    if (value instanceof Map) {
      Map valueMap = (Map) value;
      Object defaultValue = valueMap.get("defaultValue");
      if (defaultValue != null) {
        builder.setDefaultValue(defaultValue.toString());
      }
      Object translations = valueMap.get("translations");
      if (translations instanceof Map) {
        Map<?, ?> translationsMap = (Map) translations;
        Map<String, String> typedTranslations = new HashMap<>();
        for (Entry entry : translationsMap.entrySet()) {
          if (entry.getKey() != null && entry.getValue() != null) {
            typedTranslations.put(entry.getKey().toString(), entry.getValue().toString());
          }
        }
        builder.setTranslations(typedTranslations);
      }
    } else {
      throw new InvalidValueTypeException(value.toString(), "I18nValue", null);
    }
    return builder.build();
  }
}
