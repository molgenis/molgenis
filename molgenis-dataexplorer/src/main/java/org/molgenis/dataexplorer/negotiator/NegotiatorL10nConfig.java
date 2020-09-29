package org.molgenis.dataexplorer.negotiator;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NegotiatorL10nConfig {
  public static final String NAMESPACE = "negotiator";

  @Bean
  public PropertiesMessageSource negotiatorMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
