package org.molgenis.data.i18n;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.i18n.LocalizationMessageSource;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.settings.AppSettings;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class LocalizationConfig {
  private final L10nStringFactory l10nStringFactory;
  private final DataService dataService;
  private final AppSettings appSettings;
  private final MessageFormatFactory messageFormatFactory;

  public LocalizationConfig(
      L10nStringFactory l10nStringFactory,
      DataService dataService,
      AppSettings appSettings,
      MessageFormatFactory messageFormatFactory) {
    this.l10nStringFactory = requireNonNull(l10nStringFactory);
    this.dataService = requireNonNull(dataService);
    this.appSettings = requireNonNull(appSettings);
    this.messageFormatFactory = requireNonNull(messageFormatFactory);
  }

  @Bean
  public LocalizationPopulator localizationPopulator() {
    return new LocalizationPopulator(localizationRepository(), l10nStringFactory);
  }

  @Bean
  public LocalizationService localizationRepository() {
    return new LocalizationService(dataService, l10nStringFactory);
  }

  @Bean
  public MessageSource messageSource() {
    LocalizationMessageSource localizationMessageSource =
        new LocalizationMessageSource(
            messageFormatFactory,
            localizationRepository(),
            () -> new Locale(appSettings.getLanguageCode()));
    ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
    resourceBundleMessageSource.addBasenames("org.hibernate.validator.ValidationMessages");
    localizationMessageSource.setParentMessageSource(resourceBundleMessageSource);
    MessageSourceHolder.setMessageSource(localizationMessageSource);
    return localizationMessageSource;
  }
}
