package org.molgenis.api.metadata.v3;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiMetadataL10nConfig {
  public static final String NAMESPACE = "api-metadata";

  @Bean
  public PropertiesMessageSource apiMetadataMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
