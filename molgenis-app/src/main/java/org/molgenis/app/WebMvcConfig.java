package org.molgenis.app;

import org.molgenis.web.i18n.HttpLocaleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

@Configuration
public class WebMvcConfig extends DelegatingWebMvcConfiguration {

  @SuppressWarnings("java:S3305") // Cannot add dependency to overriding method
  @Autowired HttpLocaleResolver localeResolver;

  @Bean
  @Override
  public LocaleResolver localeResolver() {
    return localeResolver;
  }
}
