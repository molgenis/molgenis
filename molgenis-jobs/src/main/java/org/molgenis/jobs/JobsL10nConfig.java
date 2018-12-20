package org.molgenis.jobs;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobsL10nConfig {
  public static final String NAMESPACE = "jobs";

  @Bean
  public PropertiesMessageSource jobsMessageSource() {
    return new PropertiesMessageSource(NAMESPACE);
  }
}
