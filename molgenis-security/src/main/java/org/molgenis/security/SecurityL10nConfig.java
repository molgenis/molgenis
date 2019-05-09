package org.molgenis.security;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityL10nConfig {
  public static final String NAMESPACE = "security";

  @Bean
  public PropertiesMessageSource securityMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
