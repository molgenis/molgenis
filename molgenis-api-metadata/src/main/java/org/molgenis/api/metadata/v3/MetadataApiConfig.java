package org.molgenis.api.metadata.v3;

import org.molgenis.api.convert.SortConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MetadataApiConfig implements WebMvcConfigurer {
  @Bean
  public SortConverter metadataSortConverter() {
    return new SortConverter();
  }
}
