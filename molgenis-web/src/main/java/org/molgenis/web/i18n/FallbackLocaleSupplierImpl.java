package org.molgenis.web.i18n;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Component;

@Component
public class FallbackLocaleSupplierImpl implements FallbackLocaleSupplier {
  private final AppSettings appSettings;

  FallbackLocaleSupplierImpl(AppSettings appSettings) {
    this.appSettings = requireNonNull(appSettings);
  }

  @Override
  public Locale get() {
    String languageCode = appSettings.getLanguageCode();
    return Locale.forLanguageTag(languageCode);
  }
}
