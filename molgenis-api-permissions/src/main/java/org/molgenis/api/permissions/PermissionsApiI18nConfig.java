package org.molgenis.api.permissions;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermissionsApiI18nConfig {

  private static final String NAMESPACE = "api-permissions";

  @Bean
  public PropertiesMessageSource securityApiMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
