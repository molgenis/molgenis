package org.molgenis.script.controller;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptsI18nConfig {

  public static final String NAMESPACE = "scripts";

  @Bean
  public PropertiesMessageSource ScriptsI18nMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
