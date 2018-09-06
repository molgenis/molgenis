package org.molgenis.app.manager.config;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppManagerConfig {
  public static final String NAMESPACE = "app-manager";

  @Bean
  public PropertiesMessageSource appManagerMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
