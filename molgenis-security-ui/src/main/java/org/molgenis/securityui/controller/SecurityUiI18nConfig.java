package org.molgenis.securityui.controller;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityUiI18nConfig {

  public static final String NAMESPACE = "security-ui";

  @Bean
  public PropertiesMessageSource securityUiMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
