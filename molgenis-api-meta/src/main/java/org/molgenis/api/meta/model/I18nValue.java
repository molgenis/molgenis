package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_I18nValue.class)
public abstract class I18nValue {

  @Nullable
  @CheckForNull
  public abstract String getDefaultValue();

  @Nullable
  @CheckForNull
  public abstract Map<String, String> getTranslations();

  public static I18nValue create(String defaultValue, Map<String, String> translations) {
    return I18nValue.builder().setDefaultValue(defaultValue).setTranslations(translations).build();
  }

  public static Builder builder() {
    return new AutoValue_I18nValue.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to Integererfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setDefaultValue(String defaultValue);

    public abstract Builder setTranslations(Map<String, String> name);

    public abstract I18nValue build();
  }
}
