package org.molgenis.data.config;

import javax.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class ValidationConfig {
  @Bean
  public MethodValidationPostProcessor methodValidationPostProcessor() {
    MethodValidationPostProcessor methodValidationPostProcessor =
        new MethodValidationPostProcessor();
    methodValidationPostProcessor.setValidator(validator());
    return methodValidationPostProcessor;
  }

  @Bean
  public Validator validator() {
    return new LocalValidatorFactoryBean();
  }
}
