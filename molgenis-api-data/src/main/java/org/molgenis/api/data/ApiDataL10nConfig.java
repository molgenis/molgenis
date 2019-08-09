package org.molgenis.api.data;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiDataL10nConfig {
  public static final String NAMESPACE = "api-data";

  @Bean
  public PropertiesMessageSource apiDataMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
