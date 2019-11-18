package org.molgenis.api.data.v3;

import org.molgenis.api.convert.SelectionConverter;
import org.molgenis.api.data.DataApiConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Import(DataApiConfig.class)
@Configuration
public class DataApiV3Config implements WebMvcConfigurer {
  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(selectionConverter());
  }

  @Bean
  public SelectionConverter selectionConverter() {
    return new SelectionConverter();
  }

  @Bean
  public FetchMapper fetchMapper() {
    return new FetchMapper();
  }
}
