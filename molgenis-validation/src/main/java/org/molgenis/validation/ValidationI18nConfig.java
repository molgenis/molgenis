package org.molgenis.validation;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationI18nConfig {

  public static final String NAMESPACE = "validation";

  @Bean
  public PropertiesMessageSource validationMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
