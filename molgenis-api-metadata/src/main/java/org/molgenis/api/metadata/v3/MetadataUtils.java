package org.molgenis.api.metadata.v3;

import static org.molgenis.data.util.AttributeUtils.getI18nAttributeName;
import static org.molgenis.util.i18n.LanguageService.getLanguageCodes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.molgenis.data.Entity;

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
}
