package org.molgenis.api;

import org.molgenis.api.convert.SortConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiConfig implements WebMvcConfigurer {
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(sortConverter());
  }

  @Bean
  public SortConverter sortConverter() {
    return new SortConverter();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping(ApiNamespace.API_PATH + "/**").allowedMethods("*");
  }
}
