package org.molgenis.data.importer;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportI18nConfig {
  private static final String NAMESPACE = "data-import";

  @Bean
  public PropertiesMessageSource dataImportMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
