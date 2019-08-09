package org.molgenis.api;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiL10nConfig {
  public static final String NAMESPACE = "api";

  @Bean
  public PropertiesMessageSource apiMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
