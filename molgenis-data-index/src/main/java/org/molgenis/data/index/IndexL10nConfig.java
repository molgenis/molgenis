package org.molgenis.data.index;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexL10nConfig {
  public static final String NAMESPACE = "index";

  @Bean
  public PropertiesMessageSource indexMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
