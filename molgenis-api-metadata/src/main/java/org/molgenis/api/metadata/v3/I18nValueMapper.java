package org.molgenis.api.metadata.v3;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.molgenis.api.metadata.v3.model.I18nValue;
import org.molgenis.data.InvalidValueTypeException;

class I18nValueMapper {
  private I18nValueMapper() {}

  @SuppressWarnings("unchecked")
  static I18nValue toI18nValue(Object value) {
    I18nValue.Builder builder = I18nValue.builder();
    if (value instanceof Map<?, ?>) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      Object defaultValue = valueMap.get("defaultValue");
      if (defaultValue != null) {
        builder.setDefaultValue(defaultValue.toString());
      }
      Object translations = valueMap.get("translations");
      if (translations instanceof Map<?, ?>) {
        builder.setTranslations(ImmutableMap.copyOf((Map<String, String>) translations));
      }
    } else {
      throw new InvalidValueTypeException(value.toString(), "I18nValue", null);
    }
    return builder.build();
  }
}
