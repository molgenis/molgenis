package org.molgenis.api.data.v3;

import org.molgenis.api.SortConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DataApiConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(sort());
  }

  @Bean
  public SortConverter sort() {
    return new SortConverter();
  }
}
