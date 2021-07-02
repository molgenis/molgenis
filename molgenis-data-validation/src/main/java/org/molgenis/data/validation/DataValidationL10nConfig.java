package org.molgenis.data.validation;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataValidationL10nConfig {
  public static final String NAMESPACE = "data-validation";

  @Bean
  public PropertiesMessageSource dataValidationMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
