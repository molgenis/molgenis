package org.molgenis.questionnaires.config;

import org.molgenis.util.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuestionnaireConfig {
  @Bean
  public PropertiesMessageSource questionnaireMessageSource() {
    return new PropertiesMessageSource("questionnaire");
  }
}
